// src/main/java/com/RecipeCode/teamproject/es/reco/controller/FeedPageController.java
package com.RecipeCode.teamproject.es.reco.controller;

import com.RecipeCode.teamproject.es.reco.dto.FeedPageDto;
import com.RecipeCode.teamproject.es.reco.service.FeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class FeedPageController {

    private final FeedService feed;

    // 홈 = 피드
    @GetMapping({"/"})
    public String home(Model model,
                       HttpSession session,
                       Principal principal,
                       @RequestParam(required = false) String after,
                       @RequestParam(defaultValue = "20") int size) {

        // 로그인 이메일 확보 (사이트 정책에 맞게 한 줄만 쓰면 됨)
        String sessionEmail = (String) session.getAttribute("loginEmail"); // 로그인 시 세션에 넣어둔 값이 있으면 사용
        String secEmail = (principal != null) ? principal.getName() : null; // Spring Security 기본값일 때

        String ue = (sessionEmail != null && !sessionEmail.isBlank())
                ? sessionEmail
                : (secEmail != null ? secEmail : "");

        ue = (ue == null) ? "" : ue.trim().toLowerCase();
        boolean hasUser = !ue.isBlank();

        int sz = Math.min(Math.max(size, 1), 50);
        FeedPageDto res = hasUser ? feed.personalFeed(ue, after, sz) : feed.hot(after, sz);

        model.addAttribute("userEmail", ue);
        model.addAttribute("size", sz);
        model.addAttribute("hasUser", hasUser);
        model.addAttribute("items", res.getItems());
        model.addAttribute("next",  res.getNext());

        return "home"; // /WEB-INF/views/home.jsp
    }

    // 기존 /feed도 유지 (직접 userEmail 넘길 때)
    @GetMapping("/feed")
    public String feed(@RequestParam(required = false) String userEmail,
                       @RequestParam(required = false) String after,
                       @RequestParam(defaultValue = "20") int size,
                       Model model) {

        String ue = (userEmail == null) ? "" : userEmail.trim().toLowerCase();
        boolean hasUser = !ue.isBlank();
        int sz = Math.min(Math.max(size, 1), 50);

        FeedPageDto res = hasUser ? feed.personalFeed(ue, after, sz) : feed.hot(after, sz);

        model.addAttribute("userEmail", ue);
        model.addAttribute("size", sz);
        model.addAttribute("hasUser", hasUser);
        model.addAttribute("items", res.getItems());
        model.addAttribute("next",  res.getNext());
        return "feed";
    }

    @ResponseBody
    @GetMapping("/healthz")
    public String healthz() { return "OK"; }
}
