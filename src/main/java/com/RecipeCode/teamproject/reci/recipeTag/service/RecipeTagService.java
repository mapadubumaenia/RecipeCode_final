package com.RecipeCode.teamproject.reci.recipeTag.service;

import com.RecipeCode.teamproject.common.ErrorMsg;
import com.RecipeCode.teamproject.common.MapStruct;
import com.RecipeCode.teamproject.reci.recipeTag.dto.RecipeTagDto;
import com.RecipeCode.teamproject.reci.recipeTag.entity.RecipeTag;
import com.RecipeCode.teamproject.reci.recipeTag.repository.RecipeTagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecipeTagService {
    private final RecipeTagRepository recipeTagRepository;
    private final MapStruct mapStruct;
    private final ErrorMsg errorMsg;

    public Page<RecipeTagDto> selectRecipeTagList(String searchKeyword, Pageable pageable) {
        Page<RecipeTag> page = recipeTagRepository.selectRecipeTagList(searchKeyword, pageable);
        return page.map(recipeTag -> mapStruct.toDto(recipeTag));
    }

    public void save(RecipeTagDto recipeTagDto) {
        RecipeTag recipeTag = mapStruct.toEntity(recipeTagDto);
        recipeTagRepository.save(recipeTag);
    }

    public RecipeTagDto findById(long tagId) {
//        JPA 상세조회 함수 실행
        RecipeTag recipeTag = recipeTagRepository.findById(tagId)
                .orElseThrow(() -> new RuntimeException(errorMsg.getMessage("errors.not.found")));

        return mapStruct.toDto(recipeTag);
    }
}
