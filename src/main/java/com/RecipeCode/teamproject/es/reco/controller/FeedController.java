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
        // ✅ 이메일 소문자 고정 + size 가드(1~50)
        String ue = (userEmail == null) ? "" : userEmail.trim().toLowerCase();
        int sz = Math.min(Math.max(size, 1), 50);
        return feed.personalFeed(ue, after, sz);
    }

    @GetMapping("/hot")
    public FeedPageDto hot(
            @RequestParam(required = false) String after,
            @RequestParam(defaultValue = "20") int size
    ) {
        int sz = Math.min(Math.max(size, 1), 50);
        return feed.hot(after, sz);
    }
}
