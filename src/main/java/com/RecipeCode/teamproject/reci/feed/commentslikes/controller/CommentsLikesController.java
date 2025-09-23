package com.RecipeCode.teamproject.reci.feed.commentslikes.controller;


import com.RecipeCode.teamproject.reci.auth.entity.Member;
import com.RecipeCode.teamproject.reci.feed.comments.entity.Comments;
import com.RecipeCode.teamproject.reci.feed.comments.repository.CommentsRepository;
import com.RecipeCode.teamproject.reci.feed.commentslikes.dto.CommentsLikesDto;
import com.RecipeCode.teamproject.reci.feed.commentslikes.service.CommentsLikesService;
import jakarta.servlet.http.HttpSession;
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

    @PostMapping("/{commentId}")
    public ResponseEntity<?> toggleLike(@PathVariable Long commentId,
                                        HttpSession session) {
        // 세션
        Member member = (Member) session.getAttribute("loginUser");

        // 하드코딩 테스트용 유저
        if (member == null) {
            member = new Member();
            member.setUserEmail("sj12@naver.com");
            session.setAttribute("loginUser", member);
            log.info("테스트용 하드코딩 생성: {}", member);
        }

        // 댓글 확인
        Comments comments = commentsRepository.findById(commentId).orElse(null);
        if (comments == null) {
            return ResponseEntity.badRequest().body("댓글이 존재하지 않습니다.");
        }

        // 좋아요 여부 확인
        boolean hasLiked = commentsLikesService.hasLiked(member, comments);

        if (hasLiked) {
            commentsLikesService.unlike(member, comments);
        } else {
            commentsLikesService.like(member, comments);
        }

        // dto로 응답
        CommentsLikesDto dto = new CommentsLikesDto();
        dto.setCommentsId(comments.getCommentsId());
        dto.setLiked(!hasLiked);
        dto.setLikesCount(commentsLikesService.countLikes(comments));

        return ResponseEntity.ok(dto);
    }
}
