package com.RecipeCode.teamproject.reci.mypage.service;

import com.RecipeCode.teamproject.common.ErrorMsg;
import com.RecipeCode.teamproject.common.RecipeMapStruct;
import com.RecipeCode.teamproject.reci.auth.repository.MemberRepository;
import com.RecipeCode.teamproject.reci.feed.recipes.dto.RecipesDto;
import com.RecipeCode.teamproject.reci.feed.recipes.entity.Recipes;
import com.RecipeCode.teamproject.reci.feed.recipes.repository.RecipesRepository;
import com.RecipeCode.teamproject.reci.feed.recipeslikes.repository.RecipesLikesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MyPageService {
    private final RecipeMapStruct recipeMapStruct;
    private final RecipesRepository recipesRepository;
    private final RecipesLikesRepository recipesLikesRepository;
    private final MemberRepository memberRepository;
    private final ErrorMsg errorMsg;

    // Profile 페이지 내 피드 조회
    public Slice<RecipesDto> getMyRecipes(String userEmail,
                                          Pageable pageable) {
        Slice<Recipes> slice = recipesRepository.findByUserEmail(userEmail, pageable);
        return slice.map(s->recipeMapStruct.toRecipeDto(s));
    }

    // Profile 페이지 내가 좋아요 한 피드 조회
    public Slice<RecipesDto> getMyLikedRecipes(String userEmail,
                                               Pageable pageable) {
        Slice<Recipes> slice = recipesLikesRepository.findByLikedRecipes(userEmail, pageable);
        List<RecipesDto> dtos = slice.getContent()
                .stream().map(recipes->recipeMapStruct.toRecipeDto(recipes))
                .collect(Collectors.toList());
        for (RecipesDto dto : dtos) {
            dto.setLiked(true);
        }

        return new SliceImpl<>(dtos, pageable, slice.hasNext());
    }
}
