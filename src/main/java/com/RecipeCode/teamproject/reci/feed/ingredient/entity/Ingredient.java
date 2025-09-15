package com.RecipeCode.teamproject.reci.feed.ingredient.entity;

import com.RecipeCode.teamproject.common.BaseTimeEntity;
import com.RecipeCode.teamproject.common.BooleanToYNConverter;
import com.RecipeCode.teamproject.reci.feed.recipes.entity.Recipes;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode(of = "id", callSuper = false)
@Builder
@Entity
@Table(name = "INGREDIENT")
@SequenceGenerator(name = "INGREDIENT_KEY_JPA",
                   sequenceName = "INGREDIENT_KEY",
                   allocationSize = 1)
public class Ingredient extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
    generator = "INGREDIENT_KEY_JPA")
    private Long id;                                    // pk
    private String ingredientName;
    private String ingredientAmount;
    private Long sortOrder;

// Recipes FK (부모 Recipes 엔티티 참조)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RECIPE_UUID", referencedColumnName="UUID", nullable = false)        // uuid
    private Recipes recipes;

    @Convert(converter = BooleanToYNConverter.class)
    private boolean deleted;

}
