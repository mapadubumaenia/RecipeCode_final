package com.RecipeCode.teamproject.reci.feed.recipeslikes.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class RecipesLikesDto {

    private Long likeId;
    private String userEmail;
    private String uuid;
    private boolean liked;      // 현재 유저가 눌렀는지 여부
    private Long likesCount;    // 총 좋아요 수


}
