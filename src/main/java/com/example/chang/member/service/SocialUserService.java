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
public class SocialUserService {

    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;

    @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
    private String redirect;
    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String client;
    @Value("${spring.security.oauth2.client.registration.kakao.authorization-grant-type}")
    private String authorizationType;
    @Value("${spring.security.oauth2.client.registration.kakao.client-secret}")
    private String secret;

    public ApiResponseDto<SocialUserInfoDto> kakaoLogin(String code, HttpServletResponse response) throws JsonProcessingException {
        String accessToken = getToken(code);
        SocialUserInfoDto userInfoDto = getKakaoUserInfo(accessToken);
        Member member = registerKakaoUserIfNeeded(userInfoDto);
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


//    카카오
    private String getToken(String code) throws JsonProcessingException{
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type","application/x-www-form-urlencoded;charset=utf-8");
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type",authorizationType);
        body.add("client_secret",secret);
        body.add("client_id",client);
        body.add("redirect_uri",redirect);
        body.add("redirect_uri","http://localhost:8080/oauth/callback/kakao");
        body.add("code",code);

        HttpEntity<MultiValueMap<String, String>> kakaoTokenRequest = new HttpEntity<>(body, headers);
        RestTemplate rt = new RestTemplate();
        ResponseEntity<String> response = null;
        try {
            response = rt.exchange(
                    "https://kauth.kakao.com/oauth/token",
                    HttpMethod.POST,
                    kakaoTokenRequest,
                    String.class
            );
        } catch (HttpClientErrorException e) {
            log.error("Kakao API authentication failed with status code " + e.getRawStatusCode());
            log.error("Response headers: " + e.getResponseHeaders());
            log.error("Response body: " + e.getResponseBodyAsString());
            throw e;
        }
        String responseBody = response.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(responseBody);

        return jsonNode.get("access_token").asText();
    }
    private SocialUserInfoDto getKakaoUserInfo(String accessToken) throws JsonProcessingException {
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
                    "https://kapi.kakao.com/v2/user/me",
                    HttpMethod.POST,
                    kakaoUserInfoRequest,
                    String.class
            );
            responseBody = response.getBody();
        } catch (HttpClientErrorException ex) {
            log.error(ex.getMessage());
            log.error("Kakao API authentication failed with status code " + ex.getRawStatusCode());
            throw ex;
        }

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(responseBody);

        Long id = jsonNode.get("id").asLong();
        String nickname = jsonNode.get("properties")
                .get("nickname").asText();
        String email = jsonNode.get("kakao_account")
                .get("email").asText();

        log.info("카카오 사용자 정보: " + id + ", " + nickname + ", " + email);
        return new SocialUserInfoDto(nickname, email);
    }


    // 3. 필요시에 회원가입
    private Member registerKakaoUserIfNeeded(SocialUserInfoDto userInfo) {
        // 로그인 타입 && 사용자 EMAIL로 회원 유무 확인
        Member findUser = (Member) memberRepository.findByEmail(userInfo.getEmail())
                .orElse(null);
        if(findUser == null){
            findUser = memberRepository.save(Member.builder()
                    .username(userInfo.getUsername())
                    .pw(passwordEncoder.encode(UUID.randomUUID().toString()))
                    .email(userInfo.getEmail())
                    .build());
        }
        return findUser;
    }
}
