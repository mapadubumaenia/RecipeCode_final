package com.RecipeCode.teamproject.reci.auth.controller;

import com.RecipeCode.teamproject.reci.auth.dto.MemberDto;
import com.RecipeCode.teamproject.reci.auth.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Log4j2
@Controller
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    //    로그인 함수
    @GetMapping("/auth/login")
    public String login() {
        return "sample";
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
        return "auth/register";
    }
}
