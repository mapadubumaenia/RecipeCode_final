package com.RecipeCode.teamproject.reci.recipes.service;
import com.RecipeCode.teamproject.reci.auth.entity.Member;
import com.RecipeCode.teamproject.reci.recipes.dto.RecipesDto;
import com.RecipeCode.teamproject.reci.recipes.entity.Recipes;
import com.RecipeCode.teamproject.reci.recipes.repository.RecipesRepository;
import jakarta.persistence.EntityManager;
import lombok.extern.log4j.Log4j2;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Log4j2
@SpringBootTest
@Transactional
class RecipesServiceTest {

    @Autowired
    RecipesRepository recipesRepository;
    @Autowired
    RecipesService recipesService;
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

        Recipes recipe1 = Recipes.builder()
                .recipeTitle("첫 번째 레시피")
                .recipeCategory("한식")
                .postStatus("PUBLIC")
                .member(member)
                .build();

        Recipes recipe2 = Recipes.builder()
                .recipeTitle("두 번째 레시피")
                .recipeCategory("양식")
                .postStatus("PUBLIC")
                .member(member)
                .build();

        recipesRepository.save(recipe1);
        recipesRepository.save(recipe2);

//      userEmail을 List로 불러옴
        List<String> followIds = List.of("tester01");
        Page<RecipesDto> feed = recipesService.getFollowFeed(followIds, pageable);

        log.info("피드 결과: {}", feed.getContent());
    }
}