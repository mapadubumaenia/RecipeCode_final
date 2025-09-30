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
}
