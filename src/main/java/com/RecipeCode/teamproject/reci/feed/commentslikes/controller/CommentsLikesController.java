package com.RecipeCode.teamproject.reci.feed.commentslikes.controller;


import com.RecipeCode.teamproject.common.SecurityUtil;
import com.RecipeCode.teamproject.reci.auth.dto.SecurityUserDto;
import com.RecipeCode.teamproject.reci.feed.comments.entity.Comments;
import com.RecipeCode.teamproject.reci.feed.comments.repository.CommentsRepository;
import com.RecipeCode.teamproject.reci.feed.commentslikes.dto.CommentsLikesDto;
import com.RecipeCode.teamproject.reci.feed.commentslikes.service.CommentsLikesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController      // JSON 반환 전용, @ResponseBody 불필요
@RequiredArgsConstructor
@RequestMapping("/comments/likes")
@Log4j2
public class CommentsLikesController {
    private final CommentsLikesService commentsLikesService;
    private final CommentsRepository commentsRepository;
    private final SecurityUtil securityUtil;

    @PostMapping("/{commentId}")
    public ResponseEntity<?> toggleLike(@PathVariable Long commentId) {
        SecurityUserDto loginUser = securityUtil.getLoginUser();
        String userEmail = loginUser.getUsername();

        // 댓글 확인
        Comments comments = commentsRepository.findById(commentId).orElse(null);
        if (comments == null) {
            return ResponseEntity.badRequest().body("댓글이 존재하지 않습니다.");
        }

        // 좋아요 여부 확인
        boolean hasLiked = commentsLikesService.hasLiked(comments);

        if (hasLiked) {
            commentsLikesService.unlike(comments);
        } else {
            commentsLikesService.like(comments);
        }

        // dto로 응답
        CommentsLikesDto dto = new CommentsLikesDto();
        dto.setCommentsId(comments.getCommentsId());
        dto.setLiked(!hasLiked);
        dto.setLikesCount(commentsLikesService.countLikes(comments));

        return ResponseEntity.ok(dto);
    }
}
