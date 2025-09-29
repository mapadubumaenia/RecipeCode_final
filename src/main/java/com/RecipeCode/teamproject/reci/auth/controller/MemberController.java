package com.RecipeCode.teamproject.reci.auth.controller;

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
        MediaType mediaType = MediaType.IMAGE_JPEG;

        if (image == null || image.length == 0) {
            try {
                ClassPathResource defaultImg = new ClassPathResource("static/images/default_profile.jpg");
                log.info("리소스 존재 여부: {}", defaultImg.exists());
                log.info("리소스 경로: {}", defaultImg.getPath());
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

}
