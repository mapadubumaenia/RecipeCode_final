package com.RecipeCode.teamproject.reci.auth.service;

import com.RecipeCode.teamproject.common.ErrorMsg;
import com.RecipeCode.teamproject.reci.admin.entity.Admin;
import com.RecipeCode.teamproject.reci.admin.repository.AdminRepository;
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
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
//    DB Member 레포지토리 DI
    private final AdminRepository adminRepository;
    private final MemberRepository memberRepository;
    private final ErrorMsg errorMsg;


//    함수 재정의 : 자동 기능 : alt + insert
    @Override
    public UserDetails loadUserByUsername(String username) {
        // 1) 일반유저 조회
        Set<GrantedAuthority> authorities = new HashSet<>();

        Optional<Member> memberOt = memberRepository.findByUserEmail(username);
        if (memberOt.isPresent()) {
            Member member = memberOt.get();
            authorities.add(new SimpleGrantedAuthority(member.getRole()));

            return new SecurityUserDto(
                    member,
                    authorities,
                    null
            );
        }

        // 2) 관리자 조회
        Optional<Admin> adminOpt = adminRepository.findByAdminEmail(username);
        if (adminOpt.isPresent()) {
            Admin admin = adminOpt.get();
            authorities.add(new SimpleGrantedAuthority(admin.getRole()));

            Member adminMember=Member.builder()
                    .userEmail(admin.getAdminEmail())
                    .userId(admin.getAdminId())
                    .password(admin.getPassword())
                    .nickname(admin.getNickname())
                    .profileImage(admin.getProfileImage())
                    .profileImageUrl(admin.getProfileImageUrl())
                    .role(admin.getRole())
                    .userBlog(admin.getAdminBlog())
                    .userInsta(admin.getAdminInsta())
                    .userIntroduce(admin.getAdminIntroduce())
                    .userLocation(admin.getAdminLocation())
                    .userWebsite(admin.getAdminWebsite())
                    .userYoutube(admin.getAdminYoutube())
                    .profileStatus(admin.getProfileStatus())
                    .userInterestTag(admin.getAdminInterestTag())
                    .build();

            return new SecurityUserDto(
                    adminMember,
                    authorities,
                    null
            );
        }
        throw new RuntimeException(errorMsg.getMessage("errors.not.found"));
    }
}
