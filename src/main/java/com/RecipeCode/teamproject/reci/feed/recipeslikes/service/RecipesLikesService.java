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
import com.RecipeCode.teamproject.reci.function.notification.enums.NotificationEvent;
import com.RecipeCode.teamproject.reci.function.notification.service.NotificationService;
import com.RecipeCode.teamproject.es.reco.service.EventIngestService;   // ★ 추가
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RecipesLikesService {

    private final RecipesLikesRepository recipesLikesRepository;
    private final RecipesRepository recipesRepository;
    private final MemberRepository memberRepository;
    private final RecipeMapStruct recipeMapStruct;
    private final ErrorMsg errorMsg;
    private final NotificationService notificationService;
    private final EventIngestService eventIngestService;   // ★ 추가

    @Transactional
    public RecipesLikesDto toggleLike(String recipeUuid, String userEmail){
        Member member = memberRepository.findByUserEmail(userEmail)
                .orElseThrow(() -> new RuntimeException(errorMsg.getMessage("errors.not.found")));
        Recipes recipes = recipesRepository.findByUuid(recipeUuid)
                .orElseThrow(() -> new RuntimeException(errorMsg.getMessage("errors.not.found")));

        if (recipes.getLikeCount() == null) recipes.setLikeCount(0L);

        boolean liked;
        RecipesLikesDto recipesLikesDto = new RecipesLikesDto();

        if (recipesLikesRepository.existsByMemberAndRecipes(member, recipes)) {
            // 취소
            recipesLikesRepository.deleteByMemberAndRecipes(member, recipes);
            long curr = recipes.getLikeCount() == null ? 0L : recipes.getLikeCount();
            recipes.setLikeCount(Math.max(0L, curr - 1));
            liked = false;
        } else {
            // 자기글 방지
            if (userEmail.equalsIgnoreCase(recipes.getMember().getUserEmail())) {
                throw new IllegalArgumentException(errorMsg.getMessage("errors.my.likes"));
            }
            // 추가
            RecipesLikes like = new RecipesLikes();
            like.setMember(member);
            like.setRecipes(recipes);
            recipesLikesRepository.save(like);

            recipes.setLikeCount(recipes.getLikeCount() + 1);
            liked = true;

            // 알림
            notificationService.createNotification(
                    like.getMember().getUserEmail(),
                    like.getRecipes().getMember().getUserEmail(),
                    NotificationEvent.LIKE,
                    "LIKE",
                    String.valueOf(like.getLikeId())
            );
        }

        // DB 집계로 동기화
        long count = recipesLikesRepository.countVisibleLikes(recipeUuid);
        recipes.setLikeCount(count);

        // DTO
        recipesLikesDto.setUserEmail(userEmail);
        recipesLikesDto.setUuid(recipeUuid);
        recipesLikesDto.setLiked(liked);
        recipesLikesDto.setLikesCount(count);

        // ★ 이벤트는 '정말 성공'했을 때만, 그리고 커밋 이후에 전송
        if (liked) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override public void afterCommit() {
                    eventIngestService.sendEvent(
                            userEmail.trim().toLowerCase(),
                            recipeUuid,
                            "like"
                    );
                }
            });
        }

        return recipesLikesDto;
    }

    @Transactional(readOnly = true)
    public boolean isLiked(String recipeUuid, String userEmail) {
        return recipesLikesRepository.existsByMember_UserEmailAndRecipes_Uuid(userEmail, recipeUuid);
    }

    @Transactional(readOnly = true)
    public Map<String, Boolean> likedMapFor(String userEmail, List<String> uuids) {
        if (uuids == null || uuids.isEmpty()) return Map.of();
        Map<String, Boolean> out = new HashMap<>();
        for (String id : uuids) {
            boolean liked = recipesLikesRepository.existsByMember_UserEmailAndRecipes_Uuid(userEmail, id);
            out.put(id, liked);
        }
        return out;
    }
}
