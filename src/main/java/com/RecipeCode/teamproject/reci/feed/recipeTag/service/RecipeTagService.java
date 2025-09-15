package com.RecipeCode.teamproject.reci.feed.recipeTag.service;

import com.RecipeCode.teamproject.common.ErrorMsg;
import com.RecipeCode.teamproject.common.RecipeMapStruct;
import com.RecipeCode.teamproject.reci.feed.recipeTag.dto.RecipeTagDto;
import com.RecipeCode.teamproject.reci.feed.recipeTag.entity.RecipeTag;
import com.RecipeCode.teamproject.reci.feed.recipeTag.repository.RecipeTagRepository;
import com.RecipeCode.teamproject.reci.feed.recipes.entity.Recipes;
import com.RecipeCode.teamproject.reci.tag.dto.TagDto;
import com.RecipeCode.teamproject.reci.tag.entity.Tag;
import com.RecipeCode.teamproject.reci.tag.repository.TagRepository;
import com.RecipeCode.teamproject.reci.tag.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecipeTagService {

    private final RecipeTagRepository recipeTagRepository;
    private final TagRepository tagRepository;
    private final RecipeMapStruct recipeMapStruct;
    private final TagService tagService;
    private final ErrorMsg errorMsg;

    //    레시피에 태그 저장
    public void saveTagsForRecipe(List<TagDto> tagDtos,
                                  Recipes recipe) {
        for (TagDto dto : tagDtos) {
//            1.태그가 이미 DB에 있는지 확인(없으면 생성)
            Tag tag = tagService.saveOrGetTag(dto.getTag()); // 태그 존재 체크 -> 없으면 생성

//            2. 이미 연결된 태그인지 체크
            boolean exists = recipeTagRepository.existsByRecipesAndTag(recipe,tag);
            if (exists) continue; // 중복 방지

//            3. 새로 연결
            RecipeTag recipeTag = new RecipeTag();
            recipeTag.setRecipes(recipe);
            recipeTag.setTag(tag);

            // 양방향 동기화
            recipe.getRecipeTag().add(recipeTag);
            recipeTagRepository.save(recipeTag);
        }
    }
    // 전체 교체
    public void replaceTagsForRecipe(List<TagDto> tagDtos, Recipes recipe) {
        // 기존 연결 모두 제거
        recipeTagRepository.deleteByRecipesUuid(recipe.getUuid());
        recipe.getRecipeTag().clear(); // 양방향 컬렉션도 깨끗이

        // 새로 연결
        for (TagDto dto : tagDtos) {
            Tag tag = tagService.saveOrGetTag(dto.getTag()); // 존재하면 재사용, 없으면 생성
            RecipeTag link = new RecipeTag();
            link.setRecipes(recipe);
            link.setTag(tag);
            recipeTagRepository.save(link);
            recipe.getRecipeTag().add(link); // 양방향 동기화
        }
    }

    //  RecipeTagService 쪽에 연결 끊긴 태그 처리 로직 추가
    public void cleanupUnusedTags() {
        List<Long> inUseIds = recipeTagRepository.findAllTagIdsInUse();
        List<Tag> allTags = tagRepository.findAll();
        for (Tag tag : allTags) {
            if (!inUseIds.contains(tag.getTagId()) && !tag.isDeleted()) {
                tag.setDeleted(true);
                tagRepository.save(tag);
            }
        }
    }

    //    레시피별 태그 조회
    public List<RecipeTagDto> getTagByRecipeUuid(String recipeUuid) {
        List<RecipeTag> recipeTags = recipeTagRepository.findByRecipesUuid(recipeUuid);
        return recipeMapStruct.toRecipeTagDtoList(recipeTags);
    }
}
