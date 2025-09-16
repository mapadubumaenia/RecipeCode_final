package com.RecipeCode.teamproject.reci.function.recipeReport.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class RecipeReportDto {
    private Long reportId;
    private String adminEmail;
    private String userEmail;
    private String uuid;
    private String reason;
    private Long reportStatus; // 신고 상태 (0=대기중, 1=처리중, 2=답변완료)
    private Long reportType;   // 신고 유형 (0=욕설, 1=스팸, 2=저작권)
    private String recipeTitle;
    private Long duplicateCount;
}
