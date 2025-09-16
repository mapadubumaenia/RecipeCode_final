package com.RecipeCode.teamproject.reci.feed.recipeslikes.service;

import com.RecipeCode.teamproject.common.ErrorMsg;
import com.RecipeCode.teamproject.common.RecipeMapStruct;
import com.RecipeCode.teamproject.reci.auth.entity.Member;
import com.RecipeCode.teamproject.reci.auth.repository.MemberRepository;
import com.RecipeCode.teamproject.reci.feed.recipes.entity.Recipes;
import com.RecipeCode.teamproject.reci.feed.recipes.repository.RecipesRepository;
import com.RecipeCode.teamproject.reci.feed.recipeslikes.dto.RecipesLikesDto;
import com.RecipeCode.teamproject.reci.feed.recipeslikes.entity.RecipesLikes;
import com.RecipeCode.teamproject.reci.feed.recipeslikes.repository.RecipesLikesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RecipesLikesService {

    private final RecipesLikesRepository recipesLikesRepository;
    private final RecipesRepository recipesRepository;
    private final MemberRepository memberRepository;
    private final RecipeMapStruct recipeMapStruct;
    private final ErrorMsg errorMsg;

    // 좋아요 토글 기능
    @Transactional
    public RecipesLikesDto toggleLike(String recipeUuid, String userEmail){
        Member member = memberRepository.findByUserEmail(userEmail)
                .orElseThrow(()->new RuntimeException(errorMsg.getMessage("errors.not.found")));
        Recipes recipes = recipesRepository.findByUuid(recipeUuid)
                .orElseThrow(()->new RuntimeException(errorMsg.getMessage("errors.not.found")));

        boolean liked;
        RecipesLikesDto recipesLikesDto = new RecipesLikesDto();

        // 이미 좋아요 되어 있으면 취소
        if (recipesLikesRepository.existsByMemberAndRecipes(member,recipes)){
            recipesLikesRepository.deleteByMemberAndRecipes(member,recipes);
            recipes.setLikeCount(recipes.getLikeCount() - 1);
            liked = false;
        } else {
            // 새 좋아요 추가
            RecipesLikes like = new RecipesLikes();
            like.setMember(member);
            like.setRecipes(recipes);
            recipesLikesRepository.save(like);

            recipes.setLikeCount(recipes.getLikeCount() + 1);
            liked = true;
        }

        // 결과 DTO 채우기
        recipesLikesDto.setUserEmail(userEmail);
        recipesLikesDto.setUuid(recipeUuid);
        recipesLikesDto.setLiked(liked);
        recipesLikesDto.setLikesCount(recipesLikesDto.getLikesCount());

        return recipesLikesDto;
    }


}
