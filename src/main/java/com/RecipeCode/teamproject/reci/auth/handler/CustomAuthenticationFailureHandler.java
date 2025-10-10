package com.RecipeCode.teamproject.reci.auth.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler  {
    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception)
            throws IOException, ServletException {

        String errorMessage = "로그인에 실패했습니다.";

        if (exception instanceof BadCredentialsException) {
            errorMessage = "비밀번호가 올바르지 않습니다.";
        } else if (exception instanceof UsernameNotFoundException) {
            errorMessage = "존재하지 않는 이메일입니다.";
        } else if (exception instanceof DisabledException) {
            errorMessage = "비활성화된 계정입니다.";
        } else if (exception instanceof LockedException) {
            errorMessage = "잠긴 계정입니다.";
        }

        // ✅ JSP로 직접 전달할 메시지 속성 설정
        request.setAttribute("errors", errorMessage);

        // ✅ /WEB-INF/views/errors.jsp 로 포워드 (리다이렉트 X)
        request.getRequestDispatcher("/WEB-INF/views/errors.jsp").forward(request, response);
    }



}
