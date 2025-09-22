package com.RecipeCode.teamproject.reci.function.follow.controller;

import com.RecipeCode.teamproject.reci.auth.dto.MemberDto;
import com.RecipeCode.teamproject.reci.function.follow.service.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
//
//@RestController
//@RequiredArgsConstructor
//@RequestMapping("/api")
//public class FollowController {
//    private final FollowService followService;
//
//    @GetMapping("/mypage/search")
//    public ResponseEntity<List<MemberDto>> searchUsers(
//                                            @RequestParam("keyword") String keyword){
//        List<MemberDto> users = followService.searchUsers(keyword);
//        return ResponseEntity.ok(users);
//    }
//
//    @GetMapping("/mypage/following")
//    public ResponseEntity<List<MemberDto>> getFollowing() {
//        return ResponseEntity.ok(Collections.emptyList());
//    }
//}
