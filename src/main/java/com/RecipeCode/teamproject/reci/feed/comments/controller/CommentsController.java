package com.RecipeCode.teamproject.reci.feed.comments.controller;

import com.RecipeCode.teamproject.common.SecurityUtil;
import com.RecipeCode.teamproject.reci.auth.dto.SecurityUserDto;
import com.RecipeCode.teamproject.reci.feed.comments.dto.CommentsDto;
import com.RecipeCode.teamproject.reci.feed.comments.service.CommentsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController      // JSON 반환 전용, @ResponseBody 불필요
@RequiredArgsConstructor
@RequestMapping("/comments")
@Log4j2
public class CommentsController {

    private final CommentsService commentsService;
    private final SecurityUtil securityUtil;

    // 댓글 수
    @GetMapping("/count/{recipeUuid}")
    public ResponseEntity<?> getCommentsCount(@PathVariable String recipeUuid){
        long count = commentsService.countCommentsByRecipe(recipeUuid);
        return ResponseEntity.ok().body(Map.of("commentsCount",count));
    }

    // 댓글 불러오기
    @GetMapping("/{recipeUuid}")
    public List<CommentsDto> getComments(@PathVariable String recipeUuid,
                                         @RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "10") int size) {

        SecurityUserDto loginUser = securityUtil.getLoginUser();
        String userEmail = loginUser != null ? loginUser.getUsername() : null;

        log.info("댓글 조회: recipeUuid={}, page={}, size={},userEmail={}", recipeUuid, page, size, userEmail);
        List<CommentsDto> list = commentsService.countByRecipes_Uuid(recipeUuid, page, size, userEmail);
        log.info("조회된 댓글 수: {}", list.size());
        return list;
    }

    // 댓글 작성
    @PostMapping("/{recipeUuid}")
    public CommentsDto saveComment(@RequestBody CommentsDto commentsDto,
                                   @PathVariable String recipeUuid) {
        // 세션 확인
        SecurityUserDto loginUser = securityUtil.getLoginUser();
        if (loginUser == null) {
            throw new RuntimeException("로그인 후 이용 가능합니다.");
        }
        String userEmail = loginUser.getUsername();

        log.info("댓글 작성: commentsDto={}, recipeUuid={}, userEmail={}", commentsDto, recipeUuid, userEmail);
        commentsService.saveComment(commentsDto, recipeUuid, userEmail);
        return commentsDto;
    }

    // 대댓글 불러오기
    @GetMapping("/replies/{parentId}")
    public List<CommentsDto> getReplies(@PathVariable Long parentId) {
        SecurityUserDto loginUser = securityUtil.getLoginUser();
        String userEmail = loginUser != null ? loginUser.getUsername() : null;

        log.info("대댓글 조회: parentId={}, userEmail={}", parentId, userEmail);
        return commentsService.getReplies(parentId, userEmail);
    }

    // 대댓글 작성
    @PostMapping("/replies/{parentId}")
    public void saveReply(@PathVariable Long parentId,
                          @RequestBody CommentsDto commentsDto) {
        // 세션 확인
        SecurityUserDto loginUser = securityUtil.getLoginUser();
        if (loginUser == null) {
            throw new RuntimeException("로그인 후 이용 가능합니다.");
        }
        String userEmail = loginUser.getUsername();

        commentsService.saveReply(commentsDto, parentId, userEmail);
    }

    // 댓글 수정
    @PatchMapping("/{commentsId}")
    public CommentsDto updateComment(@PathVariable Long commentsId,
                                     @RequestBody CommentsDto commentsDto) {
        // 세션 확인
        SecurityUserDto loginUser = securityUtil.getLoginUser();
        if (loginUser == null) {
            throw new RuntimeException("로그인 후 이용 가능합니다.");
        }
        String userEmail = loginUser.getUsername();

        log.info("댓글 수정 요청: commentsDto={}, 내용={}, userEmail={}", commentsId, commentsDto.getCommentsContent(), userEmail);
        return commentsService.updateComment(commentsId,commentsDto, userEmail);
    }

    // 댓글 삭제
    @DeleteMapping("/{commentsId}")
    public void deleteComment(@PathVariable Long commentsId) {
        // 로그인 확인
        SecurityUserDto loginUser = securityUtil.getLoginUser();
        if (loginUser == null) {
            throw new RuntimeException("로그인 후 이용 가능합니다.");
        }
        String userEmail = loginUser.getUsername();

        commentsService.deleteComment(commentsId, userEmail);
    }


    // 관리자 삭제
    @DeleteMapping("/admin/{commentsId}")
    public void adminDeleteComment(@PathVariable Long commentsId) {
        commentsService.adminDeleteComment(commentsId);
    }
}
