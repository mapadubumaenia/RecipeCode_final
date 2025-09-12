package com.RecipeCode.teamproject.reci.feed.recipecontent.service;

import com.RecipeCode.teamproject.common.ErrorMsg;
import com.RecipeCode.teamproject.reci.feed.recipecontent.dto.RecipeContentDto;
import com.RecipeCode.teamproject.reci.feed.recipecontent.entity.RecipeContent;
import com.RecipeCode.teamproject.reci.feed.recipecontent.repository.RecipeContentRepository;
import com.RecipeCode.teamproject.reci.feed.recipes.entity.Recipes;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecipeContentService {
    private final RecipeContentRepository recipeContentRepository;
    private final ErrorMsg errorMsg;

    @Transactional
    public void saveStepContents(Recipes recipes,
                                 List<RecipeContentDto> contents,
                                 List<MultipartFile> stepImages) throws Exception {
        if (contents == null || contents.isEmpty()) return;

        for (int i = 0; i < contents.size(); i++) {
            RecipeContentDto dto = contents.get(i);

            RecipeContent content = new RecipeContent();
            content.setRecipes(recipes);
            content.setStepExplain(dto.getStepExplain() != null ? dto.getStepExplain() : "");
            content.setStepOrder(dto.getStepOrder() != null ? dto.getStepOrder() : (long) (i+1));

            // 먼저 1차 저장 (id 생성)
            recipeContentRepository.save(content);

//            이미지 처리
            if (stepImages != null && i < stepImages.size()){
                MultipartFile stepFile = stepImages.get(i);
                if (stepFile != null && !stepFile.isEmpty()) {
                    content.setRecipeImage(stepFile.getBytes());
                    content.setRecipeImageUrl(generateStepDownloadUrl(content.getStepId()));
                }
            }
//        1) 먼저 저장해서 stepId 발급
//            recipeContentRepository.save(content);

//            stepId가 생긴 후 URL 설정
            if (content.getRecipeImage() != null){
                content.setRecipeImageUrl(generateStepDownloadUrl(content.getStepId()));
                recipeContentRepository.save(content);  // update
            }
        }
    }

//
    @Transactional(readOnly = true)
    public byte[] findRecipeImage(Long recipeId) {
        RecipeContent content = recipeContentRepository.findById(recipeId)
                .orElseThrow(() -> new RuntimeException(errorMsg.getMessage("errors.not.found")));
        return content.getRecipeImage();
    }

    // RecipeContentService or RecipesService 안에
    public String generateStepDownloadUrl(Long stepId) {
        return ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/recipes/content/download")
                .queryParam("stepId", stepId)
                .toUriString();
    }
}
