package com.RecipeCode.teamproject.reci.function.emailCertify.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ResetPasswordRequest {
    private String email;
    private String code;
    private String newPassword;
}
