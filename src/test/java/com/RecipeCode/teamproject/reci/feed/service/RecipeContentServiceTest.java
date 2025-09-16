package com.RecipeCode.teamproject.reci.feed.service;

import com.RecipeCode.teamproject.reci.auth.entity.Member;
import com.RecipeCode.teamproject.reci.auth.repository.MemberRepository;
import com.RecipeCode.teamproject.reci.feed.recipecontent.dto.RecipeContentDto;
import com.RecipeCode.teamproject.reci.feed.recipecontent.entity.RecipeContent;
import com.RecipeCode.teamproject.reci.feed.recipecontent.repository.RecipeContentRepository;
import com.RecipeCode.teamproject.reci.feed.recipecontent.service.RecipeContentService;
import com.RecipeCode.teamproject.reci.feed.recipes.entity.Recipes;
import com.RecipeCode.teamproject.reci.feed.recipes.repository.RecipesRepository;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Log4j2
@SpringBootTest
@Transactional
class RecipeContentServiceTest {

    @Autowired
    private RecipeContentService recipeContentService;
    @Autowired
    private RecipeContentRepository recipeContentRepository;
    @Autowired
    private RecipesRepository recipesRepository;
    @Autowired
    private MemberRepository memberRepository;

    private Recipes testRecipe;
    private Member testMember;


    @BeforeEach
    void setUp() {

        // ✅ 가짜 HttpServletRequest 세팅 (URL 생성용)
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setScheme("http");
        request.setServerName("localhost");
        request.setServerPort(8080);
        request.setContextPath("/api"); // 컨텍스트 패스 있으면 추가
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

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
        testRecipe.setRecipeIntro("테스트 설명");
        testRecipe.setRecipeCategory("한식");
        testRecipe.setPostStatus("PUBLIC");
        testRecipe.setCookingTime(15L);
        testRecipe.setMember(testMember);
        testRecipe.setLikeCount(0L);
        testRecipe.setReportCount(0L);
        testRecipe.setCommentCount(0L);
        testRecipe.setViewCount(0L);

        recipesRepository.saveAndFlush(testRecipe);

        log.info("✔ 테스트용 Member, Recipes 셋업 완료");
    }


    @Test
    @DisplayName("RecipeContent 저장 및 조회 테스트")
    void testSaveAndGetRecipeContents() {
        // given
        RecipeContentDto dto1 = new RecipeContentDto();
        dto1.setStepExplain("양파를 썬다");
        dto1.setStepOrder(10L);

        RecipeContentDto dto2 = new RecipeContentDto();
        dto2.setStepExplain("팬에 볶는다");
        dto2.setStepOrder(20L);

        List<RecipeContentDto> contentDtos = Arrays.asList(dto1, dto2);

        // 이미지 더미 데이터 (byte[])
        List<byte[]> images = Arrays.asList(
                "이미지1".getBytes(),
                "이미지2".getBytes()
        );

        // when
        recipeContentService.saveRecipeContent(contentDtos, images, testRecipe);

        // then
        List<RecipeContentDto> result = recipeContentService.getContents(testRecipe.getUuid());

        log.info("조회된 콘텐츠 리스트: {}", result);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getStepExplain()).isEqualTo("양파를 썬다");
        assertThat(result.get(1).getStepExplain()).isEqualTo("팬에 볶는다");

        // ✅ URL 생성 확인
        List<RecipeContent> savedContents = recipeContentRepository.findAll();
        assertThat(savedContents.get(0).getRecipeImageUrl()).isNotNull();
        assertThat(savedContents.get(1).getRecipeImageUrl()).isNotNull();
        log.info("생성된 다운로드 URL: {}", savedContents);

    }

}