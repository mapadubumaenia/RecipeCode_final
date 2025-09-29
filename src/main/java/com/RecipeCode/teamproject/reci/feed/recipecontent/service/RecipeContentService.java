package com.RecipeCode.teamproject.reci.feed.recipecontent.service;

import com.RecipeCode.teamproject.common.ErrorMsg;
import com.RecipeCode.teamproject.common.RecipeMapStruct;
import com.RecipeCode.teamproject.reci.feed.recipecontent.dto.RecipeContentDto;
import com.RecipeCode.teamproject.reci.feed.recipecontent.entity.RecipeContent;
import com.RecipeCode.teamproject.reci.feed.recipecontent.repository.RecipeContentRepository;
import com.RecipeCode.teamproject.reci.feed.recipes.entity.Recipes;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecipeContentService {
    private final RecipeContentRepository recipeContentRepository;
    private final RecipeMapStruct recipeMapStruct;
    private final ErrorMsg errorMsg;


    /* ==========================
        상세조회 (레시피 기준)
    ========================== */
    public List<RecipeContentDto> getContents(String recipesUuid) {
        List<RecipeContent> contents = recipeContentRepository
                .findByRecipesUuidOrderByStepOrderAsc(recipesUuid);

        if (contents.isEmpty()) {
            throw new RuntimeException(errorMsg.getMessage("errors.not.found"));
        }
        return recipeMapStruct.toRecipeContentDtoList(contents);
    }

    public RecipeContent findById(Long stepId) {
        return recipeContentRepository.findById(stepId)
                .orElseThrow(()-> new RuntimeException(errorMsg.getMessage("errors.not.found")));
    }

    /* ==========================
        등록
    ========================== */
    public void saveRecipeContent(List<RecipeContentDto> contentDtos,
                                  List<byte[]> images,
                                  Recipes recipes){
        // 1) 기존 단계 삭제
//        recipeContentRepository.deleteByRecipesUuid(recipes.getUuid());


        // 2) 새 단계 등록
        for (int i = 0; i < contentDtos.size(); i++) {
            RecipeContent content = recipeMapStruct.toRecipeContentEntity(contentDtos.get(i));
            content.setRecipes(recipes);
            content.setStepOrder((i+1L)*10);

            if(images != null && images.size() > i && images.get(i) != null) {
                content.setRecipeImage(images.get(i));
            }
            //  3) 먼저 저장해서 stepId 생성
            RecipeContent saved = recipeContentRepository.save(content);

            //   4) stepId 기반으로 다운로드 URL 생성
            saved.setRecipeImageUrl(generateStepDownloadUrl(saved.getStepId()));

            //    5) 다시 update
            recipeContentRepository.save(saved);
        }
    }

//    public void updateRecipeContents(Recipes recipe,
//                                     List<RecipeContentDto> contentDtos,
//                                     List<byte[]> images) {
//        // 기존 stepId -> 엔티티 Map
//        Map<Long, RecipeContent> existing = recipeContentRepository
//                .findByRecipesUuidOrderByStepOrderAsc(recipe.getUuid())
//                .stream().collect(Collectors.toMap(RecipeContent::getStepId, Function.identity()));
//
//        for (int i = 0; i < contentDtos.size(); i++) {
//            RecipeContentDto dto = contentDtos.get(i);
//            RecipeContent entity;
//
//            boolean newImageUploaded = (images != null && images.size() > i && images.get(i) != null);
//
//            if (dto.getStepId() != null && existing.containsKey(dto.getStepId())) {
//                // 기존 단계 업데이트
//                entity = existing.remove(dto.getStepId());
//                recipeMapStruct.updateRecipeContent(dto, entity);
//                entity.setStepOrder((i+1L) * 10);
//
//                // 이미지 새로 업로드된 경우만 교체
//                if (images != null && images.size() > i && images.get(i) != null) {
//                    entity.setRecipeImage(images.get(i));
//                }
//            } else {
//                // 새 단계 추가
//                entity = recipeMapStruct.toRecipeContentEntity(dto);
//                entity.setRecipes(recipe);
//                entity.setStepOrder((i+1L) * 10);
//                if (images != null && images.size() > i && images.get(i) != null) {
//                    entity.setRecipeImage(images.get(i));
//                }
//            }
//            recipeContentRepository.save(entity);
//        }
//
//        // 요청에 없는 단계 삭제
//        if (!existing.isEmpty()) {
//            recipeContentRepository.deleteAll(existing.values());
//        }
//    }

    public void updateRecipeContents(Recipes recipe, List<RecipeContentDto> contentDtos) {
        Map<Long, RecipeContent> existing = recipeContentRepository
                .findByRecipesUuidOrderByStepOrderAsc(recipe.getUuid())
                .stream()
                .collect(Collectors.toMap(RecipeContent::getStepId, Function.identity()));

        for (int i = 0; i < contentDtos.size(); i++) {
            RecipeContentDto dto = contentDtos.get(i);
            RecipeContent entity;

            if (dto.getStepId() != null && existing.containsKey(dto.getStepId())) {
                entity = existing.remove(dto.getStepId());
                recipeMapStruct.updateRecipeContent(dto, entity);
                entity.setStepOrder((i + 1L) * 10);
            } else {
                entity = recipeMapStruct.toRecipeContentEntity(dto);
                entity.setRecipes(recipe);
                entity.setStepOrder((i + 1L) * 10);
                entity = recipeContentRepository.save(entity); // stepId 확보
            }

            // 🔑 업로드가 있을 때만 바이트/URL 교체
            if (dto.getRecipeImage() != null && !dto.getRecipeImage().isEmpty()) {
                try {
                    entity.setRecipeImage(dto.getRecipeImage().getBytes());
                } catch (IOException e) {
                    throw new UncheckedIOException("단계 이미지 변환 실패", e);
                }
                entity.setRecipeImageUrl(generateStepDownloadUrl(entity.getStepId()));
            }
            // 업로드가 없으면 건드리지 않음(기존 이미지/URL 유지)

            // 초기 마이그레이션 등으로 URL이 비었으면 보정
            if (entity.getRecipeImage() == null || entity.getRecipeImageUrl().isBlank()) {
                entity.setRecipeImageUrl(generateStepDownloadUrl(entity.getStepId()));
            }

            recipeContentRepository.save(entity);
        }

        // 요청에 빠진 단계는 삭제
        if (!existing.isEmpty()) recipeContentRepository.deleteAll(existing.values());
    }


    public String generateStepDownloadUrl(Long stepId) {
        try{
            return ServletUriComponentsBuilder
                    .fromCurrentContextPath()
                    .path("/recipes/content/download")
                    .queryParam("stepId", stepId)
                    .toUriString();
        } catch (IllegalStateException e) {
            return "/recipes/content/download?stepId=" + stepId;
        }
    }

    /* 삭제(레시피 단위) */
    public void deleteByRecipe(String recipesUuid) {
        recipeContentRepository.deleteByRecipesUuid(recipesUuid);
    }
}
