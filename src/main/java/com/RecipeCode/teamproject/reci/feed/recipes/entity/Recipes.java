package com.RecipeCode.teamproject.reci.feed.recipes.entity;

import com.RecipeCode.teamproject.common.BaseTimeEntity;
import com.RecipeCode.teamproject.reci.auth.entity.Member;
import com.RecipeCode.teamproject.reci.feed.ingredient.entity.Ingredient;
import com.RecipeCode.teamproject.reci.feed.recipecontent.entity.RecipeContent;
import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode(of = "uuid", callSuper = false)
@Entity
@Table(name = "RECIPES")
public class Recipes extends BaseTimeEntity {

    @Id
    private String uuid;                        // 기본키

    private String recipeTitle;
    private String recipeIntro;
    private String recipeCategory;
    private Long cookingTime;

    @Lob
    private byte[] thumbnail;
    private String thumbnailUrl;

    private String postStatus;           // 공개여부
    private Long viewCount;               // 조회수
    private Long likeCount;               // 좋아요
    private Long reportCount;             // 신고수
    private Long commentCount;            // 댓글수

    private String difficulty;                  // 난이도


    @OneToMany(mappedBy = "recipes", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<Ingredient> ingredients = new ArrayList<>();

//  RecipeContent
//  recipesRepository.save() 할 때 contents도 같이 저장
//  저장 시에는 RecipeContent에 반드시 setRecipes(recipe)로 부모를 지정
//  cascade = CascadeType.ALL : 부모 엔티티에 수행한 작업을 자식 엔티티에도 전파
//  orphanRemoval = true : 부모 엔티티가 사라지면 자식을 자동 삭제
    @OneToMany(mappedBy = "recipes", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("stepOrder ASC")
    private List<RecipeContent> contents = new ArrayList<>();

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "userEmail", nullable = false)
  private Member member;


}
