package com.RecipeCode.teamproject.reci.function.notificationDelivery.dto;

import com.RecipeCode.teamproject.reci.auth.entity.Member;
import com.RecipeCode.teamproject.reci.auth.repository.MemberRepository;
import com.RecipeCode.teamproject.reci.feed.comments.repository.CommentsRepository;
import com.RecipeCode.teamproject.reci.feed.recipeslikes.repository.RecipesLikesRepository;
import com.RecipeCode.teamproject.reci.function.notification.dto.NotificationDto;
import com.RecipeCode.teamproject.reci.function.notification.entity.Notification;
import com.RecipeCode.teamproject.reci.function.notificationDelivery.entity.NotificationDelivery;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class NotificationDeliveryDto {

    private Long deliveryId;
    private String receiverEmail;
    private Long notificationId;
    private NotificationDto notification;
    private boolean isRead;
    private LocalDateTime readTime;

    public static NotificationDeliveryDto fromEntity(
            NotificationDelivery delivery,
            MemberRepository memberRepository,
            CommentsRepository commentsRepository,
            RecipesLikesRepository recipesLikesRepository
    ) {
        Notification notification = delivery.getNotification();

        // 1) 이메일로 userId 조회
        String actorEmail = notification.getActorEmail();
        String actorUserId = memberRepository.findByUserEmail(actorEmail)
                .map(Member::getUserId)
                .orElse(actorEmail);

        // 2)  메시지는 저장된 값을 그대로 사용
        String message = notification.getMessage();

        // 3) 기본 NotificationDto
        NotificationDto notificationDto = new NotificationDto(
                notification.getNotificationId(),
                actorEmail,
                actorUserId,
                notification.getTitle(),
                notification.getEvent(),
                message,
                notification.getSourceType(),
                notification.getSourceId(),
                notification.getInsertTime(),
                null
        );
        // 4) 소스 타입에 따른 링크 데이터 세팅
        String st = notification.getSourceType();
        if ("COMMENT".equals(st)) {
            try {
                Long commentId = Long.valueOf(notification.getSourceId());
                commentsRepository.findById(commentId).ifPresent(c ->
                        notificationDto.setRecipeUuid(c.getRecipes().getUuid())
                );
            } catch (NumberFormatException ignore) {
            }
        } else if ("LIKE".equals(st)) {
            try {
                Long likeId = Long.valueOf(notification.getSourceId());
                recipesLikesRepository.findById(likeId).ifPresent(like ->
                        notificationDto.setRecipeUuid(like.getRecipes().getUuid())
                );
            } catch (NumberFormatException ignore) {
            }
        } else if ("RECIPE".equals(st)) {
            //  신고 '유지' 케이스: sourceId에 recipeUuid가 들어있음
            notificationDto.setRecipeUuid(notification.getSourceId());
        } else if ("RECIPE_REPORT".equals(st)) {
            //  신고 '삭제' 케이스: 이동 없음 → recipeUuid 설정하지 않음
            // no-op
        }
        // 기타/알 수 없는 타입도 예외 던지지 말고 그대로 둔다.

        return new NotificationDeliveryDto(
                delivery.getDeliveryId(),
                delivery.getReceiverEmail(),
                notification.getNotificationId(),
                notificationDto,
                delivery.isRead(),
                delivery.getReadTime()
        );
    }
}