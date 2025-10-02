package com.RecipeCode.teamproject.reci.auth.controller;

import com.RecipeCode.teamproject.reci.auth.repository.MemberRepository;
import com.RecipeCode.teamproject.reci.auth.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class MemberRestController {

    private final MemberService memberService;

    @GetMapping("/check-email")
    public Map<String, Boolean> checkEmail(@RequestParam String value) {
        boolean exists = memberService.existsByEmail(value);
        return Collections.singletonMap("exists", exists);
    }

    @GetMapping("/check-handle")
    public Map<String, Boolean> checkHandle(@RequestParam String value) {
        boolean exists = memberService.existsByUserId(value);
        return Collections.singletonMap("exists", exists);
    }
}
