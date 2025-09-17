package com.RecipeCode.teamproject.reci.mypage.controller;

import com.RecipeCode.teamproject.reci.auth.dto.SecurityUserDto;
import com.RecipeCode.teamproject.reci.feed.recipes.dto.RecipesDto;
import com.RecipeCode.teamproject.reci.mypage.service.MyPageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mypage")
@RequiredArgsConstructor
public class MyPageController {
    private final MyPageService myPageService;

    @GetMapping("/my-recipes")
    public Slice<RecipesDto> getMyRecipes(
            @AuthenticationPrincipal SecurityUserDto user,
            @PageableDefault(size = 10, sort = "insertTime",
                            direction = Sort.Direction.DESC) Pageable pageable) {

        return myPageService.getMyRecipes(user.getUsername(), pageable);
    }
}
