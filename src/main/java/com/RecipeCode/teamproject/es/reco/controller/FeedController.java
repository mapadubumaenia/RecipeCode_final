package com.RecipeCode.teamproject.es.reco.controller;

import com.RecipeCode.teamproject.es.reco.service.FeedService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/feed")
public class FeedController {

    private final FeedService feed;

    public FeedController(FeedService feed) { this.feed = feed; }

    // 로그인 연동 전: userId 파라미터로 테스트
    @GetMapping("/personal")
    public Map<String, Object> personal(
            @RequestParam String userId,
            @RequestParam(required = false) String after,
            @RequestParam(defaultValue = "20") int size
    ) {
        return feed.personalFeed(userId, after, size);
    }
}