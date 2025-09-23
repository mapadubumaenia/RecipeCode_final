package com.RecipeCode.teamproject.reci.feed.comments.service;

import com.RecipeCode.teamproject.common.ErrorMsg;
import com.RecipeCode.teamproject.common.MapStruct;
import com.RecipeCode.teamproject.reci.auth.entity.Member;
import com.RecipeCode.teamproject.reci.auth.repository.MemberRepository;
import com.RecipeCode.teamproject.reci.feed.comments.dto.CommentsDto;
import com.RecipeCode.teamproject.reci.feed.comments.entity.Comments;
import com.RecipeCode.teamproject.reci.feed.comments.repository.CommentsRepository;
import com.RecipeCode.teamproject.reci.feed.recipes.entity.Recipes;
import com.RecipeCode.teamproject.reci.feed.recipes.repository.RecipesRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class CommentsService {
    private final CommentsRepository commentsRepository;
    private final MemberRepository memberRepository;
    private final RecipesRepository recipesRepository;
    private final MapStruct mapStruct;
    private final ErrorMsg errorMsg;

    // 댓글 수 세기
    public long countCommentsByRecipe(String recipeUuid) {
        return commentsRepository.countByRecipes_Uuid(recipeUuid);
    }

    // 댓글 불러오기
    public List<CommentsDto> countByRecipes_Uuid(String recipeUuid, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("insertTime").descending());

        Recipes recipe = recipesRepository.findById(recipeUuid)
                .orElseThrow(() -> new RuntimeException(errorMsg.getMessage("errors.not.found")));

        return commentsRepository.findByRecipesUuidAndParentIdIsNull(recipeUuid, pageable)
                .stream().map(comments -> {
                    CommentsDto dto = mapStruct.toDto(comments);
                    dto.setReplyCount(commentsRepository.countByParentId_CommentsId(comments.getCommentsId()));
                    return dto;
                }).toList();
    }

    // 댓글 작성
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

        commentsRepository.save(comment);
    }


    // 대댓글 불러오기
    public List<CommentsDto> getReplies(Long parentId) {
        return commentsRepository.findByParentIdCommentsId(parentId)
                .stream()
                .map(mapStruct::toDto)
                .toList();
    }

    // 대댓글 작성
    public void saveReply(CommentsDto commentsDto, Long parentId, String userEmail) {
        Comments reply = mapStruct.toEntity(commentsDto);

        Comments parent = commentsRepository.findById(parentId)
                .orElseThrow(() -> new RuntimeException(errorMsg.getMessage("errors.not.found")));
        reply.setParentId(parent);
        reply.setRecipes(parent.getRecipes()); // 같은 레시피 연결

        Member member = memberRepository.findByUserEmail(userEmail)
                .orElseThrow(() -> new RuntimeException(errorMsg.getMessage("errors.not.found")));
        reply.setMember(member);

        commentsRepository.save(reply);
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
        // 수정
        comment.setCommentsContent(commentsDto.getCommentsContent());
        comment.setUpdateTime(java.time.LocalDateTime.now());

        CommentsDto updatedDto = mapStruct.toDto(comment);
        updatedDto.setReplyCount(commentsRepository.countByParentId_CommentsId(commentsId));

        return updatedDto;
    }

    // 댓글/대댓글 삭제
    @Transactional
    public void deleteComment(Long commentsId) {
        commentsRepository.deleteById(commentsId);
    }

}
