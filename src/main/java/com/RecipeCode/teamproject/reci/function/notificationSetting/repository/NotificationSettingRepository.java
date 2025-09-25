package com.RecipeCode.teamproject.reci.function.notificationSetting.repository;

import com.RecipeCode.teamproject.reci.function.notificationSetting.entity.NotificationSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationSettingRepository extends JpaRepository<NotificationSetting, Long> {
    // 특정 유저의 특정 알림 타입 허용 여부 조회
    Optional<NotificationSetting> findByMember_UserEmailAndTypeCode(String userEmail, String typeCode);
}
