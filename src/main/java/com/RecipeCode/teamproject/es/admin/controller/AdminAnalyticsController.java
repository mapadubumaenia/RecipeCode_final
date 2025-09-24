package com.RecipeCode.teamproject.es.admin.controller;

import com.RecipeCode.teamproject.es.admin.service.AdminAnalyticsService;
import lombok.extern.log4j.Log4j2;
import org.slf4j.Logger;
import org.springframework.web.bind.annotation.*;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/admin/analytics")
public class AdminAnalyticsController {

    private static final Logger log = LoggerFactory.getLogger(AdminAnalyticsController.class);
    private final AdminAnalyticsService svc;
    public AdminAnalyticsController(AdminAnalyticsService svc) { this.svc = svc; }

    // 1) 제로결과 키워드 Top N
    @GetMapping("/zero-keywords")
    public List<Map<String, Object>> zeroKeywords(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(defaultValue = "20") int size
    ) {
        Instant f = safeParseInstant(from);
        Instant t = safeParseInstant(to);
        log.info("[ZW] controller hit from={}, to={}, size={}", f, t, size);
        List<Map<String, Object>> out = svc.zeroResultKeywords(f, t, size);
        log.info("[ZW] controller returns {} rows", out.size());
        return out;
    }

    // 2) 많이 본 게시물
    @GetMapping("/top-viewed")
    public List<Map<String, Object>> topViewed(
            @RequestParam(defaultValue = "7") int days,
            @RequestParam(defaultValue = "20") int size
    ) {
        return svc.topViewed(days, size);
    }

    // 3) 트렌딩 태그
    @GetMapping("/trending-tags")
    public List<Map<String, Object>> trendingTags(
            @RequestParam(defaultValue = "30") int days,
            @RequestParam(defaultValue = "20") int size
    ) {
        return svc.trendingTags(days, size);
    }

    // 4) 많이 좋아요 받은 게시물
    @GetMapping("/top-liked")
    public List<Map<String, Object>> topLiked(
            @RequestParam(defaultValue = "7") int days,
            @RequestParam(defaultValue = "20") int size
    ) {
        return svc.topLiked(days, size);
    }

    // 5) 일자별 업로드 추이
    @GetMapping("/uploads-by-day")
    public List<Map<String, Object>> uploadsByDay(
            @RequestParam(defaultValue = "30") int days
    ) {
        return svc.uploadsByDay(days);
    }

    // 6) 상위 크리에이터
    @GetMapping("/top-creators")
    public List<Map<String, Object>> topCreators(
            @RequestParam(defaultValue = "30") int days,
            @RequestParam(defaultValue = "20") int size
    ) {
        return svc.topCreators(days, size);
    }





    private java.time.Instant safeParseInstant(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            return java.time.Instant.parse(s);  // ISO-8601 (예: 2025-09-21T00:00:00Z)
        } catch (Exception e) {
            // 필요하면 로그 찍기
            // org.slf4j.LoggerFactory.getLogger(getClass()).warn("Invalid instant: {}", s, e);
            return null; // 파싱 실패 시 null 처리
        }
    }


}