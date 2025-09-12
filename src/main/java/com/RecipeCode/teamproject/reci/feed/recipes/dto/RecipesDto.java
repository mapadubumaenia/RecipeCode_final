package com.RecipeCode.teamproject.reci.feed.recipes.dto;

import com.RecipeCode.teamproject.reci.feed.ingredient.dto.IngredientDto;
import com.RecipeCode.teamproject.reci.recipecontent.dto.RecipeContentDto;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
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
    private List<String> tags;                  // 태그 배열

    private Long viewCount;
    private Long likeCount;
    private Long reportCount;
    private Long commentCount;

    private LocalDateTime insertTime;
    private LocalDateTime updateTime;
}
