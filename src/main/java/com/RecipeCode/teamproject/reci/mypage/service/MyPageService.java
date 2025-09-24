package com.RecipeCode.teamproject.reci.mypage.service;

import com.RecipeCode.teamproject.common.ErrorMsg;
import com.RecipeCode.teamproject.common.RecipeMapStruct;

import com.RecipeCode.teamproject.reci.auth.repository.MemberRepository;
import com.RecipeCode.teamproject.reci.feed.recipes.dto.RecipesDto;
import com.RecipeCode.teamproject.reci.feed.recipes.entity.Recipes;
import com.RecipeCode.teamproject.reci.feed.recipes.repository.RecipesRepository;
import com.RecipeCode.teamproject.reci.feed.recipeslikes.repository.RecipesLikesRepository;
import com.RecipeCode.teamproject.reci.function.follow.repository.FollowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MyPageService {
    private final RecipeMapStruct recipeMapStruct;
    private final RecipesRepository recipesRepository;
    private final RecipesLikesRepository recipesLikesRepository;
    private final FollowRepository followRepository;
    private final MemberRepository memberRepository;
    private final ErrorMsg errorMsg;

    // Profile 페이지 내 피드 조회
    public Slice<RecipesDto> getMyRecipes(String userEmail,
                                          Pageable pageable) {
        Slice<Recipes> slice = recipesRepository.findByUserEmail(userEmail, pageable);
        return slice.map(s->recipeMapStruct.toRecipeDto(s));
    }

    // Profile 페이지 내가 좋아요 한 피드 조회
    public Slice<RecipesDto> getMyLikedRecipes(String userEmail,
                                               Pageable pageable) {
        Slice<Recipes> slice = recipesLikesRepository.findByLikedRecipes(userEmail, pageable);
        List<RecipesDto> dtos = slice.getContent()
                .stream().map(recipes->recipeMapStruct.toRecipeDto(recipes))
                .collect(Collectors.toList());
        for (RecipesDto dto : dtos) {
            dto.setLiked(true);
        }

        return new SliceImpl<>(dtos, pageable, slice.hasNext());
    }

    // 리스트가 비면 빈 Slice 반환?
    private <T> Slice<T> emptySlice(Pageable pageable) {
        return new SliceImpl<>(List.of(), pageable, false);
    }

    // 내가 팔로우하는 사용자들의 최신 레시피(유저별 1건)
    @Transactional
    public Slice<RecipesDto> getFollowingLatestFeed(String viewerEmail, Pageable pageable) {
        List<String> userIds = followRepository.findFollowingUserIds(viewerEmail);
        if(userIds.isEmpty()) return emptySlice(pageable);

        Page<Recipes> page = recipesRepository.findLatestPublicPerUser(userIds, pageable);

        // 좋아요 표시 동기화
        List<String> uuids = page.getContent().stream().map(Recipes::getUuid).toList();
        List<String> likedUuids = uuids.isEmpty() ? List.of()
                : recipesLikesRepository.findLikedRecipesUuids(viewerEmail, uuids);

        return page.map(r -> {
            RecipesDto dto = recipeMapStruct.toRecipeDto(r);
            dto.setLiked(likedUuids.contains(r.getUuid()));
            return dto;
        });
    }

    public Slice<RecipesDto> getFollowersLatestFeed(String viewerEmail, Pageable pageable) {
        List<String> userIds = followRepository.findFollowerUserIds(viewerEmail);
        if(userIds.isEmpty()) return emptySlice(pageable);

        Page<Recipes> page = recipesRepository.findLatestPublicPerUser(userIds, pageable);

        List<String> uuids = page.getContent().stream().map(Recipes::getUuid).toList();
        List<String> likedUuids = uuids.isEmpty() ? List.of()
                : recipesLikesRepository.findLikedRecipesUuids(viewerEmail, uuids);

        return page.map(r-> {
            RecipesDto dto = recipeMapStruct.toRecipeDto(r);
            dto.setLiked(likedUuids.contains(r.getUuid()));
            return dto;
        });

    }
}
