package com.RecipeCode.teamproject.reci.function.notification.controller;

import com.RecipeCode.teamproject.common.SecurityUtil;
import com.RecipeCode.teamproject.reci.function.notification.service.NotificationService;
import com.RecipeCode.teamproject.reci.function.notificationDelivery.dto.NotificationDeliveryDto;
import com.RecipeCode.teamproject.reci.function.notificationDelivery.entity.NotificationDelivery;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final SecurityUtil securityUtil;

    // 알림 목록 조회
    // 경로를 따로 지정하지 않으면 클래스의 경로가 그대로 엔드 포인트가 됨
    // 단순 목록 조회용이면 경로를 따로 지정하지 않아도 됨
    @GetMapping
    public List<NotificationDeliveryDto> getUserNotifications() {
        String userEmail = securityUtil.getLoginUser().getUsername();
        List<NotificationDelivery> deliveries = notificationService.getUserNotifications(userEmail);

        return deliveries.stream()
                .map(NotificationDeliveryDto::fromEntity)
                .collect(Collectors.toList());
    }

    // 안 읽은 알림 개수
    @GetMapping("/unreadCount")
    public Long getUnreadCount(){
        String userEmail = securityUtil.getLoginUser().getUsername();
        return notificationService.getUnreadCount(userEmail);
    }

    // 알림 읽음 처리 (알림 클릭하면)
    @PatchMapping("/{deliveryId}/read")
    public void markAsRead(@PathVariable Long deliveryId) {
        notificationService.markAsRead(deliveryId);
    }

}
