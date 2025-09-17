package com.RecipeCode.teamproject.reci.mypage.controller;

import com.RecipeCode.teamproject.reci.auth.dto.SecurityUserDto;
import com.RecipeCode.teamproject.reci.auth.entity.Member;
import com.RecipeCode.teamproject.reci.feed.recipes.dto.RecipesDto;
import com.RecipeCode.teamproject.reci.mypage.service.MyPageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
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
@Log4j2
public class MyPageController {
    private final MyPageService myPageService;

    @GetMapping("/my-recipes")
    public Slice<RecipesDto> getMyRecipes(
//            @AuthenticationPrincipal SecurityUserDto user,
            @PageableDefault(size = 5, sort = "insertTime",
                            direction = Sort.Direction.DESC) Pageable pageable) {
//        TODO: 테스트유저 : 테스트 후 수정
        String fakeUserEmail = "asdf1234@naver.com";
        // fakeUserEmail -> user.getUsername() 로 변경할 것
        return myPageService.getMyRecipes(fakeUserEmail, pageable);
    }

    @GetMapping("/my-liked")
    public Slice<RecipesDto> getMyLikedRecipes(
//            @AuthenticationPrincipal SecurityUserDto user,
            @PageableDefault(size = 5, sort = "insertTime",
                            direction = Sort.Direction.DESC) Pageable pageable) {
//        TODO: 테스트유저 : 테스트 후 수정
        String fakeUserEmail = "asdf1234@naver.com";
        // fakeUserEmail -> user.getUsername() 로 변경할 것
        return myPageService.getMyLikedRecipes(fakeUserEmail, pageable);
    }

}
