package com.RecipeCode.teamproject.reci.function.follow.service;

import com.RecipeCode.teamproject.common.ErrorMsg;
import com.RecipeCode.teamproject.common.RecipeMapStruct;
import com.RecipeCode.teamproject.reci.auth.repository.MemberRepository;
import com.RecipeCode.teamproject.reci.feed.recipes.dto.RecipesDto;
import com.RecipeCode.teamproject.reci.feed.recipes.repository.RecipesRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProfileFeedService {

    private final RecipesRepository recipesRepository;
    private final RecipeMapStruct  recipeMapStruct;
    private final MemberRepository memberRepository;
    private final ErrorMsg errorMsg;

    // 특정 유저의 레시피 목록 조회 (무한 스크롤용)
    @Transactional
    public Slice<RecipesDto> getUserRecipes(String userId, Pageable pageable) {
        // 1) userId → userEmail 변환
        String userEmail = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException(errorMsg.getMessage("errors.not.found")))
                .getUserEmail();

        // 2) userEmail 기반 레시피 조회
        return recipesRepository.findByUserEmail(userEmail, pageable)
                .map(recipe -> recipeMapStruct.toRecipeDto(recipe));
    }

    @Transactional
    public Slice<RecipesDto> getUserRecipesByEmail(String userEmail, Pageable pageable) {
        return recipesRepository.findByUserEmail(userEmail, pageable)
                .map(recipeMapStruct::toRecipeDto);
    }
}
