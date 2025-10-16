package com.RecipeCode.teamproject.config;

import com.RecipeCode.teamproject.reci.auth.handler.CustomAuthenticationFailureHandler;
import com.RecipeCode.teamproject.reci.auth.service.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;



@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final CustomAuthenticationFailureHandler customAuthenticationFailureHandler;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final AuthenticationSuccessHandler oAuth2SuccessHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
//                .csrf(csrf->csrf.disable())\
                // csrf 예외설정
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/api/email-certify/**")
                )
                // 개발용: 전부 열어두기
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/search","/recipes/**","/comments/**").permitAll() // JSP 뷰
                        .requestMatchers("/css/**","/js/**","/images/**","/webjars/**").permitAll()
                        .requestMatchers("/api/email-certify/**").permitAll()
                        .requestMatchers("/admin/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers("/mypage/**", "/profile/**", "/follow/**").authenticated()
                        .requestMatchers("/api/follow/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/search").permitAll() // 검색 API// 댓글 조회 허용
                        .anyRequest().permitAll()

                )
                // 로그인/기본인증 비활성화
                .formLogin(form -> form
                        .loginPage("/auth/login")                                // 사용자 정의 로그인 페이지
                        .loginProcessingUrl("/auth/loginProcess")                // 로그인 처리 URL
                        .usernameParameter("userEmail")                              // form에서 name="email"
                        .successHandler(oAuth2SuccessHandler)
                        .failureHandler(customAuthenticationFailureHandler) // 실패 시 이동
                        .permitAll())
                .oauth2Login(oauth -> oauth
                        .loginPage("/auth/login") // 같은 페이지에서 소셜 버튼 제공
                        .userInfoEndpoint(user -> user.userService(customOAuth2UserService))
                        .successHandler(oAuth2SuccessHandler) // 로그인 성공 후 후처리
                )
                .logout(logout -> logout
                        .logoutUrl("/auth/logout")
                        .logoutSuccessUrl("/auth/login")
                        .invalidateHttpSession(true)           // 세션 무효화
                        .deleteCookies("JSESSIONID"))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            String referer = request.getHeader("Referer");
                            String loginPage = request.getContextPath() + "/auth/login";
                            if (referer != null && !referer.contains("/auth/login")) {
                                loginPage += "?redirect=" + referer;
                            }
                            response.sendRedirect(loginPage);
                        })
                )
                .httpBasic(basic -> basic.disable())
                .build();
    }
}
