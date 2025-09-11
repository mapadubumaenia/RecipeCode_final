package com.RecipeCode.teamproject.reci.recipecontent.entity;

import com.RecipeCode.teamproject.common.BaseTimeEntity;
import com.RecipeCode.teamproject.reci.recipes.entity.Recipes;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode(of = "recipeStip", callSuper = false)
@Builder
@Entity
@Table(name = "RECIPE_CONTENT")
public class RecipeContent extends BaseTimeEntity {

    @Id
    private Long recipeStep;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipes_uuid", nullable = false)
    private Recipes uuid;
    @Lob
    private byte[] recipeImage;
    private String recipeImageUrl;
    @Column(length = 1000, nullable = false)
    private String stepExplain;

//    순서 변경용
    private Long sortOrder;

}
