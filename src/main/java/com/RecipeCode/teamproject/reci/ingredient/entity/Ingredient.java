package com.RecipeCode.teamproject.reci.ingredient.entity;

import com.RecipeCode.teamproject.common.BaseTimeEntity;
import com.RecipeCode.teamproject.reci.recipes.entity.Recipes;
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
    private Long id;
    @Column(length = 100, nullable = false)
    private String ingredientName;
    @Column(length = 50)
    private String ingredientAmount;
    private Long sortOrder;

// Recipes FK (부모 Recipes 엔티티 참조)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipes_uuid", nullable = false)
    private Recipes recipeUuid;

}
