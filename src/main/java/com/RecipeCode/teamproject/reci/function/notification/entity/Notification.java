package com.RecipeCode.teamproject.reci.function.notification.entity;

import com.RecipeCode.teamproject.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "NOTIFICATION")
@SequenceGenerator(
        name = "SQ_NOTIFICATION_JPA",
        sequenceName = "NOTIFICATION_KEY",
        allocationSize = 1
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(of = "notificationId", callSuper = false)
public class Notification extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE
            , generator = "SQ_NOTIFICATION_JPA")
    private Long notificationId;
    private String actorEmail;
    private String title;
    private String event;
    private String message;
    private String sourceType;
    private String sourceId;

}
