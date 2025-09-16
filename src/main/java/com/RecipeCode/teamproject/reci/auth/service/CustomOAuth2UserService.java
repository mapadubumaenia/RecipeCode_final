package com.RecipeCode.teamproject.reci.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException{
        // 기본 서비스
        OAuth2User oAuth2User = new DefaultOAuth2UserService().loadUser(userRequest);
        // provider 정보
        String registrationId = userRequest.getClientRegistration().getRegistrationId(); // google, kakao
        // attributes (구글/카카오 사용자 정보)
        var attributes = oAuth2User.getAttributes();

        return oAuth2User;
    }
}
