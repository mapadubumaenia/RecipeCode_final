package com.RecipeCode.teamproject.reci.function.notificationDelivery.dto;

import com.RecipeCode.teamproject.reci.auth.entity.Member;
import com.RecipeCode.teamproject.reci.auth.repository.MemberRepository;
import com.RecipeCode.teamproject.reci.feed.comments.repository.CommentsRepository;
import com.RecipeCode.teamproject.reci.feed.recipeslikes.repository.RecipesLikesRepository;
import com.RecipeCode.teamproject.reci.function.notification.dto.NotificationDto;
import com.RecipeCode.teamproject.reci.function.notification.entity.Notification;
import com.RecipeCode.teamproject.reci.function.notification.enums.NotificationEvent;
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

        // 2) 메시지 조립
        String message = NotificationEvent.valueOf(notification.getEvent())
                .formatMessage(actorUserId);

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

        if ("COMMENT".equals(notification.getSourceType())) {
            Long commentId = Long.valueOf(notification.getSourceId());
            commentsRepository.findById(commentId).ifPresent(comment -> {
                notificationDto.setRecipeUuid(comment.getRecipes().getUuid());
            });
        } else if ("LIKE".equals(notification.getSourceType())) {
            Long likeId = Long.valueOf(notification.getSourceId());
            recipesLikesRepository.findById(likeId).ifPresent(like -> {
                notificationDto.setRecipeUuid(like.getRecipes().getUuid());
            });
        }

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