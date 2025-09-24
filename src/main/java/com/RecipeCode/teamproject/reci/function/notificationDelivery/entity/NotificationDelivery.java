package com.RecipeCode.teamproject.reci.function.notificationDelivery.entity;

import com.RecipeCode.teamproject.common.BooleanToYNConverter;
import com.RecipeCode.teamproject.reci.function.notification.entity.Notification;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "NOTIFICATION_DELIVERY")
@SequenceGenerator(
        name = "SQ_NOTIFICATION_DELIVERY_JPA",
        sequenceName = "NOTIFICATION_DELIVERY_KEY",
        allocationSize = 1
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(of = "deliveryId", callSuper = false)
public class NotificationDelivery {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE
            , generator = "SQ_NOTIFICATION_DELIVERY_JPA")
    private Long deliveryId;
    private String receiverEmail;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "NOTIFICATION_ID", nullable = false)
    private Notification notification;
    @Convert(converter = BooleanToYNConverter.class)
    private boolean isRead; // N=안읽음, Y=읽음
    private LocalDateTime readTime;
}
