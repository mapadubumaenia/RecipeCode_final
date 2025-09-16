package com.RecipeCode.teamproject.reci.recipes.service;
import com.RecipeCode.teamproject.reci.auth.entity.Member;
import com.RecipeCode.teamproject.reci.auth.repository.MemberRepository;
import com.RecipeCode.teamproject.reci.feed.recipes.service.RecipesService;
import com.RecipeCode.teamproject.reci.feed.ingredient.dto.IngredientDto;
import com.RecipeCode.teamproject.reci.feed.recipeTag.entity.RecipeTag;
import com.RecipeCode.teamproject.reci.feed.recipeTag.repository.RecipeTagRepository;

import com.RecipeCode.teamproject.reci.feed.recipes.dto.RecipesDto;
import com.RecipeCode.teamproject.reci.feed.recipes.entity.Recipes;
import com.RecipeCode.teamproject.reci.feed.recipes.repository.RecipesRepository;
import com.RecipeCode.teamproject.reci.tag.repository.TagRepository;
import jakarta.persistence.EntityManager;
import lombok.extern.log4j.Log4j2;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;


@Log4j2
@SpringBootTest
@Transactional
class RecipesServiceTest {

    @Autowired
    MemberRepository memberRepository;
    @Autowired
    RecipesRepository recipesRepository;
    @Autowired
    RecipesService recipesService;
    @Autowired
    TagRepository tagRepository;
    @Autowired
    RecipeTagRepository recipeTagRepository;
//  EntityManager : DB에 엔티티를 저장하고 관리하는 핵심 클래스
    @Autowired
    EntityManager em;


    @Test
    void getFollowFeed() {

        Pageable pageable = PageRequest.of(0, 10);

        Member member = new Member();
        member.setUserEmail("testUser@example.com");
        member.setUserId("tester01");
        member.setNickname("테스트유저");
        member.setPassword("encodedPassword");
        member.setProfileStatus("PUBLIC");

        em.persist(member);

        Recipes recipe1 = new Recipes();
                recipe1.setRecipeTitle("첫 번째 레시피");
                recipe1.setRecipeCategory("한식");
                recipe1.setPostStatus("PUBLIC");
                recipe1.setMember(member);

        Recipes recipe2 = new Recipes();
                recipe2.setRecipeTitle("두 번째 레시피");
                recipe2.setRecipeCategory("양식");
                recipe2.setPostStatus("PUBLIC");
                recipe2.setMember(member);

        recipesRepository.save(recipe1);
        recipesRepository.save(recipe2);

//      userEmail을 List로 불러옴
        List<String> followIds = List.of("tester01");
        Page<RecipesDto> feed = recipesService.getFollowFeed(followIds, pageable);

        log.info("피드 결과: {}", feed.getContent());
    }


//    @Test
//    void testSave() {
//            // 1. 테스트용 멤버 저장
//            Member member = new Member();
//            member.setUserEmail("testUser@example.com");
//            member.setUserId("tester01");
//            member.setNickname("테스트유저");
//            member.setPassword("encodedPassword");
//            member.setProfileStatus("PUBLIC");
//            memberRepository.save(member);
//
//            // 2. DTO 준비
//            IngredientDto ingredientDto = new IngredientDto();
//            ingredientDto.setIngredientName("스파게티면");
//            ingredientDto.setIngredientAmount("200g");
//            ingredientDto.setSortOrder(1L);
//
//            RecipeContentDto recipeContentDto = new RecipeContentDto();
//            recipeContentDto.setStepOrder(10L);
//            recipeContentDto.setStepExplain("끓는 물에 면을 삶는다.");
//
//            RecipesDto recipesDto = new RecipesDto();
//            recipesDto.setRecipeTitle("태그 포함 테스트 레시피");
//            recipesDto.setRecipeCategory("양식");
//            recipesDto.setPostStatus("PUBLIC");
//            recipesDto.setDifficulty("쉬움");
//            recipesDto.setCookingTime(20L);
//            recipesDto.setIngredients(List.of(ingredientDto));
//            recipesDto.setContents(List.of(recipeContentDto));
//            recipesDto.setCommentCount(0L);
//            recipesDto.setLikeCount(0L);
//            recipesDto.setViewCount(0L);
//            recipesDto.setReportCount(0L);
//
//            // 🔥 태그 추가
//            recipesDto.setTags(List.of("파스타", "간단요리", "저녁메뉴"));
//
//            // 3. 서비스 호출
//            String uuid = recipesService.save(recipesDto, member.getUserEmail());
//
//            // 4. 검증
//            assertNotNull(uuid);
//            System.out.println("등록된 레시피 uuid = " + uuid);
//
//            // 5. 태그 검증
//            List<RecipeTag> recipeTags = recipeTagRepository.findByRecipes_Uuid(uuid);
//            assertEquals(3, recipeTags.size()); // 태그 3개 들어갔는지 확인
//            recipeTags.forEach(rt -> System.out.println("저장된 태그 = " + rt.getTag().getTag()));
//    }

}