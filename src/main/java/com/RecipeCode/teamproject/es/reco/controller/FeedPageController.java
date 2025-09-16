// src/main/java/com/RecipeCode/teamproject/es/reco/controller/FeedPageController.java
package com.RecipeCode.teamproject.es.reco.controller;

import com.RecipeCode.teamproject.es.reco.dto.FeedPageDto;
import com.RecipeCode.teamproject.es.reco.service.FeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class FeedPageController {

    private final FeedService feed;

    @GetMapping("/feed")
    public String feed(@RequestParam(required = false) String userId,
                       @RequestParam(required = false) String after,
                       @RequestParam(defaultValue = "20") int size,
                       Model model) {

        String uid = (userId == null) ? "" : userId.trim();
        boolean hasUser = !uid.isBlank();

        // ★ SSR 단계에서도 분기: 개인화 or hot
        FeedPageDto res = hasUser ? feed.personalFeed(uid, after, size)
                : feed.hot(after, size);

        model.addAttribute("userId", uid);
        model.addAttribute("size", size);
        model.addAttribute("hasUser", hasUser);
        model.addAttribute("items", res.getItems());
        model.addAttribute("next",  res.getNext());

        return "feed"; // /WEB-INF/views/feed.jsp
    }

    @ResponseBody
    @GetMapping("/healthz")
    public String healthz() { return "OK"; }
}
