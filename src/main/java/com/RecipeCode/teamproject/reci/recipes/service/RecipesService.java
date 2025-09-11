package com.RecipeCode.teamproject.reci.recipes.service;

import com.RecipeCode.teamproject.reci.recipes.repository.RecipesRepository;
import com.RecipeCode.teamproject.reci.recipes.repository.RecipesSummaryView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecipesService {

    private final RecipesRepository recipesRepository;

//    메인 화면용 (공개 레시피만)
    public List<RecipesSummaryView> getPublicRecipes() {
        return recipesRepository.findByPostStatus("PUBLIC");
    }

//    마이 페이지 (내 모든 레시피)
    public List<RecipesSummaryView> getMyRecipes(String userEmail) {
        return recipesRepository.findByUserEmail(userEmail);
    }

//    마이 페이지 (내 공개 레시피 or 비공개만)
    public List<RecipesSummaryView> getMyRecipesByPostStatus(String userEmail, String postStatus) {
        return recipesRepository.findByUserEmailAndPostStatus(userEmail, postStatus);
    }
}
