package com.RecipeCode.teamproject.reci.function.notification.service;

import com.RecipeCode.teamproject.common.ErrorMsg;
import com.RecipeCode.teamproject.reci.auth.repository.MemberRepository;
import com.RecipeCode.teamproject.reci.feed.comments.repository.CommentsRepository;
import com.RecipeCode.teamproject.reci.feed.recipeslikes.repository.RecipesLikesRepository;
import com.RecipeCode.teamproject.reci.function.notification.entity.Notification;
import com.RecipeCode.teamproject.reci.function.notification.enums.NotificationEvent;
import com.RecipeCode.teamproject.reci.function.notification.repository.NotificationRepository;
import com.RecipeCode.teamproject.reci.function.notificationDelivery.dto.NotificationDeliveryDto;
import com.RecipeCode.teamproject.reci.function.notificationDelivery.entity.NotificationDelivery;
import com.RecipeCode.teamproject.reci.function.notificationDelivery.repository.NotificationDeliveryRepository;
import com.RecipeCode.teamproject.reci.function.notificationSetting.entity.NotificationSetting;
import com.RecipeCode.teamproject.reci.function.notificationSetting.repository.NotificationSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationDeliveryRepository deliveryRepository;
    private final NotificationSettingRepository settingRepository;
    private final ErrorMsg errorMsg;
    private final CommentsRepository commentsRepository;
    private final MemberRepository memberRepository;
    private final RecipesLikesRepository recipesLikesRepository;

    // 알림 생성 + Delivery 전송
    public void createNotification(String actorEmail,
                                   String targetEmail,
                                   NotificationEvent event,
                                   String sourceType,
                                   String sourceId) {

        // 자기 자신에게는 알림 보내지 않음
        if (actorEmail.equals(targetEmail)) {
            return;
        }
        // 1) 대상 유저의 알림 허용 여부 확인
        Optional<NotificationSetting> settingOptional = settingRepository.findByMember_UserEmailAndTypeCode(targetEmail, event.getCode());
        boolean isAllowed = settingOptional.map(setting -> setting.getAllow() == 1L).orElse(true);

        // 허용되지 않았으면 알림을 생성하지 않고 종료
        if (!isAllowed) {
            return;
        }

        // 2) 알림 원본(Notification) 저장 - 어떤 내용으로 어떤 이벤트에 대해 보낸건지
        Notification notification = new Notification();
        notification.setActorEmail(actorEmail);
        notification.setEvent(event.getCode()); // DB에는 "FOLLOW", "COMMENT", "LIKE"
        notification.setMessage(event.formatMessage(actorEmail)); //
        notification.setSourceType(sourceType);
        notification.setSourceId(sourceId);
        notification.setTitle(event + " 알림");
        Notification savedNotification = notificationRepository.save(notification);

        // 3) 알림을 보낸 정보 저장 - 기본을 N으로 (false)
        NotificationDelivery delivery = new NotificationDelivery();
        delivery.setNotification(savedNotification);
        delivery.setReceiverEmail(targetEmail);
        delivery.setRead(false);
        delivery.setReadTime(null);
        deliveryRepository.save(delivery);
    }

    // 유저 알림 목록 조회
    public List<NotificationDeliveryDto> getUserNotifications(String userEmail) {
        List<NotificationDelivery> deliveries =
                deliveryRepository.findByReceiverEmailOrderByDeliveryIdDesc(userEmail);

        return deliveries.stream()
                .map(d -> NotificationDeliveryDto.fromEntity(
                        d,
                        memberRepository,
                        commentsRepository,
                        recipesLikesRepository
                ))
                .collect(Collectors.toList());
    }

    // 유저의 안 읽은 알림 개수
    public Long getUnreadCount(String userEmail) {
        return deliveryRepository.countByReceiverEmailAndIsRead(userEmail, false);
    }

    // 개별 알림 읽음 처리
    public void markAsRead(Long deliveryId) {
        NotificationDelivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new RuntimeException(errorMsg.getMessage("errors.notification.notfound")));
        delivery.setRead(true);
        delivery.setReadTime(LocalDateTime.now());
    }
    // 전체 알림 읽음 처리
    public int markAllAsRead(String userEmail) {
        return deliveryRepository.markAllAsRead(userEmail);
    }
}