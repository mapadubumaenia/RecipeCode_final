package com.RecipeCode.teamproject.reci.auth.controller;

import com.RecipeCode.teamproject.reci.auth.dto.MemberDto;
import com.RecipeCode.teamproject.reci.auth.entity.Member;
import com.RecipeCode.teamproject.reci.auth.repository.MemberRepository;
import com.RecipeCode.teamproject.reci.auth.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Log4j2
@Controller
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;
    private final MemberRepository memberRepository;

    //    로그인 함수
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
                .orElseThrow(() -> new RuntimeException("회원 없음"));

        byte[] image = member.getProfileImage();
        if (image == null) {
            return ResponseEntity.notFound().build();
        }

        MediaType mediaType = MediaType.IMAGE_JPEG; // 기본값
        if (member.getProfileImageUrl() != null && member.getProfileImageUrl().endsWith(".png")) {
            mediaType = MediaType.IMAGE_PNG;
        }

        // MIME 타입 추측 (JPG 기준, PNG면 IMAGE_PNG)
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(image);
    }

}
