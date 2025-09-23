package com.RecipeCode.teamproject.reci.feed.commentslikes.service;

import com.RecipeCode.teamproject.reci.auth.entity.Member;
import com.RecipeCode.teamproject.reci.feed.comments.entity.Comments;
import com.RecipeCode.teamproject.reci.feed.commentslikes.entity.CommentsLikes;
import com.RecipeCode.teamproject.reci.feed.commentslikes.repository.CommentsLikesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentsLikesService {
    private final CommentsLikesRepository commentsLikesRepository;

    // 좋아요 체크
    public boolean hasLiked(Member member, Comments comments) {
        return commentsLikesRepository.existsByMemberAndComments(member, comments);
    }

    // 추가
    @Transactional
    public void like(Member member, Comments comments) {
        if (!hasLiked(member, comments)) {
            CommentsLikes cl = new CommentsLikes();
            cl.setMember(member);
            cl.setComments(comments);
            commentsLikesRepository.save(cl);

            comments.increaseLikeCount();
        }
    }

    // 취소
    @Transactional
    public void unlike(Member member, Comments comments) {
        if (hasLiked(member, comments)) {
            commentsLikesRepository.deleteByMemberAndComments(member, comments);

            comments.decreaseLikeCount();
        }
    }

    // 개수 조회
    public long countLikes(Comments comments) {
        return commentsLikesRepository.countByComments(comments);
    }

}
