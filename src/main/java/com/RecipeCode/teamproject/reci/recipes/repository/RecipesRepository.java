package com.RecipeCode.teamproject.reci.recipes.repository;

import com.RecipeCode.teamproject.reci.recipes.entity.Recipes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecipesRepository extends JpaRepository<Recipes, String> {

//    공개된 레시피만(메인용)
    List<RecipesSummaryView> findByPostStatus(String postStatus);

//    특정 유저의 전체 레시피 (공개/비공개 모두, 마이페이지용)
    List<RecipesSummaryView> findByUserEmail(String userEmail);

//    특정 유저 + 공개 여부
    List<RecipesSummaryView> findByUserEmailAndPostStatus(String userEmail, String postStatus);

}
