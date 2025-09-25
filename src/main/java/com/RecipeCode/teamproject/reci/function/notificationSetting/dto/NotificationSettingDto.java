package com.RecipeCode.teamproject.reci.function.notificationSetting.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class NotificationSettingDto {
    private Long settingId;
    private String userEmail;
    private String typeCode;  // 팔로우알림, 댓글 알림
    private Long allow; // 0=미허용 1=허용
}
