package com.RecipeCode.teamproject.reci.auth.service;

import com.RecipeCode.teamproject.common.ErrorMsg;
import com.RecipeCode.teamproject.reci.auth.dto.SecurityUserDto;
import com.RecipeCode.teamproject.reci.auth.entity.Member;
import com.RecipeCode.teamproject.reci.auth.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
//    DB Member 레포지토리 DI
    private final MemberRepository memberRepository;
    private final ErrorMsg errorMsg;

//    함수 재정의 : 자동 기능 : alt + insert
    @Override
    public UserDetails loadUserByUsername(String username) {
        Member member
                = memberRepository.findById(username)
                    .orElseThrow(() -> new RuntimeException(errorMsg.getMessage("errors.not.found")));

//
//        생성자 : (email, password)
        return new SecurityUserDto(member.getUserEmail(),
                member.getPassword()
        );

    }
}
