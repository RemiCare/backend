package com.kgu.life_watch.global.config;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import lombok.RequiredArgsConstructor;

import com.kgu.life_watch.global.filter.FilterExceptionHandler;
import com.kgu.life_watch.global.jwt.JwtAuthenticationFilter;
import com.kgu.life_watch.global.jwt.JwtTokenProvider;
import com.kgu.life_watch.global.security.CustomUserDetailsService;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig {

  private final JwtTokenProvider jwtTokenProvider;
  private final CustomUserDetailsService customUserDetailsService;

  @Bean
  public JwtAuthenticationFilter jwtAuthenticationFilter() {
    return new JwtAuthenticationFilter(jwtTokenProvider, customUserDetailsService); // JWT 필터 등록
  }

  @Bean
  public FilterExceptionHandler filterExceptionHandler() {
    return new FilterExceptionHandler();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(); // 비밀번호 암호화를 위한 인코더
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http.csrf(csrf -> csrf.disable()) // JWT 기반 인증이라 CSRF 비활성화
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(
                        "/swagger",
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/api-docs",
                        "/api-docs/**",
                        "/v1/api-docs/**",
                        "/health/**",
                        "/chat/**",
                        "/ws-chat/**")
                    .permitAll()

                    // 모바일 앱 Health Connect 동기화 API 허용
                    .requestMatchers(HttpMethod.POST, "/api/health/sync")
                    .permitAll()
                    .requestMatchers("/api/auth/**", "/api/alert/**")
                    .permitAll() // 로그인과 회원가입은 인증 없이 접근 가능
                    .anyRequest()
                    .authenticated() // 그 외 요청은 인증 필요
            )
        // CORS 설정을 수동으로 추가
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .addFilterBefore(filterExceptionHandler(), UsernamePasswordAuthenticationFilter.class)
        .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
        .build();
  }

  // CORS 설정을 위한 Bean 정의
  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(
        Arrays.asList(
            "http://localhost:3000",
            "http://localhost:8080",
            "https://jiangxy.github.io",
            "https://server.lifewatch.store",
            "https://dqm6a810qt4d9.cloudfront.net",
            "https://www.lifewatch.store")); // 추후에 배포시 수정 필요
    configuration.setAllowedMethods(
        Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS", "HEAD")); // 허용할 HTTP 메서드
    configuration.setAllowedHeaders(
        Arrays.asList("Authorization", "Content-Type", "withCredentials")); // 허용할 헤더
    configuration.setExposedHeaders(Arrays.asList("Authorization")); // 응답에서 노출할 헤더
    configuration.setAllowCredentials(true); // 자격 증명 포함 요청 허용
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration); // 모든 경로에 대해 CORS 설정 적용
    return source;
  }
}
