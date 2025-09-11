package com.RecipeCode.teamproject.reci.recipes.service;

import com.RecipeCode.teamproject.common.ErrorMsg;
import com.RecipeCode.teamproject.common.MapStruct;
import com.RecipeCode.teamproject.reci.auth.entity.Member;
import com.RecipeCode.teamproject.reci.auth.repository.MemberRepository;
import com.RecipeCode.teamproject.reci.recipes.dto.RecipesDto;
import com.RecipeCode.teamproject.reci.recipes.entity.Recipes;
import com.RecipeCode.teamproject.reci.recipes.repository.RecipesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecipesService {

    private final MemberRepository memberRepository;
    private final RecipesRepository recipesRepository;
    private final MapStruct mapStruct;
    private final ErrorMsg errorMsg;

// 내 팔로우 페이지 : 특정 ID 팔로우 피드보기 (최신순)
    public Page<RecipesDto> getFollowFeed(List<String> followIds, Pageable pageable) {
//        공개 레시피
        String status = "PUBLIC";

        Page<Recipes> recipesPage = recipesRepository
                .findByMember_UserIdInAndPostStatusOrderByInsertTimeDesc(
                    followIds, status, pageable);

        return recipesPage.map(recipesDto -> mapStruct.toDto(recipesDto));
    }


//    레시피 등록
    @Transactional
    public String save(RecipesDto recipesDto, String userEmail) {
//        1) 작성자 Member 조회
        Member member = memberRepository.findById(userEmail)
                .orElseThrow(()-> new RuntimeException(errorMsg.getMessage("errors.not.found")));

//        2) Dto -> Entity 변환
        Recipes recipes = mapStruct.toEntity(recipesDto);
        recipes.setMember(member);

//        3) 재료 매핑
        if (recipes.getIngredients() != null) {
            recipes.getIngredients().forEach(ingredient -> ingredient.setRecipes(recipes));
        }

//        4) 조리 순서 매핑
        if(recipes.getContents() != null) {
            recipes.getContents().forEach(content -> content.setRecipes(recipes));
        }
//        5) 저장
            Recipes insert = recipesRepository.save(recipes);

            return insert.getUuid();    // 새로 생성된 레시피 ID 반환
    }
}
