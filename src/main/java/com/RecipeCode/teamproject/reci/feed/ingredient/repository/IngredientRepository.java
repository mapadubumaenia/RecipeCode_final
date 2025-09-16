package com.RecipeCode.teamproject.reci.feed.ingredient.repository;


import com.RecipeCode.teamproject.reci.feed.ingredient.entity.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IngredientRepository extends JpaRepository<Ingredient, Long> {

    // 삭제 안 된 재료만 조회
    List<Ingredient> findByRecipesUuidAndDeletedFalseOrderBySortOrderAsc(String uuid);

    // 삭제
    @Modifying
    @Query("delete from Ingredient i where i.recipes.uuid = :uuid")
    void deleteByRecipesUuid(@Param("uuid") String uuid);

//    소프트 삭제 제외하고 조회
    @Query(value = "select i from Ingredient i \n" +
            "where i.recipes.uuid = :uuid\n" +
            "and i.recipes.deleted = false\n" +
            "order by i.sortOrder asc")
    List<Ingredient> findVisibleByRecipeUuid(String uuid);

}
