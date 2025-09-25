package com.RecipeCode.teamproject.es.reco.controller;

import com.RecipeCode.teamproject.es.admin.service.AdminAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/trends")
public class TrendsApiController {

    private final AdminAnalyticsService adminAnalyticsService;

    @GetMapping("/tags")
    public Map<String, Object> trendingTags(
            @RequestParam(defaultValue = "30") int days,
            @RequestParam(defaultValue = "12") int size // 화면 디자인엔 8~12개가 깔끔
    ) {
        List<Map<String, Object>> items = adminAnalyticsService.trendingTags(days, size);
        return Map.of("items", items);
    }
}