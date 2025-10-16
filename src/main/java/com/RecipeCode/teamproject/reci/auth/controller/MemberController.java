package com.RecipeCode.teamproject.reci.auth.controller;

import com.RecipeCode.teamproject.reci.admin.entity.Admin;
import com.RecipeCode.teamproject.reci.admin.repository.AdminRepository;
import com.RecipeCode.teamproject.reci.auth.dto.MemberDto;
import com.RecipeCode.teamproject.reci.auth.entity.Member;
import com.RecipeCode.teamproject.reci.auth.repository.MemberRepository;
import com.RecipeCode.teamproject.reci.auth.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.Principal;

@Log4j2
@Controller
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;
    private final MemberRepository memberRepository;
    private final AdminRepository adminRepository;


    //    로그인 페이지
    @GetMapping("/auth/login")
    public String login() {
        return "auth/login";
    }


    // 비밀번호 찾기
    @GetMapping("/auth/findPassword")
    public String findPassword() {
        return "auth/findPassword";
    }

    //    회원가입 페이지 이동
    @GetMapping("/auth/register")
    public String register() {
        return "auth/register";
    }

    //    회원가입
    @PostMapping("/auth/register/addition")
    public String register(Model model, @ModelAttribute MemberDto memberDto) {
        memberService.save(memberDto);
        model.addAttribute("msg","회원 가입을 성공했습니다");
        return "auth/login";
    }


    @GetMapping("/member/{userId}/profile-image")
    public ResponseEntity<byte[]> getProfileImage(@PathVariable String userId) {
        Member member = memberRepository.findByUserId(userId)
                .orElseGet(() -> {
                    // 🔹 2. 회원이 없으면 관리자 테이블에서 조회
                    Admin admin = adminRepository.findByAdminId(userId)
                            .orElseThrow(() -> new RuntimeException("회원 없음"));
                    // Admin → Member 변환 없이 바로 임시 Member 객체 생성
                    Member m = new Member();
                    m.setUserId(admin.getAdminId());
                    m.setProfileImage(admin.getProfileImage());
                    m.setProfileImageUrl(admin.getProfileImageUrl());
                    return m;
                });

        byte[] image = member.getProfileImage();
        MediaType mediaType = MediaType.IMAGE_JPEG;


        if (image == null || image.length == 0) {
            try {
                ClassPathResource defaultImg = new ClassPathResource("static/images/default_profile.jpg");
                image = defaultImg.getInputStream().readAllBytes();
                mediaType = MediaType.IMAGE_JPEG; // 기본 이미지가 jpg라면
            } catch (IOException e) {
                throw new RuntimeException("기본 프로필 이미지를 불러오는 데 실패했습니다.", e);
            }
        } else {
            if (member.getProfileImageUrl() != null) {
                String url = member.getProfileImageUrl().toLowerCase();
                if (url.endsWith(".png")) {
                    mediaType = MediaType.IMAGE_PNG;
                } else if (url.endsWith(".gif")) {
                    mediaType = MediaType.IMAGE_GIF;
                } else {
                mediaType = MediaType.IMAGE_JPEG;
                }
            }
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .body(image);
    }

    @GetMapping("/member/checkPasswordExist")
    @ResponseBody
    public boolean checkPasswordExist(Principal principal) {
        String userEmail = principal.getName();
        Member member = memberService.getByUserEmail(userEmail);
        return member.getPassword() != null && !member.getPassword().isEmpty();
    }

    @PostMapping("/member/verifyPassword")
    @ResponseBody
    public boolean verifyPassword(@RequestParam String password, Principal principal) {
        String username = principal.getName();
        return memberService.checkPassword(username, password);
    }

}
