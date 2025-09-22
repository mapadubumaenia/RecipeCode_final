package com.RecipeCode.teamproject.reci.auth.notisetting.entity;

import com.RecipeCode.teamproject.common.BaseTimeEntity;
import com.RecipeCode.teamproject.common.BooleanToYNConverter;
import com.RecipeCode.teamproject.reci.auth.entity.Member;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString
@AllArgsConstructor
@Table(name="NOTIFICATION_SETTING")
@SequenceGenerator(name = "NOTIFICATION_SETTING_KEY_JPA",
        sequenceName = "NOTIFICATION_SETTING_KEY",
        allocationSize = 1)
@Builder
@EqualsAndHashCode(of = "settingId", callSuper = false)
public class NotiSetting extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
            generator = "NOTIFICATION_SETTING_KEY_JPA")
    private Long settingId;

    @ManyToOne
    @JoinColumn(name = "USER_EMAIL", nullable = false)
    private Member member;

    private String typeCode;
    private boolean allow;

}
