package com.RecipeCode.teamproject.reci.recipeTag.repository;

import com.RecipeCode.teamproject.reci.recipeTag.entity.RecipeTag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RecipeTagRepository extends JpaRepository<RecipeTag, Long> {
    @Query(value = "select t from RecipeTag t\n" +
            "where t.tagName like %:searchKeyword%")
    Page<RecipeTag> selectRecipeTagList(
            @Param("searchKeyword") String searchKeyword,
            Pageable pageable
    );
}
