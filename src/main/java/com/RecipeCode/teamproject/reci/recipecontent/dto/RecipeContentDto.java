package com.RecipeCode.teamproject.reci.recipecontent.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class RecipeContentDto {
    private Long stepId;
    private String recipeImageUrl;
    private String stepExplain;
    private Long stepOrder;
    private String recipeUuid;
}
