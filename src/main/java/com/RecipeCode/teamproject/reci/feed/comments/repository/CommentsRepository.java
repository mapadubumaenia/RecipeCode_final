package com.RecipeCode.teamproject.reci.feed.comments.repository;

import com.RecipeCode.teamproject.reci.feed.comments.entity.Comments;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentsRepository extends JpaRepository<Comments, Long> {

    @Query("SELECT COALESCE(MAX(c.commentsCount),0)FROM Comments c WHERE c.recipes.uuid = :recipeUuid")
    Long findMaxCommentsCountByRecipe(@Param("recipeUuid") String recipeUuid);

    int countByRecipes_Uuid(String recipeUuid);

    int countByParentId_CommentsId(Long commentsId);

    // 특정 레시피의 댓글 조회 (부모댓글 기준)
    List<Comments> findByRecipesUuidAndParentIdIsNull(String recipeUuid, Pageable pageable);

    // 특정 댓글의 자식 댓글 조회
    List<Comments> findByParentIdCommentsId(Long parentId);

    List<Comments> findByRecipesUuidAndParentIdIsNullAndDeletedAtIsNull(String recipeUuid, Pageable pageable);
    List<Comments> findByParentIdCommentsIdAndDeletedAtIsNull(Long parentId);
    List<Comments> findByDeletedAtIsNull();
}
