package com.RecipeCode.teamproject.reci.feed.commentslikes.service;

import com.RecipeCode.teamproject.common.SecurityUtil;
import com.RecipeCode.teamproject.reci.auth.entity.Member;
import com.RecipeCode.teamproject.reci.auth.repository.MemberRepository;
import com.RecipeCode.teamproject.reci.feed.comments.entity.Comments;
import com.RecipeCode.teamproject.reci.feed.comments.repository.CommentsRepository;
import com.RecipeCode.teamproject.reci.feed.commentslikes.entity.CommentsLikes;
import com.RecipeCode.teamproject.reci.feed.commentslikes.repository.CommentsLikesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentsLikesService {
    private final CommentsLikesRepository commentsLikesRepository;
    private final MemberRepository memberRepository;
    private final SecurityUtil securityUtil;

    // 좋아요 체크
    public boolean hasLiked(Comments comments) {
        String userEmail = securityUtil.getLoginUserEmail();
        Member member = memberRepository.findByUserEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        return commentsLikesRepository.existsByMemberAndComments(member, comments);
    }

    // 좋아요
    @Transactional
    public void like(Comments comments) {
        String userEmail = securityUtil.getLoginUserEmail();
        Member member = memberRepository.findByUserEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        if (!commentsLikesRepository.existsByMemberAndComments(member, comments)) {
            CommentsLikes like = new CommentsLikes();
            like.setMember(member);
            like.setComments(comments);
            commentsLikesRepository.save(like);
        }
    }

    // 좋아요 취소
    @Transactional
    public void unlike(Comments comments) {
        String userEmail = securityUtil.getLoginUserEmail();
        Member member = memberRepository.findByUserEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        CommentsLikes like = commentsLikesRepository.findByMemberAndComments(member, comments);
        if (like != null) {
            commentsLikesRepository.delete(like);
        }
    }

    // 댓글 좋아요 수
    public Long countLikes(Comments comments) {
        return commentsLikesRepository.countByComments(comments);
    }
}

