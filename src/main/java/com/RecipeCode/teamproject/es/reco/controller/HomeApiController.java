package com.RecipeCode.teamproject.es.reco.controller;

import com.RecipeCode.teamproject.es.admin.service.AdminAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/home")
@RequiredArgsConstructor
public class HomeApiController {

    private final AdminAnalyticsService admin;

    /** 최근 N일 좋아요 상위 게시물 */
    @GetMapping("/trending")
    public Map<String, Object> trending(@RequestParam(defaultValue = "7") int days,
                                        @RequestParam(defaultValue = "4") int size){
        List<Map<String,Object>> items = admin.topLiked(days, size);
        return Map.of("items", items);
    }
}