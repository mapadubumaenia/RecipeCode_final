package com.RecipeCode.teamproject.reci.recipeTag.dto;

import com.RecipeCode.teamproject.reci.recipes.entity.Recipes;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class RecipeTagDto {
    private Long tagId;
    private Recipes recipes;
    private String tagName;
}
