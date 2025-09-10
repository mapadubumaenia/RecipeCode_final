package com.RecipeCode.teamproject.reci.recipes.dto;

import com.RecipeCode.teamproject.reci.recipes.entity.IngredientItem;
import com.RecipeCode.teamproject.reci.recipes.entity.Inquiry;
import jakarta.persistence.Column;
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

    private String uuid;
    private String userEmail;
    private String recipeTitle;
    private String recipeIntro;
    private String recipeCategory;
    private String postStatus;
    private String difficulty;
    private Long cookingTime;

    private List<Inquiry> inquiry;

    private Long viewCount;
    private Long likeCount;
    private Long reportCount;
    private Long commentCount;

    private LocalDateTime insertTime;
    private LocalDateTime updateTime;
}
