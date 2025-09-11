package com.RecipeCode.teamproject.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                // 개발용: CSRF 완전 비활성화(POST/PUT 테스트 편의)
                .csrf(csrf -> csrf.disable())

                // 개발용: 전부 열어두기
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/search").permitAll() // JSP 뷰
                        .requestMatchers("/css/**","/js/**","/images/**","/webjars/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/search").permitAll() // 검색 API
                        .anyRequest().permitAll()
                )

                // 로그인/기본인증 비활성화
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .build();
    }
}
