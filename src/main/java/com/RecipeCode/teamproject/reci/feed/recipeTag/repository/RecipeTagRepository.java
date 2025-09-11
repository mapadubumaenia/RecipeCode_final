package com.RecipeCode.teamproject.reci.feed.recipeTag.repository;

import com.RecipeCode.teamproject.reci.feed.recipeTag.entity.RecipeTag;
import com.RecipeCode.teamproject.reci.feed.recipes.entity.Recipes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecipeTagRepository extends JpaRepository<RecipeTag, Long> {

//    @Query(value = "select t from RecipeTag t\n" +
//            "where t.tagName like %:searchKeyword%")
//    Page<RecipeTag> selectRecipeTagList(
//            @Param("searchKeyword") String searchKeyword,
//            Pageable pageable
//    );

//    특정 레시피에 달린 모든 태그 조회
    List<RecipeTag> findByRecipes(Recipes recipes);

//    레시피 uuid 기준으로 태그 조회
    List<RecipeTag> findByRecipes_Uuid(String recipeUuid);

//    태그 id 기준으로 레시피 조회(검색 기능용)
    List<RecipeTag> findByTag_TagId(Long tagId);

//    태그명으로 조회
    List<RecipeTag> findByTag_Tag(String tagName);
}
