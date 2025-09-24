package com.RecipeCode.teamproject.reci.function.notificationSetting.entity;

import com.RecipeCode.teamproject.common.BaseTimeEntity;
import com.RecipeCode.teamproject.reci.auth.entity.Member;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "NOTIFICATION_SETTING")
@SequenceGenerator(
        name = "SQ_NOTIFICATION_SETTING_JPA",
        sequenceName = "NOTIFICATION_SETTING_KEY",
        allocationSize = 1
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(of = "settingId", callSuper = false)
public class NotificationSetting extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE
            , generator = "SQ_NOTIFICATION_SETTING_JPA")
    private Long settingId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_EMAIL")
    private Member member;
    private String typeCode;  // 팔로우알림, 댓글 알림
    private Long allow; // 0=미허용 1=허용

}
