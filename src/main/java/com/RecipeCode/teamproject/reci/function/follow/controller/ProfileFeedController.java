package com.RecipeCode.teamproject.reci.function.follow.controller;

import com.RecipeCode.teamproject.reci.feed.recipes.dto.RecipesDto;
import com.RecipeCode.teamproject.reci.function.follow.service.ProfileFeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/profile")
public class ProfileFeedController {
    private final ProfileFeedService profileFeedService;

    // 특정 유저의 작성한 레시피 목록 (무한 스크롤용 API)
    @GetMapping("/{userEmail}/recipes")
    public Slice<RecipesDto> getUserRecipes(@PathVariable String userEmail,
                                            @PageableDefault(size = 5) Pageable pageable) {
        return profileFeedService.getUserRecipes(userEmail, pageable);
    }
}
