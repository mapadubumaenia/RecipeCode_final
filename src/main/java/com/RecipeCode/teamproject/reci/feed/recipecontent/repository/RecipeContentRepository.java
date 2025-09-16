package com.RecipeCode.teamproject.reci.feed.recipecontent.repository;

import com.RecipeCode.teamproject.reci.feed.recipecontent.entity.RecipeContent;
import com.RecipeCode.teamproject.reci.feed.recipes.entity.Recipes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecipeContentRepository extends JpaRepository<RecipeContent, Long> {

//    레시피 uuid로 조리 단계 리스트 조회
    List<RecipeContent> findByRecipesUuidOrderByStepOrderAsc(String recipesUuid);

//    레시피 삭제시 전부 삭제
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from RecipeContent rc where rc.recipes.uuid = :uuid")
    void deleteByRecipesUuid(@Param("uuid")String recipesUuid);

//    소프트 삭제 제외하고 조회
    @Query(value = "select rc from RecipeContent rc\n"+
                    "where rc.recipes.uuid = :uuid\n"+
                    "and rc.recipes.deleted = false\n"+
                    " order by rc.stepOrder asc")
    List<RecipeContent> findVisibleByRecipeUuid(@Param("uuid") String uuid);
}
