package com.RecipeCode.teamproject.reci.feed.ingredient.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class IngredientDto {
    private Long id;                    // pk
    private String ingredientName;
    private String ingredientAmount;
    private Long sortOrder;
    private String recipesUuid;             // fk
    private LocalDateTime insertTime;
    private LocalDateTime updateTime;
}
