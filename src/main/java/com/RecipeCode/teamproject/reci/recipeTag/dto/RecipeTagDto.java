package com.RecipeCode.teamproject.reci.recipeTag.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class RecipeTagDto {
    private Long tagId;
    //    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "UUID")
//    private Recipes recipes;
    private String tagName;
}
