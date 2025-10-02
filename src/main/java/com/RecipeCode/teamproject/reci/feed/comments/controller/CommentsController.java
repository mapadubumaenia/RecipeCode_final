package com.RecipeCode.teamproject.reci.feed.comments.controller;

import com.RecipeCode.teamproject.common.SecurityUtil;
import com.RecipeCode.teamproject.reci.auth.dto.SecurityUserDto;
import com.RecipeCode.teamproject.reci.feed.comments.dto.CommentsDto;
import com.RecipeCode.teamproject.reci.feed.comments.service.CommentsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/comments")
@Log4j2
public class CommentsController {

    private final CommentsService commentsService;
    private final SecurityUtil securityUtil;

    // 댓글 수
    @GetMapping("/count/{recipeUuid}")
    public ResponseEntity<?> getCommentsCount(@PathVariable String recipeUuid){
        try {
            long count = commentsService.countCommentsByRecipe(recipeUuid);
            return ResponseEntity.ok(Map.of("commentsCount", count));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 댓글 불러오기
    @GetMapping("/{recipeUuid}")
    public ResponseEntity<?> getComments(@PathVariable String recipeUuid,
                                         @RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "10") int size) {
        try {
            // 로그인 여부 확인 (없으면 그냥 null)
            SecurityUserDto loginUser = null;
            try {
                loginUser = securityUtil.getLoginUser();
            } catch (Exception ignored) {
                // 로그인 안 한 경우 그냥 null 유지
            }

            String userEmail = (loginUser != null) ? loginUser.getUsername() : null;

            List<CommentsDto> list = commentsService.countByRecipes_Uuid(recipeUuid, page, size, userEmail);
            return ResponseEntity.ok(list);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 댓글 작성
    @PostMapping("/{recipeUuid}")
    public ResponseEntity<?> saveComment(@RequestBody CommentsDto commentsDto,
                                         @PathVariable String recipeUuid) {
        try {
            SecurityUserDto loginUser = securityUtil.getLoginUser();
            if (loginUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "로그인 후 이용 가능합니다."));
            }
            commentsService.saveComment(commentsDto, recipeUuid, loginUser.getUsername());
            return ResponseEntity.ok(commentsDto);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 대댓글 불러오기
    @GetMapping("/replies/{parentId}")
    public ResponseEntity<?> getReplies(@PathVariable Long parentId) {
        try {
            SecurityUserDto loginUser = null;
            try {
                loginUser = securityUtil.getLoginUser();
            } catch (Exception ignored) {
                // 로그인 안 한 경우 그냥 null
            }

            String userEmail = (loginUser != null) ? loginUser.getUsername() : null;

            return ResponseEntity.ok(commentsService.getReplies(parentId, userEmail));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 대댓글 작성
    @PostMapping("/replies/{parentId}")
    public ResponseEntity<?> saveReply(@PathVariable Long parentId,
                                       @RequestBody CommentsDto commentsDto) {
        try {
            SecurityUserDto loginUser = securityUtil.getLoginUser();
            if (loginUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "로그인 후 이용 가능합니다."));
            }
            commentsService.saveReply(commentsDto, parentId, loginUser.getUsername());
            return ResponseEntity.ok(Map.of("message", "대댓글 작성 완료"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 댓글 수정
    @PatchMapping("/{commentsId}")
    public ResponseEntity<?> updateComment(@PathVariable Long commentsId,
                                           @RequestBody CommentsDto commentsDto) {
        try {
            SecurityUserDto loginUser = securityUtil.getLoginUser();
            if (loginUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "로그인 후 이용 가능합니다."));
            }
            return ResponseEntity.ok(commentsService.updateComment(commentsId, commentsDto, loginUser.getUsername()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 댓글 삭제
    @DeleteMapping("/{commentsId}")
    public ResponseEntity<?> deleteComment(@PathVariable Long commentsId) {
        try {
            SecurityUserDto loginUser = securityUtil.getLoginUser();
            if (loginUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "로그인 후 이용 가능합니다."));
            }
            commentsService.deleteComment(commentsId, loginUser.getUsername());
            return ResponseEntity.ok(Map.of("message", "삭제 완료"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
