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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Controller
@RequiredArgsConstructor
public class ProfileFeedViewController {

    private final MapStruct mapStruct;
    private final ProfileFeedService profileFeedService;
    private final FollowService followService;
    private final MemberService memberService;
    private final FollowRepository followRepository;

    // 특정 유저 프로필 페이지
    @GetMapping("/profile/{userId:.+}")
    public String profilePage(@PathVariable String userId,
                              @AuthenticationPrincipal SecurityUserDto principal,
                              @PageableDefault(size = 5) Pageable pageable,
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

        // 5)

        Set<String> userIds = new HashSet<String>();
        for (FollowDto f : followings.getContent()) {
            userIds.add(f.getMember().getUserId());
        }
        for (FollowDto f : followers.getContent()) {
            userIds.add(f.getMember().getUserId());
        }

        Map<String, Long> followerCounts = new HashMap<String, Long>();
        Map<String, Long> followingCounts = new HashMap<String, Long>();

        for (String uid : userIds) {
            String idForLookup = uid.startsWith("@") ? uid : "@" + uid;
            Member m = memberService.getByUserId(idForLookup);
            if (m != null) {
                long followersCount = followService.countFollowersOf(m);
                long followingCount = followService.countFollowingOf(m);
                followerCounts.put(uid, followersCount);
                followingCounts.put(uid, followingCount);
            }
        }

        model.addAttribute("followerCounts", followerCounts);
        model.addAttribute("followingCounts", followingCounts);

        return "profile/profile_feed";
    }
}
