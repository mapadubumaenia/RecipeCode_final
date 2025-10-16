package com.RecipeCode.teamproject.reci.feed.comments.repository;

import com.RecipeCode.teamproject.reci.feed.comments.entity.Comments;
import org.springframework.data.domain.Page;
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
    Page<Comments> findByRecipesUuidAndParentIdIsNull(String recipeUuid, Pageable pageable);

    // 특정 댓글의 자식 댓글 조회
    List<Comments> findByParentIdCommentsId(Long parentId);

    List<Comments> findByRecipesUuidAndParentIdIsNullAndDeletedAtIsNull(String recipeUuid, Pageable pageable);
    List<Comments> findByDeletedAtIsNull();

    @Query(value = "select c.recipes.uuid as uuid, count(c.commentsId) as cnt\n" +
                   "from Comments c\n" +
                   "where c.recipes.uuid in :uuids\n" +
                   "group by c.recipes.uuid")
    List<CommentCountView> countByRecipeUuids(@Param("uuids") List<String> uuids);

    interface CommentCountView {
        String getUuid();
        long getCnt();
    }
}
