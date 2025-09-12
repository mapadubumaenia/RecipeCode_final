package com.RecipeCode.teamproject.reci.feed.comments.repository;

import com.RecipeCode.teamproject.reci.feed.comments.entity.Comments;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentsRepository extends JpaRepository<Comments, Long> {

    int countByRecipes_Uuid(String recipeUuid);

    int countByParentId_CommentsId(Long commentsId);

    // 특정 레시피의 댓글 조회 (부모댓글 기준)
    List<Comments> findByRecipesUuidAndParentIdIsNull(String recipeUuid, Pageable pageable);

    // 특정 댓글의 자식 댓글 조회
    List<Comments> findByParentIdCommentsId(Long parentId);
}
