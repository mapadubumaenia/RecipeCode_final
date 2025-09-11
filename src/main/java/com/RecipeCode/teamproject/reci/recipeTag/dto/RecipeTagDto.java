package com.RecipeCode.teamproject.reci.recipeTag.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class RecipeTagDto {
    private Long tagId;
    private String uuid;
    private String tagName;    // 여러 개 태그 가능하게
}
