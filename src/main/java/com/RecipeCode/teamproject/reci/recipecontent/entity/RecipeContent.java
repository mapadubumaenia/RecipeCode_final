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
@EqualsAndHashCode(of = "stepId", callSuper = false)
@Builder
@Entity
@Table(name = "RECIPE_CONTENT")
public class RecipeContent extends BaseTimeEntity {

    @Id
    private Long stepId;                        // 기본키

    @Lob
    private byte[] recipeImage;
    private String recipeImageUrl;
    @Column(length = 1000, nullable = false)
    private String stepExplain;
//    순서 변경용
    private Long stepOrder;

//  레시피 참조키
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipes_uuid", nullable = false)
    private Recipes uuid;
}
