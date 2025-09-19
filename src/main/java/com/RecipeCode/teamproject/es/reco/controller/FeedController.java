package com.RecipeCode.teamproject.es.reco.controller;

import com.RecipeCode.teamproject.es.reco.dto.FeedPageDto;
import com.RecipeCode.teamproject.es.reco.service.FeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/feed")
@RequiredArgsConstructor
public class FeedController {

    private final FeedService feed;

    @GetMapping("/personal")
    public FeedPageDto personal(
            @RequestParam String userEmail,
            @RequestParam(required = false) String after,
            @RequestParam(defaultValue = "20") int size
    ) {
        return feed.personalFeed(userEmail, after, size);
    }

    @GetMapping("/hot")
    public FeedPageDto hot(
            @RequestParam(required = false) String after,
            @RequestParam(defaultValue = "20") int size
    ) {
        return feed.hot(after, size);
    }
}