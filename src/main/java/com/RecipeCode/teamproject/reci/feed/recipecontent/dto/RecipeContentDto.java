package com.RecipeCode.teamproject.reci.feed.recipecontent.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class RecipeContentDto {
    private Long stepId;                // PK
    private String recipeImageUrl;
    private String stepExplain;
    private Long stepOrder;
    private String recipes;             // FK
}
