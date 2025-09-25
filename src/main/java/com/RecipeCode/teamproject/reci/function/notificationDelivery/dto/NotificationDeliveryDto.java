package com.RecipeCode.teamproject.reci.function.notificationDelivery.dto;

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

    public static NotificationDeliveryDto fromEntity(NotificationDelivery delivery) {
        NotificationDto notificationDto = new NotificationDto(
                delivery.getNotification().getNotificationId(),
                delivery.getNotification().getActorEmail(),
                delivery.getNotification().getTitle(),
                delivery.getNotification().getEvent(),
                delivery.getNotification().getMessage(),
                delivery.getNotification().getSourceType(),
                delivery.getNotification().getSourceId(),
                delivery.getNotification().getInsertTime()
        );

        return new NotificationDeliveryDto(
                delivery.getDeliveryId(),
                delivery.getReceiverEmail(),
                delivery.getNotification().getNotificationId(),
                notificationDto,
                delivery.isRead(),
                delivery.getReadTime()
        );
    }
}