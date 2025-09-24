package com.RecipeCode.teamproject.reci.mypage.controller;

import com.RecipeCode.teamproject.common.MapStruct;
import com.RecipeCode.teamproject.reci.auth.dto.MemberDto;
import com.RecipeCode.teamproject.reci.auth.dto.SecurityUserDto;
import com.RecipeCode.teamproject.reci.auth.entity.Member;
import com.RecipeCode.teamproject.reci.auth.membertag.service.MemberTagService;
import com.RecipeCode.teamproject.reci.auth.notisetting.entity.NotiSetting;
import com.RecipeCode.teamproject.reci.auth.notisetting.repository.NotiSettingRepository;
import com.RecipeCode.teamproject.reci.auth.repository.MemberRepository;
import com.RecipeCode.teamproject.reci.auth.service.MemberService;
import com.RecipeCode.teamproject.reci.mypage.service.MyPageService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class MyPageViewController {

    private final NotiSettingRepository notiSettingRepository;
    private final MemberService memberService;
    private final MemberRepository memberRepository;
    private final MemberTagService memberTagService;
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
                              Model model) {
        Member member = memberRepository.findByUserEmailWithTags(principal.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

        List<NotiSetting> notiSettings = notiSettingRepository.findByMember(member);

        model.addAttribute("member", member);
        model.addAttribute("notiSettings", notiSettings);

        return "profile/profile_edit";
    }
    @PostMapping("/mypage/updateProfile")
    public String updateProfile(@ModelAttribute MemberDto memberDto,
                                @AuthenticationPrincipal SecurityUserDto principal, // 프로젝트에 맞게 조정
                                RedirectAttributes rttr) {

        Member member = memberRepository.findByUserEmailWithTags(principal.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("회원 없음"));

        memberDto.setUserEmail(principal.getUserEmail());

        memberService.updateProfile(memberDto);
        memberTagService.syncTagsForMember(member, memberDto.getInterestTags());

        rttr.addFlashAttribute("msg", "프로필이 저장되었습니다.");
        return "redirect:/mypage";
    }

    @PostMapping("/mypage/delete")
    @ResponseBody
    public ResponseEntity<String> deleteAccount(@AuthenticationPrincipal SecurityUserDto member,
                                                HttpServletRequest request,
                                                HttpServletResponse response) {

        memberService.deletedMember(member.getUserEmail()); // status = DEACTIVATED or deleted = 'Y'

        SecurityContextHolder.clearContext(); // 로그아웃 처리

        // 세션 무효화
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        // 쿠키 삭제 (JSESSIONID)
        Cookie cookie = new Cookie("JSESSIONID", null);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        return ResponseEntity.ok("탈퇴 완료");
    }
}

