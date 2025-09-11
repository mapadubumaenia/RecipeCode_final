package com.RecipeCode.teamproject.reci.feed.recipeTag.dto;


import lombok.*;

import java.util.List;

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

}
