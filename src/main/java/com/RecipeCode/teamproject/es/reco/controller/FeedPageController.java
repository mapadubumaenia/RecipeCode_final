package com.RecipeCode.teamproject.es.reco.controller;


import com.RecipeCode.teamproject.es.reco.service.FeedService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@Controller
public class FeedPageController {

    private final FeedService feed;

    public FeedPageController(FeedService feed) {
        this.feed = feed;
    }

    @GetMapping("/feed")
    public String feed(@RequestParam(required = false) String userId,
                       @RequestParam(required = false) String after,
                       @RequestParam(defaultValue = "20") int size,
                       Model model) {

        boolean hasUser = (userId != null && !userId.isBlank());
        model.addAttribute("userId", hasUser ? userId : "");
        model.addAttribute("size", size);
        model.addAttribute("hasUser", hasUser);

        if (hasUser) {
            Map<String, Object> res = feed.personalFeed(userId, after, size);
            model.addAttribute("items", res.get("items"));
            model.addAttribute("next",  res.get("next"));
        } else {
            model.addAttribute("items", Collections.emptyList());
            model.addAttribute("next",  null);
        }
        return "feed"; // → /WEB-INF/views/feed.jsp
    }

    @ResponseBody
    @GetMapping("/healthz")
    public String healthz() { return "OK"; }
}
