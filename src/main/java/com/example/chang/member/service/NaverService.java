package com.example.chang.member.service;

import com.example.chang.auth.jwt.JwtUtil;
import com.example.chang.auth.refresh.RefreshToken;
import com.example.chang.auth.refresh.RefreshTokenRepository;
import com.example.chang.auth.refresh.TokenDto;
import com.example.chang.common.ApiResponseDto;
import com.example.chang.common.ResponseUtils;
import com.example.chang.member.dto.SocialUserInfoDto;
import com.example.chang.member.entity.Member;
import com.example.chang.member.repository.MemberRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletResponse;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
@Component
public class NaverService {

    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;


    @Value("${spring.security.oauth2.client.registration.naver.client-id}")
    private String clientId;
    @Value("${spring.security.oauth2.client.registration.naver.client-secret}")
    private String secretKey;
    @Value("${spring.security.oauth2.client.registration.naver.redirect-uri}")
    private String redirectUri;
    @Value("${spring.security.oauth2.client.registration.naver.authorization-grant-type}")
    private String grantType;
    @Value("${spring.security.oauth2.client.provider.naver.token-uri}")
    private String tokenUri;
    @Value("${spring.security.oauth2.client.provider.naver.user-info-uri}")
    private String userInfoUri;

    public ApiResponseDto<SocialUserInfoDto> naverLogin(String code, HttpServletResponse response) throws JsonProcessingException {
        String accessToken = getToken(code);
        SocialUserInfoDto userInfoDto = getNaverUserInfo(accessToken);
        Member member = registerNaverUserIfNeeded(userInfoDto);
        TokenDto tokenDto = jwtUtil.createAllToken(member.getEmail(), String.valueOf(member.getId()));
        Optional<RefreshToken> refreshToken = refreshTokenRepository.findAllByMemberId(member.getEmail());
        if(refreshToken.isPresent()){
            refreshTokenRepository.save(refreshToken.get().updateToken(tokenDto.getRefresh_Token()));
        }else{
            RefreshToken newToken = new RefreshToken(tokenDto.getRefresh_Token(), member.getEmail());
            refreshTokenRepository.save(newToken);
        }
        jwtUtil.setHeader(response,tokenDto);
        return ResponseUtils.ok(SocialUserInfoDto.of(member.getEmail(),member.getUsername()));
    }

    private String getToken(String code) throws JsonProcessingException {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", grantType);
        body.add("client_id", clientId);
        body.add("client_secret", secretKey);
        body.add("redirect_uri", redirectUri);
        body.add("code", code);

        HttpEntity<MultiValueMap<String, String>> kakaoTokenRequest = new HttpEntity<>(body, headers);
        RestTemplate rt = new RestTemplate();
        ResponseEntity<String> response = null;
        try {
            response = rt.exchange(
                    tokenUri,
                    HttpMethod.POST,
                    kakaoTokenRequest,
                    String.class
            );
        } catch (HttpClientErrorException e) {
            log.error("Naver API authentication failed with status code " + e.getRawStatusCode());
            log.error("Response headers: " + e.getResponseHeaders());
            log.error("Response body: " + e.getResponseBodyAsString());
            throw e;
        }
        String responseBody = response.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(responseBody);

        return jsonNode.get("access_token").asText();
    }

    private SocialUserInfoDto getNaverUserInfo(String accessToken) throws JsonProcessingException {
        // HTTP Header 생성
        log.info("access토큰  : " + accessToken);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // HTTP 요청 보내기
        HttpEntity<MultiValueMap<String, String>> kakaoUserInfoRequest = new HttpEntity<>(headers);
        RestTemplate rt = new RestTemplate();
        String responseBody = "";
        try {
            ResponseEntity<String> response = rt.exchange(
                    userInfoUri,
                    HttpMethod.POST,
                    kakaoUserInfoRequest,
                    String.class
            );
            responseBody = response.getBody();
        } catch (HttpClientErrorException ex) {
            log.error(ex.getMessage());
            log.error("Naver API authentication failed with status code " + ex.getRawStatusCode());
            throw ex;
        }

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(responseBody);

        Long id = jsonNode.get("id").asLong();
        String nickname = jsonNode.get("name")
                .get("nickname").asText();
        String email = jsonNode.get("email")
                .get("email").asText();

        log.info("네이버 사용자 정보: " + id + ", " + nickname + ", " + email);
        return new SocialUserInfoDto(nickname, email);
    }

    private Member registerNaverUserIfNeeded(SocialUserInfoDto userInfoDto){
        Member findUser = (Member) memberRepository.findByEmail(userInfoDto.getEmail())
                .orElse(null);
        if(findUser == null){
            findUser = memberRepository.save(Member.builder()
                    .username(userInfoDto.getUsername())
                    .pw(passwordEncoder.encode(UUID.randomUUID().toString()))
                    .email(userInfoDto.getEmail())
                    .build());
        }
        return findUser;
    }
}
