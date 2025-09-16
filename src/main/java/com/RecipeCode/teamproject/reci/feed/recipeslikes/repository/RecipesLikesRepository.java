package com.RecipeCode.teamproject.reci.feed.recipeslikes.repository;

import com.RecipeCode.teamproject.reci.auth.entity.Member;
import com.RecipeCode.teamproject.reci.feed.recipes.entity.Recipes;
import com.RecipeCode.teamproject.reci.feed.recipeslikes.entity.RecipesLikes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecipesLikesRepository extends JpaRepository<RecipesLikes, Long> {

//    눌렀는지 확인하기
    boolean existsByMemberAndRecipes(Member member, Recipes recipes);

//    지우기
    void deleteByMemberAndRecipes(Member member, Recipes recipes);

//    갯수세기
    long countByRecipes(Recipes recipes);
}
