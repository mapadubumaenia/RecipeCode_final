package com.RecipeCode.teamproject.reci.feed.comments.controller;

import com.RecipeCode.teamproject.reci.feed.comments.dto.CommentsDto;
import com.RecipeCode.teamproject.reci.feed.comments.service.CommentsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController      // JSON 반환전용, @ResponseBody 필요없게
@RequiredArgsConstructor
@RequestMapping("/comments")
@Log4j2
public class CommentsController {
    private final CommentsService commentsService;

    // 댓글 불러오기
    @GetMapping("/{recipeUuid}")
    public List<CommentsDto> getComments(@PathVariable String recipeUuid,
                                         @RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "10") int size,
                                         @RequestParam(defaultValue = "desc") String sort) {
        log.info("댓글 조회: recipeUuid={}, page={}, size={}, sort={}", recipeUuid, page, size, sort);
        return commentsService.countByRecipes_Uuid(recipeUuid, page, size);
    }

    // 댓글 작성
    @PostMapping("/{recipeUuid}")
    public void saveComment(@RequestParam CommentsDto commentsDto,
                            @PathVariable String recipeUuid,
                            @RequestParam String userEmail) {
        log.info("댓글 작성: commentsDto={}, recipeUuid={}, userEmail={}", commentsDto, recipeUuid, userEmail);
        commentsService.saveComment(commentsDto, recipeUuid, userEmail);
    }

    // 대댓글 불러오기
    @GetMapping("/replies/{parentId}")
    public void getReplies(@PathVariable Long parentId) {
        log.info("대댓글 조회: parentId={}", parentId);
        commentsService.getReplies(parentId);
    }

    // 대댓작성
    @PostMapping("/replies/{parentId}")
    public void saveReply(@PathVariable Long parentId,
                          @RequestBody CommentsDto commentsDto) {
        log.info("대댓글 작성: parentId={}", parentId);
        commentsService.saveReply(commentsDto, parentId);
    }

    // 삭제
    @DeleteMapping("/{commentsId}")
    public void deleteComment(@PathVariable Long commentsId) {
        commentsService.deleteComment(commentsId);
    }
}
