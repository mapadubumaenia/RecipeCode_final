package com.RecipeCode.teamproject.reci.function.notification.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class NotificationDto {
    private Long notificationId;
    private String actorEmail;
    private String title;
    private String event;
    private String message;
    private String sourceType;
    private String sourceId;
    private LocalDateTime insertTime;


}

