package com.RecipeCode.teamproject.reci.mypage.controller;

import com.RecipeCode.teamproject.common.MapStruct;
import com.RecipeCode.teamproject.reci.auth.dto.MemberDto;
import com.RecipeCode.teamproject.reci.auth.dto.SecurityUserDto;
import com.RecipeCode.teamproject.reci.auth.entity.Member;
import com.RecipeCode.teamproject.reci.auth.notisetting.entity.NotiSetting;
import com.RecipeCode.teamproject.reci.auth.notisetting.repository.NotiSettingRepository;
import com.RecipeCode.teamproject.reci.auth.repository.MemberRepository;
import com.RecipeCode.teamproject.reci.auth.service.MemberService;
import com.RecipeCode.teamproject.reci.mypage.service.MyPageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class MyPageViewController {

    private final NotiSettingRepository notiSettingRepository;
    private final MemberService memberService;
    private final MapStruct mapStruct;

    @GetMapping("/mypage")
    public String myPageView(@AuthenticationPrincipal SecurityUserDto principal,
                             Model model) {
        // 1) 비로그인일 때 로그인 페이지로
        if (principal == null) {
            return "redirect:/auth/login";
        }

        // 2) 로그인 이메일로 Member 조회
        String email = principal.getUsername(); // SecurityUserDto의 username = userEmail
        Member user = memberService.getByUserEmail(email);
        MemberDto memberDto = mapStruct.toDto(user);
        // 3) 화면에 뿌리기
        model.addAttribute("user", memberDto);

        // 4) JS에서 필요하면 바로 씀 (예: mypage-feed.js)
         model.addAttribute("currentUserEmail", memberDto.getUserEmail());
        return "profile/mypage_all";
    }

    @GetMapping("/mypage/edit")
    public String profileEdit(@AuthenticationPrincipal SecurityUserDto principal,
                              Model model){
        Member member = memberService.getByUserEmail(principal.getUsername());

        List<NotiSetting> notiSettings = notiSettingRepository.findByMember(member);

        model.addAttribute("member", member);
        model.addAttribute("notiSettings", notiSettings);

        return "profile/profile_edit";
    }
}
