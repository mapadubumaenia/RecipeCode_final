package com.RecipeCode.teamproject.reci.function.notificationDelivery.repository;

import com.RecipeCode.teamproject.reci.function.notificationDelivery.entity.NotificationDelivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationDeliveryRepository extends JpaRepository<NotificationDelivery, Long> {
    // 특정 유저의 알림 전체 조회 - 최신순
    List<NotificationDelivery> findByReceiverEmailOrderByDeliveryIdDesc(String receiverEmail);

    // 특정 유저의 읽지 않은 알림 개수
    Long countByReceiverEmailAndIsRead(String receiverEmail, boolean isRead);

    // 특정 유저의 읽지 않은 알림 목록
    List<NotificationDelivery> findByReceiverEmailAndIsReadOrderByDeliveryIdDesc(String receiverEmail, boolean isRead);
}
