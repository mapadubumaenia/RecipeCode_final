package com.RecipeCode.teamproject.reci.feed.recipes.service;

import com.RecipeCode.teamproject.reci.auth.entity.Member;
import com.RecipeCode.teamproject.reci.auth.repository.MemberRepository;
import com.RecipeCode.teamproject.reci.feed.ingredient.dto.IngredientDto;
import com.RecipeCode.teamproject.reci.feed.recipecontent.dto.RecipeContentDto;
import com.RecipeCode.teamproject.reci.feed.recipes.dto.RecipesDto;
import com.RecipeCode.teamproject.reci.feed.recipes.entity.Recipes;
import com.RecipeCode.teamproject.reci.feed.recipes.repository.RecipesRepository;
import com.RecipeCode.teamproject.reci.tag.dto.TagDto;
import com.RecipeCode.teamproject.reci.tag.repository.TagRepository;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@Log4j2
class RecipesServiceTest {

    @Autowired
    RecipesService recipesService;
    @Autowired
    RecipesRepository recipesRepository;
    @Autowired
    TagRepository tagRepository;
    @Autowired
    MemberRepository memberRepository;

    @Test
    void softDeleteRecipe() {
        // given
        Member member = new Member();
        member.setUserEmail("img@test.com");
        member.setUserId("이미지유저");
        member.setNickname("tester");
        member.setPassword("123456");
        member.setProfileStatus("PUBLIC");
        memberRepository.save(member);

        RecipesDto dto = new RecipesDto(
                "이미지 레시피4", "이미지 설명", "한식",
                "PUBLIC", "쉬움", 20L,
                null, null, null,
                "IMAGE", null, null
        );


        List<IngredientDto> ingredients = List.of(
                new IngredientDto(null, "양파", "1개", null)
        );
        List<RecipeContentDto> contents = List.of(
                new RecipeContentDto("썬다", 10L, null)
        );
        List<TagDto> tags = List.of(new TagDto(null, "한식"));

        List<byte[]> images = List.of("step-img".getBytes(StandardCharsets.UTF_8));
        byte[] thumbnail = "thumb".getBytes(StandardCharsets.UTF_8);

        // when
        String uuid = recipesService.createRecipe(dto, ingredients, contents, images, tags, thumbnail, null, member.getUserEmail());



        // soft delete 실행
        recipesService.softDeleteRecipe(uuid);
        recipesRepository.findById(uuid);



    }

    @Test
    void getRecipeDetails() {
        // given
        String existingUuid = "aacb56cf-5513-4d46-9ddc-c4810fb5862b";

        // when
        RecipesDto result = recipesService.getRecipeDetails(existingUuid);

        // then
        log.info("조회 결과: {}", result);
    }
    @Test
    void getRecipeDetails_없는레시피조회() {
        // given
        String invalidUuid = "bbcb56cf-5553-4d46-9ddc-c4810fb4762b";

        // when & then
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> recipesService.getRecipeDetails(invalidUuid));

        log.info("예외 메시지: {}", ex.getMessage());
    }
}
