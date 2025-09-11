package com.RecipeCode.teamproject.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .formLogin(form -> form
                        .loginPage("/auth/login")                                // 사용자 정의 로그인 페이지
                        .loginProcessingUrl("/auth/loginProcess")                // 로그인 처리 URL
                        .usernameParameter("userEmail")                              // form에서 name="email"
                        .defaultSuccessUrl("/auth/login", true)     // 로그인 성공 시 이동
                        .failureUrl("/errors") // 실패 시 이동
                        .permitAll())
                .httpBasic(basic -> basic.disable())
                .build();
    }
}