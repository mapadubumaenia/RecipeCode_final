package com.RecipeCode.teamproject.reci.auth.notisetting.dto;

import jakarta.persistence.Entity;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class NotiSettingDto {
    private Long settingId;
    private String userEmail;
    private String typeCode;
    private boolean allow;
}
