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


//    상세조회용 생성자
    public RecipeContentDto(Long stepId,
                            String recipeImageUrl,
                            String stepExplain,
                            Long stepOrder){
        this.stepId = stepId;
        this.recipeImageUrl = recipeImageUrl;
        this.stepExplain = stepExplain;
        this.stepOrder = stepOrder;
    }

//    등록용 생성자
    public RecipeContentDto(String stepExplain,
                            Long stepOrder,
                            String recipes){
        this.stepExplain = stepExplain;
        this.stepOrder = stepOrder;
        this.recipes = recipes;
    }

//    수정용 생성자
    public RecipeContentDto(Long stepId,
                            String stepExplain,
                            Long stepOrder,
                            String recipes){
        this.stepId = stepId;
        this.stepExplain = stepExplain;
        this.stepOrder = stepOrder;
        this.recipes = recipes;
    }

}
