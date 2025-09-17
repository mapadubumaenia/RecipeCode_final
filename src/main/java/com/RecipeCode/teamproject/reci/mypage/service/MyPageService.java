package com.RecipeCode.teamproject.reci.mypage.service;

import com.RecipeCode.teamproject.common.ErrorMsg;
import com.RecipeCode.teamproject.common.RecipeMapStruct;
import com.RecipeCode.teamproject.reci.auth.repository.MemberRepository;
import com.RecipeCode.teamproject.reci.feed.recipes.dto.RecipesDto;
import com.RecipeCode.teamproject.reci.feed.recipes.entity.Recipes;
import com.RecipeCode.teamproject.reci.feed.recipes.repository.RecipesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MyPageService {
    private final RecipeMapStruct recipeMapStruct;
    private final RecipesRepository recipesRepository;
    private final MemberRepository memberRepository;
    private final ErrorMsg errorMsg;

    public Slice<RecipesDto> getMyRecipes(String userEmail,
                                          Pageable pageable) {
        Slice<Recipes> slice = recipesRepository.findByUserEmail(userEmail, pageable);
        return slice.map(s->recipeMapStruct.toRecipeDto(s));
    }
}
