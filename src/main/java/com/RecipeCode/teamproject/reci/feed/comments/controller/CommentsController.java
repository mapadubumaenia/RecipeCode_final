package com.RecipeCode.teamproject.reci.feed.comments.controller;

import com.RecipeCode.teamproject.reci.feed.comments.dto.CommentsDto;
import com.RecipeCode.teamproject.reci.feed.comments.service.CommentsService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController      // JSON 반환전용, @ResponseBody 필요없게
@RequiredArgsConstructor
@RequestMapping("/api/comments")
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
        List<CommentsDto> list = commentsService.countByRecipes_Uuid(recipeUuid, page, size);
        log.info("조회된 댓글 수: {}", list.size());
        return list;
    }

    // 댓글 작성
    @PostMapping("/{recipeUuid}")
    public CommentsDto saveComment(@RequestBody CommentsDto commentsDto,
                            @PathVariable String recipeUuid,
                            HttpSession session) {
<<<<<<< Updated upstream
        //세션
        String userEmail = (String) session.getAttribute("userEmail");
        if (userEmail == null) {
            userEmail = "sj12@naver.com";  //하드코딩
            session.setAttribute("userEmail", userEmail);
            throw new RuntimeException("로그인 후 이용 가능합니다.");
        }
=======
//        //세션
//        String userEmail = (String) session.getAttribute("userEmail");
//        if (userEmail == null) throw new RuntimeException("로그인 후 이용 가능합니다.");

        String userEmail = "sj12@naver.com"; // 일단
>>>>>>> Stashed changes

        log.info("댓글 작성: commentsDto={}, recipeUuid={}, userEmail={}", commentsDto, recipeUuid, userEmail);
        commentsService.saveComment(commentsDto, recipeUuid, userEmail);
        return commentsDto;
    }



    // 대댓글 불러오기
    @GetMapping("/replies/{parentId}")
    public List<CommentsDto> getReplies(@PathVariable Long parentId) {
        log.info("대댓글 조회: parentId={}", parentId);
        return commentsService.getReplies(parentId);
    }

    // 대댓작성
    @PostMapping("/replies/{parentId}")
    public void saveReply(@PathVariable Long parentId,
                          @RequestBody CommentsDto commentsDto,
                          HttpSession session) {
        //세션
        String userEmail = (String) session.getAttribute("userEmail");
        if (userEmail == null) {
            userEmail = "sj12@naver.com";  //하드코딩
            session.setAttribute("userEmail", userEmail);

        }
        commentsService.saveReply(commentsDto, parentId, userEmail);
    }



    // 삭제
    @DeleteMapping("/{commentsId}")
    public void deleteComment(@PathVariable Long commentsId) {
        commentsService.deleteComment(commentsId);
    }
}
