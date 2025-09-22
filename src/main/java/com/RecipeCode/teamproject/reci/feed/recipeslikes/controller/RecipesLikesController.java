package com.RecipeCode.teamproject.reci.feed.recipeslikes.controller;

import com.RecipeCode.teamproject.common.ErrorMsg;
import com.RecipeCode.teamproject.reci.auth.dto.SecurityUserDto;
import com.RecipeCode.teamproject.reci.feed.recipeslikes.dto.RecipesLikesDto;
import com.RecipeCode.teamproject.reci.feed.recipeslikes.service.RecipesLikesService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recipes")
@Log4j2
public class RecipesLikesController {

    private final RecipesLikesService recipesLikesService;
    private final ErrorMsg errorMsg;

    // 좋아요 토글

    @PostMapping("/{uuid}/like")
    public ResponseEntity<RecipesLikesDto> toggleLike(
            @PathVariable String uuid,
            @AuthenticationPrincipal SecurityUserDto user){
        if (user == null) {
            throw new IllegalArgumentException(errorMsg.getMessage("errors.unauthorized"));
        }
        RecipesLikesDto dto = recipesLikesService.toggleLike(uuid, user.getUsername());
        return ResponseEntity.ok(dto);
    }


}
