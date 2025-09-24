package com.RecipeCode.teamproject.es.admin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminPageController {
    @GetMapping("/admin/analytics")
    public String adminAnalytics() { return "analytics"; } // 위 JSP 경로
}
