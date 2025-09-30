package com.RecipeCode.teamproject.reci.feed.comments.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CommentsDto {
    private Long commentsId;        // 댓글 PK
    private String recipeUuid;      // 댓글이 달린 레시피 (Recipes.uuid)
    private String userEmail;       // 작성자 (Users 연결 시 바꿀 수 있음)
    private String userId;
    private String commentsContent; // 내용
    private Long likeCount;         // 좋아요 수
    private Long reportCount;       // 신고 수
    private boolean liked;
    private boolean alreadyReported;
    private Long parentId;          // 부모 댓글 ID (대댓글이면 채워짐)
    private String profileImageUrl; // 프로필이미지

    private LocalDateTime insertTime;
    private LocalDateTime updateTime;
    private LocalDateTime deletedAt; // 소프트 삭제

    private Long commentsCount;
    private int replyCount;
}
