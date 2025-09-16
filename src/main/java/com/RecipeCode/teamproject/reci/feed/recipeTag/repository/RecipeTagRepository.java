package com.RecipeCode.teamproject.reci.feed.recipeTag.repository;

import com.RecipeCode.teamproject.reci.feed.recipeTag.entity.RecipeTag;
import com.RecipeCode.teamproject.reci.feed.recipes.entity.Recipes;
import com.RecipeCode.teamproject.reci.tag.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecipeTagRepository extends JpaRepository<RecipeTag, Long> {

    //    uuid로 찾기
    List<RecipeTag> findByRecipesUuid(String recipesUuid);

    //    존재하는 태그 확인
    boolean existsByRecipesAndTag(Recipes recipes, Tag tag);

    //
    boolean existsByTag(Tag tag);

    @Query("select distinct rt.tag.tagId from RecipeTag rt")
    List<Long> findAllTagIdsInUse();

    //    삭제
    @Modifying
    @Query("delete from RecipeTag rt where rt.recipes.uuid = :uuid")
    void deleteByRecipesUuid(@Param("uuid") String uuid);

    @Query(value = "select rt from RecipeTag rt join fetch rt.tag where rt.recipes.uuid = :uuid\n"+
                    "and rt.recipes.deleted=false")
    List<RecipeTag> findByRecipesUuidWithTag(@Param("uuid") String uuid);

    @Query(value = "select rt from RecipeTag rt\n"+
                    "join fetch rt.tag\n"+
                    "where rt.recipes.uuid = :uuid\n"+
                    "and rt.recipes.deleted = false ")
    List<RecipeTag> findVisibleByRecipesUuid(@Param("uuid") String uuid);

}
