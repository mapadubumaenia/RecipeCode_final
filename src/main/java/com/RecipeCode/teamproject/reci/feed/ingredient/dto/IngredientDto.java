package com.RecipeCode.teamproject.reci.feed.ingredient.dto;

import lombok.*;

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

//    상세조회용
    public IngredientDto(Long id,
                         String ingredientName,
                         String ingredientAmount,
                         Long sortOrder) {
        this.id = id;
        this.ingredientName = ingredientName;
        this.ingredientAmount = ingredientAmount;
        this.sortOrder = sortOrder;
    }

//    등록
    public IngredientDto(String ingredientName,
                         String ingredientAmount) {
        this.ingredientName = ingredientName;
        this.ingredientAmount = ingredientAmount;
    }

//    수정
    public IngredientDto(Long id,
                         String ingredientName,
                         String ingredientAmount){
        this.id = id;
        this.ingredientName = ingredientName;
        this.ingredientAmount = ingredientAmount;
    }

}
