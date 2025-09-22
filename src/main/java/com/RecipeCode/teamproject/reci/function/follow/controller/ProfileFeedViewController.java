package com.RecipeCode.teamproject.reci.function.follow.controller;

import com.RecipeCode.teamproject.common.ErrorMsg;
import com.RecipeCode.teamproject.common.MapStruct;
import com.RecipeCode.teamproject.reci.auth.dto.MemberDto;
import com.RecipeCode.teamproject.reci.auth.entity.Member;
import com.RecipeCode.teamproject.reci.auth.repository.MemberRepository;
import com.RecipeCode.teamproject.reci.feed.recipes.dto.RecipesDto;
import com.RecipeCode.teamproject.reci.function.follow.dto.FollowDto;
import com.RecipeCode.teamproject.reci.function.follow.service.FollowService;
import com.RecipeCode.teamproject.reci.function.follow.service.ProfileFeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class ProfileFeedViewController {

    private final MemberRepository memberRepository;
    private final MapStruct mapStruct;
    private final ProfileFeedService profileFeedService;
    private final FollowService followService;
    private final ErrorMsg errorMsg;


    // 특정 유저 프로필 페이지
    @GetMapping("/profile/{userEmail}")
    public String profilePage(@PathVariable String userEmail,
                              @PageableDefault(size = 10) Pageable pageable,
                              Model model) {

        // 1) 유저 정보 조회
        Member user = memberRepository.findById(userEmail)
                .orElseThrow(() -> new RuntimeException(errorMsg.getMessage("errors.not.found")));
        MemberDto userDto = mapStruct.toDto(user);
        model.addAttribute("user", userDto);

        // 2) 작성한 레시피 목록 (무한 스크롤 Slice)
        Slice<RecipesDto> posts = profileFeedService.getUserRecipes(userEmail, pageable);
        model.addAttribute("posts", posts.getContent());
        model.addAttribute("postsHasNext", posts.hasNext());

        // 3) 팔로워 / 팔로잉 (미리보기 10개)
        Slice<FollowDto> followers = followService.getUserFollowerList(userEmail, pageable);
        Slice<FollowDto> followings = followService.getUserFollowingList(userEmail, pageable);

        model.addAttribute("followers", followers.getContent());
        model.addAttribute("followersHasNext", followers.hasNext());
        model.addAttribute("followings", followings.getContent());
        model.addAttribute("followingsHasNext", followings.hasNext());

        // 4) JSP 반환
        return "profile/profile_feed";
    }
}


