package com.RecipeCode.teamproject.reci.mypage.controller;

import com.RecipeCode.teamproject.reci.mypage.service.MyPageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/mypage")
public class MyPageViewController {
    private final MyPageService myPageService;

    @GetMapping("/my-recipes")
    public String myPageView(){
        return "profile/mypage_all";
    }
}
