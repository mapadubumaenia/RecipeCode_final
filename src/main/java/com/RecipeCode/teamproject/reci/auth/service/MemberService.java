package com.RecipeCode.teamproject.reci.auth.service;

import com.RecipeCode.teamproject.common.ErrorMsg;
import com.RecipeCode.teamproject.common.MapStruct;
import com.RecipeCode.teamproject.reci.admin.repository.AdminRepository;
import com.RecipeCode.teamproject.reci.auth.dto.MemberDto;
import com.RecipeCode.teamproject.reci.auth.entity.Member;
import com.RecipeCode.teamproject.reci.auth.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final AdminRepository adminRepository;
    private final MemberRepository memberRepository;
    private final MapStruct mapStruct;
    private final PasswordEncoder passwordEncoder;
    private final ErrorMsg errorMsg;

//   회원가입
    public void save(MemberDto memberDto) {
//        중복이메일 검사
        if (memberRepository.existsById(memberDto.getUserEmail())) {
            throw new RuntimeException((errorMsg.getMessage("errors.register")));
        }

//        비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(memberDto.getPassword());
        Member member = mapStruct.toEntity(memberDto);
        member.setPassword(encodedPassword);
        if (member.getProfileStatus() == null || member.getProfileStatus().isBlank()) {
            member.setProfileStatus("PUBLIC");

            memberRepository.save(member);
        }
    }
}
