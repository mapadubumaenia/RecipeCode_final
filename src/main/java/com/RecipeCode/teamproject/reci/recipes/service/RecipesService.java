package com.RecipeCode.teamproject.reci.recipes.service;

import com.RecipeCode.teamproject.common.MapStruct;
import com.RecipeCode.teamproject.reci.recipes.dto.RecipesDto;
import com.RecipeCode.teamproject.reci.recipes.entity.Recipes;
import com.RecipeCode.teamproject.reci.recipes.repository.RecipesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecipesService {

    private final RecipesRepository recipesRepository;
    private final MapStruct mapStruct;

// 팔로우 레시피 내 팔로우 피드보기 (최신순)
    public Page<RecipesDto> getFollowFeed(List<String> followIds, Pageable pageable) {
//        공개 레시피
        String status = "PUBLIC";

        Page<Recipes> recipesPage = recipesRepository
                .findByMember_UserIdInAndPostStatusOrderByInsertTimeDesc(
                    followIds, status, pageable);

        return recipesPage.map(recipesDto -> mapStruct.toDto(recipesDto));
    }

}
