package com.RecipeCode.teamproject.reci.feed.recipes.service;

import com.RecipeCode.teamproject.common.ErrorMsg;
import com.RecipeCode.teamproject.common.RecipeMapStruct;
import com.RecipeCode.teamproject.reci.auth.entity.Member;

import com.RecipeCode.teamproject.reci.feed.ingredient.dto.IngredientDto;
import com.RecipeCode.teamproject.reci.feed.ingredient.repository.IngredientRepository;
import com.RecipeCode.teamproject.reci.feed.ingredient.service.IngredientService;

import com.RecipeCode.teamproject.reci.feed.recipeTag.repository.RecipeTagRepository;
import com.RecipeCode.teamproject.reci.feed.recipeTag.service.RecipeTagService;
import com.RecipeCode.teamproject.reci.feed.recipecontent.dto.RecipeContentDto;
import com.RecipeCode.teamproject.reci.feed.recipecontent.repository.RecipeContentRepository;
import com.RecipeCode.teamproject.reci.feed.recipecontent.service.RecipeContentService;
import com.RecipeCode.teamproject.reci.feed.recipes.dto.RecipesDto;
import com.RecipeCode.teamproject.reci.feed.recipes.entity.Recipes;
import com.RecipeCode.teamproject.reci.feed.recipes.repository.RecipesRepository;
import com.RecipeCode.teamproject.reci.tag.dto.TagDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RecipesService {

    private final RecipesRepository recipesRepository;
    private final IngredientService ingredientService;
    private final RecipeContentService recipeContentService;
    private final RecipeTagService recipeTagService;
    private final IngredientRepository ingredientRepository;
    private final RecipeContentRepository recipeContentRepository;
    private final RecipeTagRepository recipeTagRepository;
    private final RecipeMapStruct recipeMapStruct;
    private final ErrorMsg errorMsg;
    @PersistenceContext
    private EntityManager em; // 👉 JPA 영속성 컨텍스트 제어용

    // 내 팔로우 페이지 : 특정 ID 팔로우 피드보기 (최신순)
    public Page<RecipesDto> getFollowFeed(List<String> followIds, Pageable pageable) {
//        공개 레시피
        String status = "PUBLIC";

        Page<Recipes> recipesPage = recipesRepository
                .findByMember_UserIdInAndPostStatusOrderByInsertTimeDesc(
                    followIds, status, pageable);

        return recipesPage.map(recipesDto -> recipeMapStruct.toRecipeDto(recipesDto));
    }

    /* 저장 */
    @Transactional
    public String createRecipe(RecipesDto recipesDto,
                               List<IngredientDto> ingredientDtos,
                               List<RecipeContentDto> contentDtos,
                               List<byte[]> images,
                               List<TagDto> tagDtos,
                               byte[] thumbnail,
                               String thumbnailUrlIgnored,
                               Member member) {
        // 1) 레시피 엔티티 변환 및 기본값 설정
        Recipes recipe = recipeMapStruct.toRecipeEntity(recipesDto);
        String uuid = UUID.randomUUID().toString();
        recipe.setUuid(uuid);
        recipe.setMember(member);

        if ("VIDEO".equalsIgnoreCase(recipesDto.getRecipeType())) {
            recipe.setRecipeType("VIDEO");
            recipe.setVideoUrl(recipesDto.getVideoUrl());
            // 동영상은 내부 썸네일/다운로드 URL 불필요
            recipe.setThumbnail(null);
            recipe.setThumbnailUrl(recipesDto.getVideoUrl()); // 피드 썸네일 쓰려면(선택)
        } else {
            recipe.setRecipeType("IMAGE");
            recipe.setVideoUrl(null);
            recipe.setThumbnail(thumbnail);
            recipe.setThumbnailUrl(generateDownloadUrl(uuid));
        }

        // 3) 본문/카운터 기본값 보정 (선택)
        //  └ 엔티티가 기본형 long 이면 생략 가능
        if (recipe.getViewCount() == null)  recipe.setViewCount(0L);
        if (recipe.getLikeCount() == null)  recipe.setLikeCount(0L);
        if (recipe.getCommentCount() == null) recipe.setCommentCount(0L);
        if (recipe.getReportCount() == null) recipe.setReportCount(0L);



        // 2) 레시피 저장
        Recipes savedRecipe = recipesRepository.saveAndFlush(recipe);

        // 단계 저장: VIDEO일 땐 이미지 리스트 비워서 넘기면 됨
        List<byte[]> imgBytes = "VIDEO".equalsIgnoreCase(recipesDto.getRecipeType()) ? List.of() : images;
        recipeContentService.saveRecipeContent(contentDtos, imgBytes, recipe);

        // 3) 연관 엔티티 저장
        ingredientService.saveAll(ingredientDtos, recipe);
        recipeContentService.saveRecipeContent(contentDtos, images, recipe);
        recipeTagService.saveTagsForRecipe(tagDtos, recipe);

        return savedRecipe.getUuid();
    }

    public String generateDownloadUrl(String uuid) {
        try {
            return ServletUriComponentsBuilder
                    .fromCurrentContextPath()          // http://localhost:8080
                    .path("/recipes/download")         // /recipes/download
                    .queryParam("uuid", uuid)          // ?uuid=...
                    .toUriString();
        } catch (IllegalStateException e) {
            // 요청 컨텍스트가 없으면 상대경로로 폴백 (테스트/배치 안전)
            return "/recipes/download?uuid=" + uuid;
        }
    }

    /* 수정 */
    @Transactional
    public void updateRecipe(String uuid,
                             RecipesDto recipesDto,
                             List<IngredientDto> ingredientDtos,
                             List<RecipeContentDto> contentDtos,
                             List<byte[]> images,
                             List<TagDto> tagDtos){
        Recipes recipe = recipesRepository.findById(uuid)
                .orElseThrow(()-> new RuntimeException(errorMsg.getMessage("errors.not.found")));

        // 1) 레시피 기본 정보 업데이트
        recipeMapStruct.updateRecipe(recipesDto, recipe);

        // 2) 하위 엔티티 전체 교체
        ingredientService.replaceAll(ingredientDtos, recipe);
        recipeContentService.saveRecipeContent(contentDtos, images, recipe);

        // 🔥 기존 태그 삭제 후 새로 추가
        recipeTagRepository.deleteByRecipesUuid(uuid);
        em.flush(); // DB 반영
        recipe.getRecipeTag().clear(); // 영속성 컨텍스트에서도 비워줌
        recipeTagService.saveTagsForRecipe(tagDtos, recipe);

        // ✅ 태그 정리
        recipeTagService.cleanupUnusedTags();
    }

    /* 상세 조회*/
    @Transactional(readOnly = true)
    public RecipesDto getRecipeDetails(String uuid) {
        Recipes recipe = recipesRepository.findByIdWithTags(uuid)
                .orElseThrow(()-> new RuntimeException(errorMsg.getMessage("errors.not.found")));

        // 재료/단계는 단방향이라 개별 repo 조회
        var ingredients = ingredientRepository
                .findByRecipesUuidAndDeletedFalseOrderBySortOrderAsc(uuid);
        var contents = recipeContentRepository
                .findByRecipesUuidOrderByStepOrderAsc(uuid);

        // 엔티티 -> DTO
        RecipesDto dto = recipeMapStruct.toRecipeDto(recipe);
        dto.setIngredients(recipeMapStruct.toIngredientDtoList(ingredients));
        dto.setContents(recipeMapStruct.toRecipeContentDtoList(contents));

        return dto;
    }

    //    상세조회
    public Recipes findById(String uuid) {
        return recipesRepository.findById(uuid)
                .orElseThrow(()-> new RuntimeException(errorMsg.getMessage("errors.not.found")));
    }

    /* 삭제 */
    @Transactional
    public void deleteRecipe(String uuid) {
        // 부모에 재료/단계 컬렉션 안 들고 있으니 FK 제약 피하려면 수동 삭제 필요
        recipeTagRepository.deleteByRecipesUuid(uuid);
        ingredientRepository.deleteByRecipesUuid(uuid);
        recipeContentRepository.deleteByRecipesUuid(uuid);
        recipesRepository.deleteById(uuid);

        em.flush(); // ✅ DB 반영 먼저
        // ✅ 태그 정리
        recipeTagService.cleanupUnusedTags();
    }

    public byte[] findThumbnailByUuid(String uuid) {
        Recipes recipes = recipesRepository.findByUuid(uuid)
                .orElseThrow(()-> new RuntimeException(errorMsg.getMessage("errors.not.found")));
        return recipes.getThumbnail();
    }


}
