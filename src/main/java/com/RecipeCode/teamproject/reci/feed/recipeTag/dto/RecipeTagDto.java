package com.RecipeCode.teamproject.reci.feed.recipeTag.dto;


import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class RecipeTagDto {
    private Long recipeTagId;
    private String recipeUuid;  // 레시피 참조
    private Long tagId;         // 태그 참조
    private String tagName;     // 태그명

//    등록용
    public  RecipeTagDto(String recipeUuid,
                         Long tagId) {
        this.recipeUuid = recipeUuid;
        this.tagId = tagId;
    }
}
