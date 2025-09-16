package com.RecipeCode.teamproject.reci.feed.recipecontent.entity;

import com.RecipeCode.teamproject.common.BaseTimeEntity;
import com.RecipeCode.teamproject.reci.feed.recipes.entity.Recipes;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode(of = "stepId", callSuper = false)
@Entity
@Table(name = "RECIPE_CONTENT")
@SequenceGenerator(name = "CONTENT_KEY_JPA",
                   sequenceName = "CONTENT_KEY",
                   allocationSize = 1)
public class RecipeContent extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
    generator = "CONTENT_KEY_JPA")
    private Long stepId;                        // 기본키

    @Lob
    private byte[] recipeImage;
    private String recipeImageUrl;
    private String stepExplain;
//    순서 변경용
    private Long stepOrder;

//  레시피 참조키
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UUID", nullable = false)
    private Recipes recipes;
}
