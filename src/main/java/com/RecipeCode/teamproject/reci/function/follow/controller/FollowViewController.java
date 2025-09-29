package com.RecipeCode.teamproject.reci.function.follow.controller;

import com.RecipeCode.teamproject.common.MapStruct;
import com.RecipeCode.teamproject.reci.auth.dto.MemberDto;
import com.RecipeCode.teamproject.reci.auth.dto.SecurityUserDto;
import com.RecipeCode.teamproject.reci.auth.entity.Member;
import com.RecipeCode.teamproject.reci.auth.service.MemberService;
import com.RecipeCode.teamproject.reci.feed.recipes.dto.RecipesDto;
import com.RecipeCode.teamproject.reci.function.follow.dto.FollowDto;
import com.RecipeCode.teamproject.reci.function.follow.repository.FollowRepository;
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

@Controller
@RequestMapping("/follow")
@RequiredArgsConstructor
public class FollowViewController {

    private final FollowRepository  followRepository;
    private final FollowService followService;
    private final MemberService memberService;
    private final MapStruct mapStruct;


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
        long followingCount = followService.countFollowingOf(owner);
        long followersCount = followService.countFollowersOf(owner);


        // 오너-뷰어 관계
        boolean isSelf = viewer.getUserEmail().equals(owner.getUserEmail());
        boolean isFollowingOwner = !isSelf &&
                followRepository.existsByFollowerAndFollowing(viewer, owner);

        MemberDto ownerDto = mapStruct.toDto(owner);

        // 프로필 이미지 URL 기본값 세팅
        if (ownerDto.getProfileImageUrl() == null || ownerDto.getProfileImageUrl().isBlank()) {
            ownerDto.setProfileImageUrl("/member/" + owner.getUserId() + "/profile-image");
        }

        model.addAttribute("profileOwner", ownerDto);
        model.addAttribute("profileOwnerEmail", ownerDto.getUserEmail());
        model.addAttribute("profileOwnerId", ownerDto.getUserId());

        model.addAttribute("isSelf", isSelf);
        model.addAttribute("isFollowingOwner", isFollowingOwner);

        model.addAttribute("followingCount", followingCount);
        model.addAttribute("followersCount", followersCount);
        return "function/Follow/follow_network";
    }

    // 내 팔로잉/팔로워로 바로 이동
    @GetMapping("/network/me")
    public String myNetwork(@AuthenticationPrincipal SecurityUserDto principal) {
        if (principal == null) return "redirect:/auth/login";
        Member me = memberService.getByUserEmail(principal.getUsername());
        if (me == null) return "redirect:/auth/login";

        // userId는 DB에 "@userid" 형태일 수 있으니 앞의 @ 제거
        String uid = me.getUserId();
        if (uid != null && uid.startsWith("@")) uid = uid.substring(1);

        // /follow/network/{userId} 로 리다이렉트
        return "redirect:/follow/network/" + java.net.URLEncoder.encode(uid, java.nio.charset.StandardCharsets.UTF_8);
    }




}
