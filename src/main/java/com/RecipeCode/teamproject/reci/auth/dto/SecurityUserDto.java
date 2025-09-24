package com.RecipeCode.teamproject.reci.auth.dto;


import com.RecipeCode.teamproject.common.ErrorMsg;
import com.RecipeCode.teamproject.reci.auth.entity.Member;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;


@Getter
@Setter
public class SecurityUserDto implements UserDetails, OAuth2User {
    private final Member member;
    private final Map<String, Object> attributes;
    private final Collection<? extends GrantedAuthority> authorities;

    public SecurityUserDto(Member member, Collection<? extends GrantedAuthority> authorities, Map<String, Object> attributes) {
        this.member = member;
        this.attributes = attributes != null ? attributes : Collections.emptyMap();
        this.authorities = authorities != null ? authorities : Collections.emptyList();
    }
    //  공통 사용 getter
    public String getNickname() { return member.getNickname(); }
    public String getUserEmail() { return member.getUserEmail(); }
    public String getUserId() { return member.getUserId(); }

    // 홈페이지 로그인
    @Override
    public String getUsername() { return member.getUserEmail(); }

    @Override
    public String getPassword() {
        return Optional.ofNullable(member.getPassword()).orElse("");
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return !"Y".equals(member.getDeleted()); }

    //소셜 로그인용
    @Override
    public Map<String, Object> getAttributes() { return attributes; }

    @Override
    public String getName() {
        // OAuth2User 기본 식별자대신 우리 서비스 기준으로 이메일 사용
        return member.getUserEmail();
    }

}
