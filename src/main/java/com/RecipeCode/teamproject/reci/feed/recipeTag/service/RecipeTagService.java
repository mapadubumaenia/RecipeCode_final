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
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    /** 태그 더티체킹 적용 : **/

    public void syncTagsForRecipe(Recipes recipe, List<TagDto> incomingDtos) {
        List<TagDto> safeDtos = (incomingDtos == null) ? List.of() : incomingDtos;

        // 1) 현재 연결된 RecipeTag들(Tag까지 fetch)
        List<RecipeTag> currentLinks = recipeTagRepository.findByRecipesUuidWithTag(recipe.getUuid());

        // 2) 현재 연결을 "정규화된 태그문자열" -> RecipeTag로 맵핑
        //  정규화 : trim + 내부연속공백 1개 + Lower - case (TagService.saveOrGetTag와 동일 규칙)
        java.util.function.Function<String,String> norm = s ->
                (s == null) ? "" : s.trim().replaceAll("\\s+", " ").toLowerCase();
        // 3) 현재 연결 맵(정규화된 문자열 -> 링크)
        java.util.Map<String, RecipeTag> currentMap = new java.util.HashMap<>();
        for (RecipeTag rt : currentLinks) {
            currentMap.put(norm.apply(rt.getTag().getTag()), rt);
        }

        // 4) 폼에서 들어온 태그를 정규화 + 중복 제거(입력 중복 방지)
        java.util.LinkedHashSet<String> want = new java.util.LinkedHashSet<>();
        for (TagDto dto : safeDtos) {
            String key = norm.apply(dto.getTag());
            if (!key.isBlank()) want.add(key);
        }

        // 5) 추가 또는 유지 판단
        for (String key : want) {
            RecipeTag link = currentMap.remove(key); // 존재하면 여기서 제거(유지)
            if(link == null) {
                // 추가: Tag가 있으면 재사용 / 없으면 생성(삭제된 상태면 재활성화)
                Tag tag = tagService.saveOrGetTag(key); // 내부에서 normalize + 재활성화 처리됨
                RecipeTag newLink = new RecipeTag();
                newLink.setRecipes(recipe);
                newLink.setTag(tag);
                recipe.getRecipeTag().add(newLink);
                recipeTagRepository.save(newLink);
            }
        }

        // 5) 남아있는 것 = 더이상 원치 않는 연결 -> 삭제
        if (!currentMap.isEmpty()) {
            java.util.Collection<RecipeTag> toDelete = currentMap.values();
            recipe.getRecipeTag().removeAll(toDelete);
            recipeTagRepository.deleteAll(toDelete);
        }

        // 6) 연결이 하나도 남지 않은 태그는 soft delete
        cleanupUnusedTags();

    }

    //    레시피별 태그 조회
    public List<RecipeTagDto> getTagByRecipeUuid(String recipeUuid) {
        List<RecipeTag> recipeTags = recipeTagRepository.findByRecipesUuid(recipeUuid);
        return recipeMapStruct.toRecipeTagDtoList(recipeTags);
    }
}
