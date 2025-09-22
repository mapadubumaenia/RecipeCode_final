package com.RecipeCode.teamproject.reci.function.follow.controller;

import com.RecipeCode.teamproject.reci.function.follow.dto.FollowDto;
import com.RecipeCode.teamproject.reci.function.follow.service.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/follow")
@RequiredArgsConstructor
public class FollowViewController {

    private final FollowService followService;

    // 팔로워 + 팔로잉 페이지
    @GetMapping("/{userEmail}")
    public String followPage(@PathVariable String userEmail,
                             @PageableDefault(size = 10) Pageable pageable,
                             Model model) {
        Slice<FollowDto> followers = followService.getUserFollowerList(userEmail, pageable);
        Slice<FollowDto> followings = followService.getUserFollowingList(userEmail, pageable);

        model.addAttribute("followers", followers.getContent());
        model.addAttribute("followersHasNext", followers.hasNext());
        model.addAttribute("followings", followings.getContent());
        model.addAttribute("followingsHasNext", followings.hasNext());
        model.addAttribute("userEmail", userEmail);

        return "function/Follow/follow_all"; // JSP 경로
    }
}
