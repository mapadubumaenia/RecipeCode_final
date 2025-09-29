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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
import java.util.stream.Collectors;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recipes")
@Log4j2
public class RecipesLikesController {

    private final RecipesLikesService recipesLikesService;
    private final ErrorMsg errorMsg;

    // 좋아요 토글

    @RequestMapping(path = "/{uuid}/like",
                    method = {RequestMethod.POST, RequestMethod.DELETE})
    public ResponseEntity<RecipesLikesDto> toggleLike(
            @PathVariable String uuid,
            @AuthenticationPrincipal SecurityUserDto user){
        if (user == null) {
            throw new IllegalArgumentException(errorMsg.getMessage("errors.unauthorized"));
        }
        RecipesLikesDto dto = recipesLikesService.toggleLike(uuid, user.getUsername());
        return ResponseEntity.ok(dto);
    }

    // [NEW] 단건 좋아요 상태: { liked: true/false }
    @GetMapping("/{uuid}/like/status")
    public ResponseEntity<Map<String, Object>> likeStatus(
            @PathVariable String uuid,
            @AuthenticationPrincipal SecurityUserDto user) {

        boolean liked = false;
        if (user != null) {
            liked = recipesLikesService.isLiked(uuid, user.getUsername());
        }
        return ResponseEntity.ok(Map.of("liked", liked));
    }

    // [NEW] 배치 좋아요 상태: { map: { "uuid1": true, "uuid2": false, ... } }
    @GetMapping("/likes/mine")
    public ResponseEntity<Map<String, Object>> likeStatusBatch(
            @RequestParam("ids") String ids,
            @AuthenticationPrincipal SecurityUserDto user) {

        Map<String, Boolean> map = Collections.emptyMap();
        if (user != null) {
            List<String> uuids = Arrays.stream(ids.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .distinct()
                    .collect(Collectors.toList());
            map = recipesLikesService.likedMapFor(user.getUsername(), uuids);
        }
        return ResponseEntity.ok(Map.of("map", map));
    }



}
