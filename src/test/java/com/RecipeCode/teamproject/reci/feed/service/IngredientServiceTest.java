package com.RecipeCode.teamproject.reci.feed.service;

import com.RecipeCode.teamproject.reci.auth.entity.Member;
import com.RecipeCode.teamproject.reci.auth.repository.MemberRepository;
import com.RecipeCode.teamproject.reci.feed.ingredient.dto.IngredientDto;
import com.RecipeCode.teamproject.reci.feed.ingredient.repository.IngredientRepository;
import com.RecipeCode.teamproject.reci.feed.ingredient.service.IngredientService;
import com.RecipeCode.teamproject.reci.feed.recipes.entity.Recipes;
import com.RecipeCode.teamproject.reci.feed.recipes.repository.RecipesRepository;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Log4j2
@SpringBootTest
@Transactional
class IngredientServiceTest {

    @Autowired
    IngredientService ingredientService;
    @Autowired
    IngredientRepository ingredientRepository;
    @Autowired
    RecipesRepository recipesRepository;
    @Autowired
    MemberRepository memberRepository;

    private Recipes testRecipe;
    private Member testMember;

    @BeforeEach
    void setUp() {

        // 유저 저장
        testMember = new Member();
        testMember.setUserEmail("test@test.com");
        testMember.setUserId("testuser");
        testMember.setNickname("tester");
        testMember.setPassword("1234");
        testMember.setProfileStatus("PUBLIC");
        memberRepository.save(testMember);

        // 레시피 저장
        testRecipe = new Recipes();
        testRecipe.setUuid(UUID.randomUUID().toString());
        testRecipe.setRecipeTitle("테스트 레시피");
        testRecipe.setRecipeIntro("간단한 테스트 레시피");
        testRecipe.setRecipeCategory("한식");
        testRecipe.setPostStatus("PUBLIC");
        testRecipe.setCookingTime(30L);
        testRecipe.setMember(testMember);
        testRecipe.setLikeCount(0L);
        testRecipe.setReportCount(0L);
        testRecipe.setCommentCount(0L);
        testRecipe.setViewCount(0L);

        recipesRepository.saveAndFlush(testRecipe);

        log.info("✔ 테스트용 Member, Recipes 셋업 완료");

    }


    @Test
    @DisplayName("Ingredient 저장 및 조회 테스트")
    void testSaveAndGetIngredients() {
        // given
        IngredientDto dto1 = new IngredientDto();
        dto1.setIngredientName("양파");
        dto1.setIngredientAmount("1개");

        IngredientDto dto2 = new IngredientDto();
        dto2.setIngredientName("고추장");
        dto2.setIngredientAmount("2스푼");

        List<IngredientDto> ingredientDtos = Arrays.asList(dto1, dto2);

        // when: 저장
        ingredientService.saveAll(ingredientDtos, testRecipe);

        // then: 조회
        List<IngredientDto> result = ingredientService.getIngredients(testRecipe.getUuid());

        log.info("조회된 재료 리스트: {}", result);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getIngredientName()).isEqualTo("양파");
        assertThat(result.get(1).getIngredientAmount()).isEqualTo("2스푼");
    }
}