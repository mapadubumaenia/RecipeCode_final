package com.RecipeCode.teamproject.reci.feed.commentslikes.repository;

import com.RecipeCode.teamproject.reci.auth.entity.Member;
import com.RecipeCode.teamproject.reci.feed.comments.entity.Comments;
import com.RecipeCode.teamproject.reci.feed.commentslikes.entity.CommentsLikes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentsLikesRepository extends JpaRepository<CommentsLikes, Long> {
    // 눌렀는지 확인하기
    boolean existsByMemberAndComments(Member member, Comments comments);

    CommentsLikes findByMemberAndComments(Member member, Comments comments);

    // 개수
    long countByComments(Comments comments);
}
