package com.RecipeCode.teamproject.reci.recipes.dto;

import com.RecipeCode.teamproject.reci.ingredient.dto.IngredientDto;
import com.RecipeCode.teamproject.reci.recipecontent.dto.RecipeContentDto;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class RecipesDto {

    private String uuid;            // PK
    private String userEmail;       // member FK
    private String recipeTitle;
    private String recipeIntro;
    private String recipeCategory;
    private String postStatus;
    private String difficulty;
    private Long cookingTime;
    private String thumbnailUrl;

    private List<RecipeContentDto> contents;
    private List<IngredientDto> ingredients;

    private Long viewCount;
    private Long likeCount;
    private Long reportCount;
    private Long commentCount;

    private LocalDateTime insertTime;
    private LocalDateTime updateTime;
}
