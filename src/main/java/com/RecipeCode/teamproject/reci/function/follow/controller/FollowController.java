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
    @PostMapping("/{followingEmail}")
    public ResponseEntity<String> follow(@PathVariable String followingEmail) {
        String followerEmail = securityUtil.getLoginUser().getUsername();
        followService.follow(followerEmail, followingEmail);
        return ResponseEntity.ok("팔로우 성공");
    }

    /** 언팔로우 */
    @DeleteMapping("/{followingEmail}")
    public ResponseEntity<String> unfollow(@PathVariable String followingEmail) {
        String followerEmail = securityUtil.getLoginUser().getUsername();
        followService.unfollow(followerEmail, followingEmail);
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
    @GetMapping("/{userEmail}/following")
    public ResponseEntity<Slice<FollowDto>> getUserFollowingList(
            @PathVariable String userEmail, Pageable pageable) {
        return ResponseEntity.ok(followService.getUserFollowingList(userEmail, pageable));
    }

    /** 특정 유저 팔로워 목록 */
    @GetMapping("/{userEmail}/follower")
    public ResponseEntity<Slice<FollowDto>> getUserFollowerList(
            @PathVariable String userEmail, Pageable pageable) {
        return ResponseEntity.ok(followService.getUserFollowerList(userEmail, pageable));
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
    @GetMapping("/{userEmail}/following/count")
    public ResponseEntity<Long> getUserFollowingCount(@PathVariable String userEmail) {
        return ResponseEntity.ok(followService.getUserFollowingCount(userEmail));
    }

    /** 특정 유저 팔로워 수 */
    @GetMapping("/{userEmail}/follower/count")
    public ResponseEntity<Long> getUserFollowerCount(@PathVariable String userEmail) {
        return ResponseEntity.ok(followService.getUserFollowerCount(userEmail));
    }

    // 서치바 검색 ㅇㅇ
    @GetMapping("/mypage/search")
    public ResponseEntity<List<MemberDto>> searchUsers(
            @RequestParam("keyword") String keyword){
        List<MemberDto> users = followService.searchUsers(keyword);
        return ResponseEntity.ok(users);
    }

}