package com.RecipeCode.teamproject.reci.function.notificationDelivery.repository;

import com.RecipeCode.teamproject.reci.function.notificationDelivery.entity.NotificationDelivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    // 특정 유저의 전체 알림을 읽음 처리
    // clearAutomatically = true : 쿼리 실행 후 영속성 컨텍스트를 자동으로 비움.
    // DB 반영 안 된 엔티티가 캐시에 남아 있으면 꼬일 수 있는데 그걸 방지.
    // flushAutomatically = true : 쿼리 실행 전에 영속성 컨텍스트를 DB랑 싱크 맞춤.
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update NotificationDelivery nd " +
            "set nd.isRead = true, nd.readTime = CURRENT_TIMESTAMP " +
            "where nd.receiverEmail = :receiverEmail and nd.isRead = false")
    int markAllAsRead(@Param("receiverEmail") String receiverEmail);
}

