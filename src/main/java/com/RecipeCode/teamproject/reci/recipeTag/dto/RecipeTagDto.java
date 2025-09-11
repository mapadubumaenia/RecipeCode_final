package com.RecipeCode.teamproject.reci.recipeTag.dto;

import com.RecipeCode.teamproject.reci.recipes.entity.Recipes;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class RecipeTagDto {
    private Long tagId;
    private String uuid;
    private List<String> tagName;    // 여러 개 태그 가능하게
}
