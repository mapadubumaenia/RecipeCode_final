package com.RecipeCode.teamproject.reci.function.commentsReport.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CommentReportDto {
    private Long reportId;
    private String adminEmail; // 처리 담당자
    private String userEmail;
    private Long commentsId;
    private String commentContent;  // 댓글 내용
    private String reason;
    private Long reportStatus = 1L; // 신고 상태 (대기중 = 0, 처리중 = 1, 답변완료 = 2)
    private LocalDateTime insertTime;
    private LocalDateTime updateTime;
    private Long reportType;   // 신고 유형 (0=욕설, 1=스팸)\
    private Long duplicateCount;    // 동일 댓글 신고 건수

}
