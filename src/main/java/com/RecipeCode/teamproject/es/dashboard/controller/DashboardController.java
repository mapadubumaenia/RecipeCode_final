package com.RecipeCode.teamproject.es.dashboard.controller;

import com.RecipeCode.teamproject.es.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    // 신규 게시글 저장 API
    @PostMapping("/posts")
    public ResponseEntity<String> createPost(@RequestParam String title,
                                             @RequestParam String userId) {
        dashboardService.saveNewPost(title, userId);
        return ResponseEntity.ok("신규 게시글 저장 완료");
    }

    // 신고 저장 API
    @PostMapping("/reports")
    public ResponseEntity<String> createReport(@RequestParam String targetType,
                                               @RequestParam String targetId,
                                               @RequestParam String userId,
                                               @RequestParam String reason,
                                               @RequestParam String status) {
        dashboardService.saveReport(targetType, targetId, userId, reason, status);
        return ResponseEntity.ok("신고 저장 완료");
    }
}
