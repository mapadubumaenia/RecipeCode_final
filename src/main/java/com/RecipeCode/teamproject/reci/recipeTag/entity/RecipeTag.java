package com.RecipeCode.teamproject.reci.recipeTag.entity;

import com.RecipeCode.teamproject.common.BaseTimeEntity;
import com.RecipeCode.teamproject.reci.recipes.entity.Recipes;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "RECIPE_TAG")
@SequenceGenerator(
        name = "SQ_RECIPE_TAG_JPA",
        sequenceName = "RECIPE_TAG_KEY",
        allocationSize = 1)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(of = "tagId", callSuper = false)
public class RecipeTag extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE
            , generator = "SQ_RECIPE_TAG_JPA")
    private Long tagId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UUID")
    private Recipes recipes;
    private String tagName;
}
