package com.RecipeCode.teamproject.reci.function.follow.controller;

import com.RecipeCode.teamproject.common.MapStruct;
import com.RecipeCode.teamproject.reci.auth.dto.MemberDto;
import com.RecipeCode.teamproject.reci.auth.dto.SecurityUserDto;
import com.RecipeCode.teamproject.reci.auth.entity.Member;
import com.RecipeCode.teamproject.reci.auth.service.MemberService;
import com.RecipeCode.teamproject.reci.feed.recipes.dto.RecipesDto;
import com.RecipeCode.teamproject.reci.function.follow.dto.FollowDto;
import com.RecipeCode.teamproject.reci.function.follow.service.FollowService;
import com.RecipeCode.teamproject.reci.function.follow.service.ProfileFeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/follow")
@RequiredArgsConstructor
public class FollowViewController {

    private final FollowService followService;
    private final MemberService memberService;
    private final ProfileFeedService profileFeedService;
    private final MapStruct mapStruct;

    // 특정 유저 프로필 페이지
    @GetMapping("/profile/{userId:.+}")
    public String profilePage(@PathVariable String userId,
                              @AuthenticationPrincipal SecurityUserDto principal,
                              @PageableDefault(size = 10) Pageable pageable,
                              Model model) {

        // 1) 로그인 체크
        if (principal == null) return "redirect:/auth/login";

        // 로그인한 유저
        Member viewer = memberService.getByUserEmail(principal.getUsername());
        if (viewer == null) return "redirect:/auth/login";
        model.addAttribute("viewer", mapStruct.toDto(viewer));
        model.addAttribute("currentUserEmail", viewer.getUserEmail());

        // 2) owner 조회 (pathVariable 기반)
        String lookupId = userId.startsWith("@") ? userId : "@" + userId;
        Member owner = memberService.getByUserId(lookupId);
        if (owner == null) {
            model.addAttribute("message", "존재하지 않는 사용자입니다.");
            return "error/404";
        }

        MemberDto ownerDto = mapStruct.toDto(owner);
        model.addAttribute("user", ownerDto);

        // 여기서 이메일 한 번만 꺼내고 이후 계속 사용
        String ownerEmail = owner.getUserEmail();

        // 3) 작성한 레시피 목록 (userEmail 기준)
        Slice<RecipesDto> posts = profileFeedService.getUserRecipesByEmail(ownerEmail, pageable);
        model.addAttribute("posts", posts.getContent());
        model.addAttribute("postsHasNext", posts.hasNext());

        // 4) 팔로워 / 팔로잉 목록 (userEmail 기준)
        Slice<FollowDto> followers = followService.getUserFollowerList(owner.getUserId(), pageable);
        Slice<FollowDto> followings = followService.getUserFollowingList(owner.getUserId(), pageable);

        model.addAttribute("followers", followers.getContent());
        model.addAttribute("followersHasNext", followers.hasNext());
        model.addAttribute("followings", followings.getContent());
        model.addAttribute("followingsHasNext", followings.hasNext());

        return "profile/profile_feed";
    }


    // my 팔로잉/필로워 목록 페이지

    @GetMapping("/network/{userId:.+}")
    public String networkPage(@PathVariable String userId,
                              @AuthenticationPrincipal SecurityUserDto principal,
                              @PageableDefault(size = 10) Pageable pageable,
                              Model model) {

        // 1) 로그인 체크
        if (principal == null) return "redirect:/auth/login";

        // viewer
        Member viewer = memberService.getByUserEmail(principal.getUsername());
        if (viewer == null) return "redirect:/auth/login";
        model.addAttribute("viewer", mapStruct.toDto(viewer));
        model.addAttribute("currentUserEmail", viewer.getUserEmail());

        // owner (path의 userId 사용!)  ← @ 제거
        String lookupId = userId.startsWith("@") ? userId : "@" + userId;
        Member owner = memberService.getByUserId(lookupId);
        if (owner == null) {
            model.addAttribute("message", "존재하지 않는 사용자입니다.");
            return "error/404";
        }

        MemberDto ownerDto = mapStruct.toDto(owner);
//        var followers  = followService.getUserFollowerList(owner.getUserId(), pageable);
//        var followings = followService.getUserFollowingList(owner.getUserId(), pageable);

        model.addAttribute("profileOwner", ownerDto);
        model.addAttribute("profileOwnerEmail", owner.getUserEmail());
        model.addAttribute("profileOwnerId", owner.getUserId());
//        model.addAttribute("followers", followers.getContent());
//        model.addAttribute("followersHasNext", followers.hasNext());
//        model.addAttribute("followings", followings.getContent());
//        model.addAttribute("followingsHasNext", followings.hasNext());
        return "function/Follow/follow_network";
    }

}
