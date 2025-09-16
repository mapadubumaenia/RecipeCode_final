package com.RecipeCode.teamproject.reci.mypage.controller;

import com.RecipeCode.teamproject.reci.mypage.service.MyPageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class MyPageViewController {
    private final MyPageService myPageService;


}
