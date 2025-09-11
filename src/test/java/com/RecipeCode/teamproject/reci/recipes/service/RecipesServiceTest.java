package com.RecipeCode.teamproject.reci.recipes.service;

import com.RecipeCode.teamproject.reci.recipes.entity.Recipes;
import com.RecipeCode.teamproject.reci.recipes.repository.RecipesRepository;
import com.RecipeCode.teamproject.reci.recipes.repository.RecipesSummaryView;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@Log4j2
@SpringBootTest
@Transactional
class RecipesServiceTest {

    @Autowired
    RecipesService recipesService;
    @Autowired
    RecipesRepository recipesRepository;

    @Test
    @DisplayName("공개 레시피만 조회된다")
    void getPublicRecipes() {
        Recipes publicRecipe = Recipes.builder()
                .recipeTitle("한 냄비 파스타")
                .recipeIntro("설거지 줄이는 꿀팁")
                .recipeCategory("양식")
                .postStatus("PUBLIC")
                .thumbnailUrl("thumb1.jpg")
                .likeCount(10L)
                .commentCount(2L)
                .build();
        Recipes privateRecipe = Recipes.builder()
                .recipeTitle("비밀 파스타")
                .recipeIntro("나만 아는 꿀팁")
                .recipeCategory("양식")
                .postStatus("PRIVATE")
                .thumbnailUrl("thumb2.jpg")
                .likeCount(1L)
                .commentCount(0L)
                .build();

        recipesRepository.save(publicRecipe);
        recipesRepository.save(privateRecipe);

        // when
        List<RecipesSummaryView> result = recipesService.getPublicRecipes();

        // then
        assertThat(result).hasSize(1); // 공개 레시피만
        assertThat(result.get(0).getRecipeTitle()).isEqualTo("한 냄비 파스타");
        assertThat(result.get(0).getPostStatus()).isEqualTo("PUBLIC");

        log.info("조회 결과 = {}", result);
    }
}