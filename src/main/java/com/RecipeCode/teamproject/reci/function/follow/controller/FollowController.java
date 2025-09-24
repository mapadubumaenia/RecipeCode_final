package com.RecipeCode.teamproject.reci.function.follow.controller;

import com.RecipeCode.teamproject.common.SecurityUtil;
import com.RecipeCode.teamproject.reci.auth.dto.MemberDto;
import com.RecipeCode.teamproject.reci.function.follow.dto.FollowDto;
import com.RecipeCode.teamproject.reci.function.follow.service.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/follow")
public class FollowController {

    private final FollowService followService;
    private final SecurityUtil securityUtil;

    /** 팔로우 */
    @PostMapping("/{userId}")
    public ResponseEntity<String> follow(@PathVariable String userId) {
        String followerEmail = securityUtil.getLoginUser().getUsername(); // 로그인한 사람은 email 기반
        followService.follow(followerEmail, userId); // userId → email 변환 처리
        return ResponseEntity.ok("팔로우 성공");
    }

    /** 언팔로우 */
    @DeleteMapping("/{userId}")
    public ResponseEntity<String> unfollow(@PathVariable String userId) {
        String followerEmail = securityUtil.getLoginUser().getUsername();
        followService.unfollow(followerEmail, userId);
        return ResponseEntity.ok("언팔로우 성공");
    }

    /** 내 팔로잉 목록 */
    @GetMapping("/following")
    public ResponseEntity<Slice<FollowDto>> getMyFollowingList(Pageable pageable) {
        String userEmail = securityUtil.getLoginUser().getUsername();
        return ResponseEntity.ok(followService.getUserFollowingList(userEmail, pageable));
    }

    /** 내 팔로워 목록 */
    @GetMapping("/follower")
    public ResponseEntity<Slice<FollowDto>> getMyFollowerList(Pageable pageable) {
        String userEmail = securityUtil.getLoginUser().getUsername();
        return ResponseEntity.ok(followService.getUserFollowerList(userEmail, pageable));
    }

    /** 특정 유저 팔로잉 목록 */
    @GetMapping("/{userId}/following")
    public ResponseEntity<Slice<FollowDto>> getUserFollowingList(
            @PathVariable String userId, Pageable pageable) {
        return ResponseEntity.ok(followService.getUserFollowingList(userId, pageable));
    }

    /** 특정 유저 팔로워 목록 */
    @GetMapping("/{userId}/follower")
    public ResponseEntity<Slice<FollowDto>> getUserFollowerList(
            @PathVariable String userId, Pageable pageable) {
        return ResponseEntity.ok(followService.getUserFollowerList(userId, pageable));
    }

    /** 내 팔로잉 수 */
    @GetMapping("/following/count")
    public ResponseEntity<Long> getMyFollowingCount() {
        String userEmail = securityUtil.getLoginUser().getUsername();
        return ResponseEntity.ok(followService.getUserFollowingCount(userEmail));
    }

    /** 내 팔로워 수 */
    @GetMapping("/follower/count")
    public ResponseEntity<Long> getMyFollowerCount() {
        String userEmail = securityUtil.getLoginUser().getUsername();
        return ResponseEntity.ok(followService.getUserFollowerCount(userEmail));
    }

    /** 특정 유저 팔로잉 수 */
    @GetMapping("/{userId}/following/count")
    public ResponseEntity<Long> getUserFollowingCount(@PathVariable String userId) {
        return ResponseEntity.ok(followService.getUserFollowingCount(userId));
    }

    /** 특정 유저 팔로워 수 */
    @GetMapping("/{userId}/follower/count")
    public ResponseEntity<Long> getUserFollowerCount(@PathVariable String userId) {
        return ResponseEntity.ok(followService.getUserFollowerCount(userId));
    }

    /** 유저 검색 */
    @GetMapping("/mypage/search")
    public ResponseEntity<List<MemberDto>> searchUsers(
            @RequestParam("keyword") String keyword) {
        return ResponseEntity.ok(followService.searchUsers(keyword));
    }
}
