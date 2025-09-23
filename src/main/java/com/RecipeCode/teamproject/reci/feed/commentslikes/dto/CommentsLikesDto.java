package com.RecipeCode.teamproject.reci.feed.commentslikes.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CommentsLikesDto {
    private Long likeId;
    private String userEmail;
    private Long commentsId;
    private boolean liked;      // 현재 유저가 눌렀는지 여부
    private Long likesCount;    // 총 좋아요 수
}
