package com.RecipeCode.teamproject.reci.auth.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.DefaultSavedRequest;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;

@Component
public class oAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final RequestCache requestCache = new HttpSessionRequestCache();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        boolean isAdmin = authorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

        String redirectUrl = null;

        if (isAdmin) {
            redirectUrl = request.getContextPath() + "/admin";
        } else {
            // 1️⃣ redirect 파라미터 > SavedRequest > 기본값
            String redirectParam = request.getParameter("redirect");
            if (redirectParam != null && !redirectParam.isBlank()) {
                redirectUrl = redirectParam;
            } else {
                DefaultSavedRequest savedRequest =
                        (DefaultSavedRequest) requestCache.getRequest(request, response);
                if (savedRequest != null) {
                    redirectUrl = savedRequest.getRedirectUrl();
                } else {
                    redirectUrl = request.getContextPath() + "/";
                }
            }
        }

        // 안전한 ASCII URL 변환 (한글 포함 redirect 방지)
        try {
            URI uri = new URI(redirectUrl);
            response.sendRedirect(uri.toASCIIString());
        } catch (URISyntaxException e) {
            response.sendRedirect(request.getContextPath() + "/");
        }
    }
}
