package com.RecipeCode.teamproject.reci.feed.comments.service;

import com.RecipeCode.teamproject.common.ErrorMsg;
import com.RecipeCode.teamproject.common.MapStruct;
import com.RecipeCode.teamproject.reci.auth.entity.Member;
import com.RecipeCode.teamproject.reci.auth.repository.MemberRepository;
import com.RecipeCode.teamproject.reci.auth.service.UserDetailsServiceImpl;
import com.RecipeCode.teamproject.reci.feed.comments.dto.CommentsDto;
import com.RecipeCode.teamproject.reci.feed.comments.entity.Comments;
import com.RecipeCode.teamproject.reci.feed.comments.repository.CommentsRepository;
import com.RecipeCode.teamproject.reci.feed.commentslikes.repository.CommentsLikesRepository;
import com.RecipeCode.teamproject.reci.feed.recipes.entity.Recipes;
import com.RecipeCode.teamproject.reci.feed.recipes.repository.RecipesRepository;
import com.RecipeCode.teamproject.reci.function.commentsReport.repository.CommentReportRepository;
import com.RecipeCode.teamproject.reci.function.notification.enums.NotificationEvent;
import com.RecipeCode.teamproject.reci.function.notification.service.NotificationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.hibernate.annotations.Parent;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
@Log4j2
public class CommentsService {
    private final CommentsRepository commentsRepository;
    private final MemberRepository memberRepository;
    private final RecipesRepository recipesRepository;
    private final CommentsLikesRepository commentsLikesRepository;
    private final CommentReportRepository commentReportRepository;
    private final MapStruct mapStruct;
    private final ErrorMsg errorMsg;
    private final UserDetailsServiceImpl userDetailsServiceImpl;
    private final NotificationService notificationService;


    // 댓글 수 세기
    public long countCommentsByRecipe(String recipeUuid) {
        return commentsRepository.countByRecipes_Uuid(recipeUuid);
    }

    // 댓글 불러오기
    public List<CommentsDto> countByRecipes_Uuid(String recipeUuid, int page, int size, String userEmail) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("insertTime").descending());

        final Member memberFinal = (userEmail != null) ? memberRepository.findByUserEmail(userEmail).orElse(null) : null;

        Page<Comments> commentsPage = commentsRepository
                .findByRecipesUuidAndParentIdIsNull(recipeUuid, pageable);

        return commentsPage.getContent().stream()
                .map(comments -> {
                    CommentsDto dto = mapStruct.toDto(comments);

                    if (comments.getDeletedAt() != null) {
                        dto.setCommentsContent("삭제된 댓글입니다.");
                    }

                    dto.setReplyCount(commentsRepository.countByParentId_CommentsId(comments.getCommentsId()));

                    long likeCount = commentsLikesRepository.countByComments(comments);
                    dto.setLikeCount(likeCount);

                    boolean liked = false;
                    if (memberFinal != null) {
                        liked = commentsLikesRepository.existsByMemberAndComments(memberFinal, comments);
                    }
                    dto.setLiked(liked);

                    boolean reported = false;
                    if (memberFinal != null) {
                        reported = commentReportRepository.existsByComments_CommentsIdAndMember_UserEmail(
                                comments.getCommentsId(), memberFinal.getUserEmail()
                        );
                    }
                    dto.setAlreadyReported(reported);

                    return dto;
                })
                .toList();
    }


    // 댓글 작성
    @Transactional
    public void saveComment(CommentsDto commentsDto, String recipeUuid, String userEmail) {
        Comments comment = mapStruct.toEntity(commentsDto);

        Recipes recipe = recipesRepository.findById(recipeUuid)
                .orElseThrow(() -> new RuntimeException(errorMsg.getMessage("errors.not.found")));
        comment.setRecipes(recipe);

        Member member = memberRepository.findByUserEmail(userEmail)
                .orElseThrow(() -> new RuntimeException(errorMsg.getMessage("errors.not.found")));
        comment.setMember(member);

        if (commentsDto.getParentId() != null) {
            Comments parent = commentsRepository.findById(commentsDto.getParentId())
                    .orElseThrow(() -> new RuntimeException(errorMsg.getMessage("errors.not.found")));
            comment.setParentId(parent);
        }

        Long maxCount = commentsRepository.findMaxCommentsCountByRecipe(recipeUuid);
        comment.setCommentsCount(maxCount + 1);

        Comments saved = commentsRepository.save(comment);

        // 레시피 댓글수 증가
        recipe.incrementCommentCount();
        recipesRepository.save(recipe);

        log.info("댓글 저장 완료, commentsId={}", saved.getCommentsId());

        // 알림 생성
        notificationService.createNotification(
                comment.getMember().getUserEmail(),                 //댓글 작성자
                comment.getRecipes().getMember().getUserEmail(),    //레시피 작성자
                NotificationEvent.COMMENT,                          //이벤트 타입
                "COMMENT",                                          //서비스 타입
                String.valueOf(comment.getCommentsId())           //소스 ID
        );
    }

    // 대댓글 불러오기
    public List<CommentsDto> getReplies(Long parentId, String userEmail) {
        final Member memberFinal = (userEmail != null) ? memberRepository.findByUserEmail(userEmail).orElse(null) : null;

        return commentsRepository.findByParentIdCommentsId(parentId)
                .stream()
                .map(comments -> {
                    CommentsDto dto = mapStruct.toDto(comments);

                    if (comments.getDeletedAt() != null) {
                        dto.setCommentsContent("삭제된 댓글입니다.");
                    }

                    // 좋아요 상태 추가
                    long likeCount = commentsLikesRepository.countByComments(comments);
                    dto.setLikeCount(likeCount);

                    boolean liked = false;
                    if (memberFinal != null) {
                        liked = commentsLikesRepository.existsByMemberAndComments(memberFinal, comments);
                    }
                    dto.setLiked(liked);

                    boolean reported = false;
                    if (memberFinal != null) {
                        reported = commentReportRepository.existsByComments_CommentsIdAndMember_UserEmail(
                                comments.getCommentsId(), memberFinal.getUserEmail()
                        );
                    }
                    dto.setAlreadyReported(reported);

                    return dto;
                })
                .toList();
    }

    // 대댓글 작성
    @Transactional
    public void saveReply(CommentsDto commentsDto, Long parentId, String userEmail) {
        Comments reply = mapStruct.toEntity(commentsDto);

        Comments parent = commentsRepository.findById(parentId)
                .orElseThrow(() -> new RuntimeException(errorMsg.getMessage("errors.not.found")));
        reply.setParentId(parent);
        reply.setRecipes(parent.getRecipes()); // 같은 레시피 연결

        Member member = memberRepository.findByUserEmail(userEmail)
                .orElseThrow(() -> new RuntimeException(errorMsg.getMessage("errors.not.found")));
        reply.setMember(member);

        Long maxCount = commentsRepository.findMaxCommentsCountByRecipe(parent.getRecipes().getUuid());
        reply.setCommentsCount(maxCount + 1);

        commentsRepository.save(reply);

        // 레시피 댓글 수 증가
        Recipes recipe = parent.getRecipes();
        recipe.incrementCommentCount();
        recipesRepository.save(recipe);
    }

    // 댓글 수정
    @Transactional
    public CommentsDto updateComment(Long commentsId, CommentsDto commentsDto, String userEmail) {
        // 조회
        Comments comment = commentsRepository.findById(commentsId)
                .orElseThrow(() -> new RuntimeException(errorMsg.getMessage("errors.not.found")));
        // 작성자 확인
        if (!comment.getMember().getUserEmail().equals(userEmail)) {
            throw new RuntimeException("본인 댓글만 수정할 수 있습니다.");
        }
        // 삭제 여부 확인
        if (comment.getDeletedAt() != null) {
            throw new RuntimeException("삭제된 댓글은 수정할 수 없습니다.");
        }
        // 수정
        comment.setCommentsContent(commentsDto.getCommentsContent());
        comment.setUpdateTime(java.time.LocalDateTime.now());

        CommentsDto updatedDto = mapStruct.toDto(comment);
        updatedDto.setReplyCount(commentsRepository.countByParentId_CommentsId(commentsId));

        return updatedDto;
    }

    // 재정렬(번호 매기기)
    @Transactional
    public void reorderComments(String recipeUuid) {
        List<Comments> comments = commentsRepository.findByRecipesUuidAndParentIdIsNullAndDeletedAtIsNull(recipeUuid, Pageable.unpaged());
        long count = 1;
        for (Comments c : comments) {
            c.setCommentsCount(count++);
        }
        commentsRepository.saveAll(comments);
    }

    // 댓글/대댓글 삭제
    @Transactional
    public void deleteComment(Long commentsId, String userEmail) {
        Comments comments = commentsRepository.findById(commentsId)
                .orElseThrow(() -> new RuntimeException("댓글을 찾을 수 없습니다."));

        // 이미 삭제
        if (comments.getDeletedAt() != null) {
            throw new RuntimeException("이미 삭제된 댓글입니다.");
        }

        // 작성자 확인
        if (!comments.getMember().getUserEmail().equals(userEmail)) {
            throw new RuntimeException("본인 댓글만 삭제할 수 있습니다.");
        }

        comments.setDeletedAt(LocalDateTime.now());
        commentsRepository.save(comments);
    }
}
