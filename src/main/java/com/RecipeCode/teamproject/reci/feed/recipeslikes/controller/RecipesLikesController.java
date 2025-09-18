package com.RecipeCode.teamproject.reci.feed.recipeslikes.controller;

import com.RecipeCode.teamproject.reci.auth.dto.SecurityUserDto;
import com.RecipeCode.teamproject.reci.feed.recipeslikes.dto.RecipesLikesDto;
import com.RecipeCode.teamproject.reci.feed.recipeslikes.service.RecipesLikesService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/recipes")
public class RecipesLikesController {

    private final RecipesLikesService recipesLikesService;

    // 좋아요 토글

//    @PostMapping("/{uuid}/like")
//    public ResponseEntity<RecipesLikesDto> toggleLike(
//            @PathVariable String uuid,
//            @AuthenticationPrincipal SecurityUserDto user){
//        if (user == null) {
//            throw new RuntimeException("No user logged in");
//        }
//        RecipesLikesDto dto = recipesLikesService.toggleLike(uuid, user.getUsername());
//        return ResponseEntity.ok(dto);
//    }

    // 세션 인증 방어 테스트 컨트롤러
    @PostMapping("/{uuid}/like")
    public ResponseEntity<RecipesLikesDto> toggleLike(
            @PathVariable String uuid,
            HttpSession session){
        String userEmail = (String) session.getAttribute("userEmail");
        if (userEmail == null) throw new RuntimeException("No user logged in");

        RecipesLikesDto dto = recipesLikesService.toggleLike(uuid, userEmail);
        return ResponseEntity.ok(dto);
    }

}
