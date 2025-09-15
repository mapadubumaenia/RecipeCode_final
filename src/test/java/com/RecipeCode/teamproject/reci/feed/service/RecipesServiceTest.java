package com.RecipeCode.teamproject.reci.feed.service;

import com.RecipeCode.teamproject.reci.auth.entity.Member;
import com.RecipeCode.teamproject.reci.auth.repository.MemberRepository;
import com.RecipeCode.teamproject.reci.feed.ingredient.dto.IngredientDto;
import com.RecipeCode.teamproject.reci.feed.ingredient.entity.Ingredient;
import com.RecipeCode.teamproject.reci.feed.ingredient.repository.IngredientRepository;
import com.RecipeCode.teamproject.reci.feed.ingredient.service.IngredientService;
import com.RecipeCode.teamproject.reci.feed.recipeTag.entity.RecipeTag;
import com.RecipeCode.teamproject.reci.feed.recipeTag.repository.RecipeTagRepository;
import com.RecipeCode.teamproject.reci.feed.recipeTag.service.RecipeTagService;
import com.RecipeCode.teamproject.reci.feed.recipecontent.dto.RecipeContentDto;
import com.RecipeCode.teamproject.reci.feed.recipecontent.entity.RecipeContent;
import com.RecipeCode.teamproject.reci.feed.recipecontent.repository.RecipeContentRepository;
import com.RecipeCode.teamproject.reci.feed.recipecontent.service.RecipeContentService;
import com.RecipeCode.teamproject.reci.feed.recipes.dto.RecipesDto;
import com.RecipeCode.teamproject.reci.feed.recipes.entity.Recipes;
import com.RecipeCode.teamproject.reci.feed.recipes.repository.RecipesRepository;
import com.RecipeCode.teamproject.reci.feed.recipes.service.RecipesService;
import com.RecipeCode.teamproject.reci.tag.dto.TagDto;
import com.RecipeCode.teamproject.reci.tag.repository.TagRepository;
import com.RecipeCode.teamproject.reci.tag.service.TagService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Log4j2
@SpringBootTest
@Transactional
class RecipesServiceTest {

    @Autowired
    RecipesService recipeService;

    @Autowired
    RecipesRepository recipeRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    TagRepository tagRepository;

    @Autowired
    RecipeTagService recipeTagService;
    @Autowired
    RecipeTagRepository recipeTagRepository;

    @PersistenceContext
    private EntityManager em; // 👉 JPA 영속성 컨텍스트 제어용

    @Autowired
    RecipeContentService recipeContentService;
    @Autowired
    RecipeContentRepository recipeContentRepository;

    @Autowired
    IngredientService ingredientService;
    @Autowired
    IngredientRepository ingredientRepository;

    @Autowired
    TagService tagService;

    @Test
    void 레시피_IMAGE_저장_검증() {
        // given
        Member member = new Member();
        member.setUserEmail("img@test.com");
        member.setUserId("이미지유저");
        member.setNickname("tester");
        member.setPassword("123456");
        member.setProfileStatus("PUBLIC");
        memberRepository.save(member);

        RecipesDto dto = new RecipesDto(
                "이미지 레시피", "이미지 설명", "한식",
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
        String uuid = recipeService.createRecipe(dto, ingredients, contents, images, tags, thumbnail, null, member);
        em.flush();
        em.clear();

        // then
        Recipes saved = recipeRepository.findById(uuid).orElseThrow();
        assertThat(saved.getThumbnail()).isNotNull();
        assertThat(saved.getRecipeType()).isEqualTo("IMAGE");
        assertThat(ingredientRepository.findByRecipesUuidAndDeletedFalseOrderBySortOrderAsc(uuid)).hasSize(1);
        assertThat(recipeContentRepository.findByRecipesUuidOrderByStepOrderAsc(uuid)).hasSize(1);
        log.info("저장된 정보 조회:{}",saved.toString());
    }

    @Test
    void 레시피_VIDEO_저장_검증() {
        // given
        Member member = new Member();
        member.setUserEmail("img@test.com");
        member.setUserId("이미지유저");
        member.setNickname("tester");
        member.setPassword("123456");
        member.setProfileStatus("PUBLIC");
        memberRepository.save(member);

        RecipesDto dto = new RecipesDto(
                "영상 레시피", "영상 설명", "한식",
                "PUBLIC", "보통", 15L,
                null, null, null,
                "VIDEO", "http://youtube.com/test", "맛있게 끓이는 법"
        );

        List<IngredientDto> ingredients = List.of(
                new IngredientDto(null, "고추장", "2스푼", null)
        );
        List<RecipeContentDto> contents = List.of(
                new RecipeContentDto("끓인다", 10L, null)
        );
        List<TagDto> tags = List.of(new TagDto(null, "영상"));

        // VIDEO 타입이므로 썸네일은 null
        List<byte[]> images = List.of();
        byte[] thumbnail = null;

        // when
        String uuid = recipeService.createRecipe(dto, ingredients, contents, images, tags, thumbnail, null, member);
        em.flush();
        em.clear();

        // then
        Recipes saved = recipeRepository.findById(uuid).orElseThrow();
        assertThat(saved.getThumbnail()).isNull(); // 영상은 thumbnail null
        assertThat(saved.getVideoUrl()).isEqualTo("http://youtube.com/test");
        assertThat(saved.getVideoText()).isEqualTo("맛있게 끓이는 법");
        assertThat(saved.getRecipeType()).isEqualTo("VIDEO");

        log.info("저장된 정보 조회:{}",saved.toString());
    }


    @Test
    void 레시피_IMAGE_수정_검증() {
        // given: 최초 레시피 등록
        Member member = new Member();
        member.setUserEmail("edit@test.com");
        member.setUserId("수정유저");
        member.setNickname("tester");
        member.setPassword("123456");
        member.setProfileStatus("PUBLIC");
        memberRepository.save(member);

        RecipesDto dto = new RecipesDto(
                "김치찌개", "얼큰한 김치찌개", "한식",
                "PUBLIC", "쉬움", 30L,
                null, null, null,
                "IMAGE", null, null
        );

        List<IngredientDto> ingredients = List.of(
                new IngredientDto(null, "김치", "200g", null),
                new IngredientDto(null, "돼지고기", "150g", null)
        );
        List<RecipeContentDto> contents = List.of(
                new RecipeContentDto("김치를 볶는다", 10L, null),
                new RecipeContentDto("물을 붓는다", 20L, null)
        );
        List<TagDto> tags = List.of(
                new TagDto(null, "찌개"),
                new TagDto(null, "매운맛")
        );
        List<byte[]> images = List.of("img-step1".getBytes(), "img-step2".getBytes());
        byte[] thumbnail = "thumb".getBytes();

        String uuid = recipeService.createRecipe(dto, ingredients, contents, images, tags, thumbnail, null, member);
        em.flush();
        em.clear();

        // when: 수정 데이터 준비
        RecipesDto updateDto = new RecipesDto(
                "된장찌개", "구수한 된장찌개", "한식",
                "PRIVATE", "보통", 40L,
                null, null, null,
                "IMAGE", null, null
        );

        List<IngredientDto> newIngredients = List.of(
                new IngredientDto(null, "된장", "100g", null),
                new IngredientDto(null, "두부", "200g", null)
        );
        List<RecipeContentDto> newContents = List.of(
                new RecipeContentDto("된장을 푼다", 10L, null),
                new RecipeContentDto("두부를 넣는다", 20L, null)
        );
        List<TagDto> newTags = List.of(new TagDto(null, "국물요리"));
        List<byte[]> newImages = List.of("new-step-img".getBytes());

        recipeService.updateRecipe(uuid, updateDto, newIngredients, newContents, newImages, newTags);
        em.flush();
        em.clear();


        // then: 수정 결과 검증
        Recipes updated = recipeRepository.findById(uuid).orElseThrow();

        assertThat(updated.getRecipeTitle()).isEqualTo("된장찌개");
        assertThat(updated.getDifficulty()).isEqualTo("보통");
        assertThat(updated.getPostStatus()).isEqualTo("PRIVATE");
        assertThat(updated.getThumbnail()).isNotNull(); // 썸네일도 교체됐는지 확인

        List<Ingredient> savedIngredients = ingredientRepository.findByRecipesUuidAndDeletedFalseOrderBySortOrderAsc(uuid);
        List<RecipeContent> savedContents = recipeContentRepository.findByRecipesUuidOrderByStepOrderAsc(uuid);
        List<RecipeTag> savedTags = recipeTagRepository.findByRecipesUuid(uuid);

        log.info("📌 저장된 재료 목록: {}", savedIngredients);
        log.info("📌 저장된 조리 단계: {}", savedContents);
        log.info("📌 저장된 태그들: {}", savedTags);
    }

}