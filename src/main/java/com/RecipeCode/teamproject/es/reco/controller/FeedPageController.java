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
    public String feed(@RequestParam(required = false) String userEmail,
                       @RequestParam(required = false) String after,
                       @RequestParam(defaultValue = "20") int size,
                       Model model) {

        String ue = (userEmail == null) ? "" : userEmail.trim().toLowerCase();
        boolean hasUser = !ue.isBlank();

        FeedPageDto res = hasUser ? feed.personalFeed(ue, after, size)
                : feed.hot(after, size);

        model.addAttribute("userEmail", ue);
        model.addAttribute("size", size);
        model.addAttribute("hasUser", hasUser);
        model.addAttribute("items", res.getItems());
        model.addAttribute("next",  res.getNext());

        return "feed";
    }

    @ResponseBody
    @GetMapping("/healthz")
    public String healthz() { return "OK"; }
}