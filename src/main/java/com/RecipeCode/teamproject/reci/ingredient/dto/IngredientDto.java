package com.RecipeCode.teamproject.reci.ingredient.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class IngredientDto {
    private Long id;
    private String ingredientName;
    private String ingredientAmount;
    private Long sortOrder;
    private String recipeUuid;
    private LocalDateTime insertTime;
    private LocalDateTime updateTime;
}
