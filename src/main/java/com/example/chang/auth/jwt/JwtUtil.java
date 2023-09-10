package com.example.chang.auth.jwt;


import com.example.chang.auth.refresh.RefreshToken;
import com.example.chang.auth.refresh.RefreshTokenRepository;
import com.example.chang.auth.refresh.TokenDto;
import com.example.chang.auth.security.UserDetailsServiceImpl;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtil {
    private final UserDetailsServiceImpl userDetailsService;

    private final RefreshTokenRepository refreshTokenRepository;
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String REFRESH_TOKEN = "RefreshToken";
    public static final String AUTHORIZATION_KEY = "auth";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final long TOKEN_TIME = 60 * 60 * 1000L;
    private static final long REFRESH_TIME = 72 * 60 * 60 * 1000L;

    @Value("${jwt.secret.key}")
    private String secretKey;
    private Key key;
    private final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256; // 해시알고리즘 256으로 만듬 키값이 얼마나되든

    // 초기화를 수행하는 메서드
    @PostConstruct
    public void init() {
        byte[] bytes = Base64.getDecoder().decode(secretKey);
        key = Keys.hmacShaKeyFor(bytes);
    }

    /*
     *   [header 토큰을 가져오기]
     *   String 으로 받은 타입이 Access 이면 AUTHORIZATION_HEADER 를 아니라면 REFRESH_TOKEN 를 가져온다.
     *   bearerToken 이 빈값이 아니고 BEARER_PREFIX("Bearer ")로 시작한다면 "Bearer "(7자)를 제외하고 반환한다.
     *   만약 조건에 해당하지 않는경우 "토큰값이 존재하지 않습니다." 메시지 반환.
     */
    public String resolveToken(HttpServletRequest request, String type) {
        String bearerToken = type.equals("Access") ? request.getHeader(AUTHORIZATION_HEADER) :request.getHeader(REFRESH_TOKEN);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(7);
        }
        //      null 조사하기
        return null;
    }

    /*
     *  [토큰생성]
     *  토큰안에 정보넣어준것 없어도됨 누구나 decode 가능 base 64기반으로 초기화 된 키값을 이용.
     *  serialized 객체를 통일해서 보내야함 2진수로 바꿈 (low level)
     *  signature - 내가 발행한 유효한 토큰인지 확인 단방향 암호
     */
    public TokenDto createAllToken(String email, String id) {
        return new TokenDto(createToken(email), createRefreshToken(id));
    }
    public String createToken(String email) {
        Date date = new Date();


        return BEARER_PREFIX +
                Jwts.builder()
                        .setSubject(email) // 토큰안에 정보넣어준것 없어도됨 누구나 decode 가능 base 64
                        .setExpiration(new Date(date.getTime() + TOKEN_TIME)) // serialized 객체를 통일해서 보내야함 2진수로 바꿈 (low level)
                        .setIssuedAt(date)
                        .signWith(key, signatureAlgorithm) // signature - 내가 발행한 유효한 토큰인지 확인 단방향 암호
                        .compact();
    }
    public String createRefreshToken(String id) {
        Date date = new Date();

        return BEARER_PREFIX +
                Jwts.builder()// 토큰안에 정보넣어준것 없어도됨 누구나 decode 가능 base 64
                        .setSubject(id)
                        .setExpiration(new Date(date.getTime() + REFRESH_TIME)) // serialized 객체를 통일해서 보내야함 2진수로 바꿈 (low level)
                        .setIssuedAt(date)
                        .signWith(key, signatureAlgorithm) // signature - 내가 발행한 유효한 토큰인지 확인 단방향 암호
                        .compact();
    }

    // 토큰 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.info("Invalid JWT signature, 유효하지 않는 JWT 서명 입니다.");
        } catch (ExpiredJwtException e) {
            log.info("Expired JWT token, 만료된 JWT token 입니다.");
        } catch (UnsupportedJwtException e) {
            log.info("Unsupported JWT token, 지원되지 않는 JWT 토큰 입니다.");
        } catch (IllegalArgumentException e) {
            log.info("JWT claims is empty, 잘못된 JWT 토큰 입니다.");
        }
        return false;
    }

    /*
    [refreshToken 검증]
    1차 토큰 검증
    DB에 저장한 토큰 비교
    */
    public Boolean refreshTokenValidation(String token) {

        if(!validateToken(token)) return false;

        Optional<RefreshToken> refreshToken = refreshTokenRepository.findAllByMemberId(getUserId(token));

        return refreshToken.isPresent() && token.equals(refreshToken.get().getRefreshToken().substring(7));
    }

    // 토큰에서 사용자 정보 가져오기
    public Claims getUserInfoFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }

    // 인증 객체 생성
    public Authentication createAuthentication(String username) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    // 토큰에서 userId 가져오는 기능
    public String getUserId(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().getSubject();
    }

    //  Header에 Token을 추가하는 메서드
    public void setHeader(HttpServletResponse response, TokenDto tokenDto) {
        response.addHeader(JwtUtil.AUTHORIZATION_HEADER, tokenDto.getAuthorization());
        response.addHeader(JwtUtil.REFRESH_TOKEN, tokenDto.getRefresh_Token());
    }

}