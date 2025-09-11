package com.RecipeCode.teamproject.reci.recipes.repository;

/*
* 목록 조회 전용 projection
* 엔티티 전체를 가져오지 않고 필요한 필드만 SELECT
*
* */

public interface RecipesSummaryView {
    String getUuid();
    String getUserEmail();
    String getRecipeTitle();
    String getThumbnailImageUrl();
    String getRecipeIntro();
    String getPostStatus();
    Long getLikeCount();
    Long getCommentsCount();
    Long getInsertTime();

}
