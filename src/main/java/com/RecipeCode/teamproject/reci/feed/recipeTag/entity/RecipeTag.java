package com.RecipeCode.teamproject.reci.feed.recipeTag.entity;

import com.RecipeCode.teamproject.common.BaseTimeEntity;
import com.RecipeCode.teamproject.reci.feed.recipes.entity.Recipes;
import com.RecipeCode.teamproject.reci.tag.entity.Tag;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "RECIPE_TAG")
@SequenceGenerator(
        name = "RECIPE_TAG_JPA",
        sequenceName = "RECIPE_TAG_KEY",
        allocationSize = 1)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(of = "recipeTagId", callSuper = false)
public class RecipeTag extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE
            , generator = "RECIPE_TAG_JPA")
    private Long recipeTagId;               //pk

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UUID")
    private Recipes recipes;                // fk

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TAG_ID")
    private Tag tag;
}
