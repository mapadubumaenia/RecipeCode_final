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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    // 좋아요 토글 기능
    @Transactional
    public RecipesLikesDto toggleLike(String recipeUuid, String userEmail){
        Member member = memberRepository.findByUserEmail(userEmail)
                .orElseThrow(()->new RuntimeException(errorMsg.getMessage("errors.not.found")));
        Recipes recipes = recipesRepository.findByUuid(recipeUuid)
                .orElseThrow(()->new RuntimeException(errorMsg.getMessage("errors.not.found")));

        if (recipes.getLikeCount() == null) recipes.setLikeCount(0L);

        boolean liked;
        RecipesLikesDto recipesLikesDto = new RecipesLikesDto();

        // 이미 좋아요 되어 있으면 취소
        if (recipesLikesRepository.existsByMemberAndRecipes(member,recipes)){
            recipesLikesRepository.deleteByMemberAndRecipes(member,recipes);

        // 음수 방지
            long curr = recipes.getLikeCount() == null ? 0L : recipes.getLikeCount();
            recipes.setLikeCount(Math.max(0L, curr - 1));

            liked = false;
        } else {
            if(userEmail.equalsIgnoreCase(recipes.getMember().getUserEmail())){
                throw new IllegalArgumentException(errorMsg.getMessage("errors.my.likes"));
            }
            // 새 좋아요 추가
            RecipesLikes like = new RecipesLikes();
            like.setMember(member);
            like.setRecipes(recipes);
            recipesLikesRepository.save(like);

            recipes.setLikeCount(recipes.getLikeCount() + 1);
            liked = true;

            // 알림 생성
            notificationService.createNotification(
                    like.getMember().getUserEmail(),                 // 좋아요 누른 사람
                    like.getRecipes().getMember().getUserEmail(),    // 레시피 작성자
                    NotificationEvent.LIKE,                          // 이벤트 타입 (LIKE)
                    "LIKE",                                          // 소스 타입
                    String.valueOf(like.getLikeId())                 // 소스 ID
            );

        }

        // 최종 동기화 (최소 수정 포인트 ②) — DB 집계로 맞춤
        long count = recipesLikesRepository.countVisibleLikes(recipeUuid);
        recipes.setLikeCount(count);

        // 결과 DTO 채우기
        recipesLikesDto.setUserEmail(userEmail);
        recipesLikesDto.setUuid(recipeUuid);
        recipesLikesDto.setLiked(liked);
        recipesLikesDto.setLikesCount(count);

        return recipesLikesDto;
    }


    // [NEW] 단건 상태 조회
    @Transactional(readOnly = true)
    public boolean isLiked(String recipeUuid, String userEmail) {
        return recipesLikesRepository.existsByMember_UserEmailAndRecipes_Uuid(userEmail, recipeUuid);
    }

    // [NEW] 배치 상태 조회
    @Transactional(readOnly = true)
    public Map<String, Boolean> likedMapFor(String userEmail, List<String> uuids) {
        if (uuids == null || uuids.isEmpty()) return Map.of();

        // repo에 맞는 쿼리 메서드가 없다면 exists 루프/커스텀 쿼리 둘 중 한가지:
        // 간단 루프(개수 적을 때 충분히 빠름)
        Map<String, Boolean> out = new HashMap<>();
        for (String id : uuids) {
            boolean liked = recipesLikesRepository.existsByMember_UserEmailAndRecipes_Uuid(userEmail, id);
            out.put(id, liked);
        }
        return out;
    }


}
