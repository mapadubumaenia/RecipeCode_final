// src/main/java/com/RecipeCode/teamproject/es/reco/controller/FeedController.java
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
            @RequestParam String userId,
            @RequestParam(required = false) String after,
            @RequestParam(defaultValue = "20") int size
    ) {
        return feed.personalFeed(userId, after, size);
    }

    // ★ hot 전용 엔드포인트
    @GetMapping("/hot")
    public FeedPageDto hot(
            @RequestParam(required = false) String after,
            @RequestParam(defaultValue = "20") int size
    ) {
        return feed.hot(after, size);
    }
}
