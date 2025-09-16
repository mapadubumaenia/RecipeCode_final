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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CommentsServiceTest {

    private CommentsRepository commentsRepository;
    private MemberRepository memberRepository;
    private RecipesRepository recipesRepository;
    private MapStruct mapStruct;
    private ErrorMsg errorMsg;

    private CommentsService service;

    @BeforeEach
    void setUp() {
        commentsRepository = mock(CommentsRepository.class);
        memberRepository = mock(MemberRepository.class);
        recipesRepository = mock(RecipesRepository.class);
        mapStruct = mock(MapStruct.class);
        errorMsg = mock(ErrorMsg.class);

        service = new CommentsService(commentsRepository, memberRepository, recipesRepository, mapStruct, errorMsg);
    }

    @Test
    void countByRecipes_Uuid() {
        Comments comment = new Comments();
        comment.setCommentsId(1L);
        comment.setCommentsContent("테스트 댓글");

        CommentsDto dto = new CommentsDto();
        dto.setCommentsId(1L);
        dto.setCommentsContent("테스트 댓글");

        Recipes recipe = new Recipes();
        recipe.setUuid("recipe-1");

        when(recipesRepository.findById("recipe-1")).thenReturn(Optional.of(recipe));
        when(commentsRepository.findByRecipesUuidAndParentIdIsNull(eq("recipe-1"), any(Pageable.class)))
                .thenReturn(Collections.singletonList(comment));
        when(mapStruct.toDto(comment)).thenReturn(dto);
        when(commentsRepository.countByParentId_CommentsId(1L)).thenReturn(0);

        List<CommentsDto> result = service.countByRecipes_Uuid("recipe-1", 0, 10);

        assertEquals(1, result.size());
        assertEquals("테스트 댓글", result.get(0).getCommentsContent());
        verify(commentsRepository, times(1)).findByRecipesUuidAndParentIdIsNull(eq("recipe-1"), any(Pageable.class));
    }

    @Test
    void saveComment() {
        CommentsDto dto = new CommentsDto();
        dto.setCommentsContent("새 댓글");

        Comments entity = new Comments();
        when(mapStruct.toEntity(dto)).thenReturn(entity);

        Recipes recipe = new Recipes();
        recipe.setUuid("recipe-1");

        Member member = new Member();
        member.setUserEmail("test@test.com");

        when(recipesRepository.findById("recipe-1")).thenReturn(Optional.of(recipe));
        when(memberRepository.findByUserEmail("test@test.com")).thenReturn(Optional.of(member));
        when(commentsRepository.save(entity)).thenReturn(entity);

        service.saveComment(dto, "recipe-1", "test@test.com");

        assertEquals(recipe, entity.getRecipes());
        assertEquals(member, entity.getMember());
        verify(commentsRepository, times(1)).save(entity);
    }

    @Test
    void getReplies() {
        Comments parent = new Comments();
        parent.setCommentsId(10L);

        Comments child = new Comments();
        child.setCommentsId(11L);

        CommentsDto dto = new CommentsDto();
        dto.setCommentsId(11L);

        when(commentsRepository.findByParentIdCommentsId(10L))
                .thenReturn(Collections.singletonList(child));
        when(mapStruct.toDto(child)).thenReturn(dto);

        List<CommentsDto> result = service.getReplies(10L);

        assertEquals(1, result.size());
        assertEquals(11L, result.get(0).getCommentsId());
        verify(commentsRepository, times(1)).findByParentIdCommentsId(10L);
    }

    @Test
    void saveReply() {
        CommentsDto dto = new CommentsDto();
        Comments entity = new Comments();
        when(mapStruct.toEntity(dto)).thenReturn(entity);

        Comments parent = new Comments();
        Recipes recipe = new Recipes();
        parent.setCommentsId(10L);
        parent.setRecipes(recipe);

        when(commentsRepository.findById(10L)).thenReturn(Optional.of(parent));
        when(commentsRepository.save(entity)).thenReturn(entity);

        service.saveReply(dto, 10L);

        assertEquals(parent, entity.getParentId());
        assertEquals(recipe, entity.getRecipes());
        verify(commentsRepository, times(1)).save(entity);
    }

    @Test
    void deleteComment() {
        doNothing().when(commentsRepository).deleteById(1L);

        service.deleteComment(1L);

        verify(commentsRepository, times(1)).deleteById(1L);
    }
}
