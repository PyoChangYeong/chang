package com.example.chang.common.config;

import com.example.chang.auth.jwt.JwtAuthFilter;
import com.example.chang.auth.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.Arrays;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity // 스프링 Security 지원을 가능하게 함
@EnableGlobalMethodSecurity(securedEnabled = true) // @Secured 어노테이션 활성화
public class WebSecurityConfig {
    private final JwtUtil jwtUtil;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        // h2-console 사용 및 resources 접근 허용 설정
        return (web) -> web.ignoring()
                // .requestMatchers(PathRequest.toH2Console())
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations());
    }


    //  Spring Security 필터 체인을 구성하는 인터페이스다.
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        // CORS 문제 해결
        http.cors().configurationSource(request -> {
            CorsConfiguration cors = new CorsConfiguration();
            //  모든 패턴 허용
            cors.setAllowedOriginPatterns(List.of("*"));
            //  test
            cors.setAllowedOrigins(List.of("http://localhost:3000"));
            //  API 메서드 허용 범위
            cors.setAllowedMethods(Arrays.asList("GET","POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
            //  test
            cors.setAllowedOrigins(List.of("http://localhost:3000"));
            //  Headers 모든 값
            cors.setAllowedHeaders(List.of("*"));
            //  header token "Authorization"값 허용
            cors.addExposedHeader("Authorization");
            //  header token "Refresh_Token"값 허용
            cors.addExposedHeader("RefreshToken");
            //  내 서버가 응답할 때 json을 JS에서 처리할 수 있게 하려면 설정 (허용하려면 true)
            //  사용자 자격증명과 함께 요청 여부 (Authorization로 사용자 인증 사용 시 true)
            cors.setAllowCredentials(true);
            return cors;
        });
        // CSRF 기능을 비활성화한다.
        http.csrf().disable();
        http.headers().frameOptions().disable();
        http.authorizeRequests()
                // 누구나 h2-console 접속 허용
                .antMatchers("/h2-console/**").permitAll();

        // 기본 설정인 Session 방식은 사용하지 않고 JWT 방식을 사용하기 위한 설정
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        //  요청에 대한 보안 검사를 구성한다.
        http.authorizeRequests()
                .antMatchers("/**").permitAll()
                .antMatchers("/kakao/**").permitAll()
                .antMatchers("/naver/**").permitAll()
                .antMatchers("/google/**").permitAll()

                //  Spring Security 필터 체인을 구성하는 인터페이스다.
                .anyRequest().authenticated()
                // JWT 인증/인가를 사용하기 위한 설정
                // JwtAuthFilter를 UsernamePasswordAuthenticationFilter 이전에 실행되도록 설정한다. JwtAuthFilter는 JWT 토큰을 검증하고 인증/인가를 처리한다.
                .and().addFilterBefore(new JwtAuthFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class);

        // 로그인 페이지를 설정한다
        // http.formLogin().loginPage("/api/user/login-page").permitAll();
        // http.exceptionHandling().accessDeniedPage("/api/user/forbidden");

        return http.build();

    }
}