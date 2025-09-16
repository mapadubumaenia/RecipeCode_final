package com.RecipeCode.teamproject.es.dashboard.controller;

import com.RecipeCode.teamproject.es.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class AdminPageController {

    private final DashboardService dashboardService;

    @GetMapping("/admin/board")
    public String dashboardPage(Model model) {
        model.addAttribute("todayRecipes", dashboardService.getTodayRecipeCount());
        model.addAttribute("pendingReports", dashboardService.getPendingReportCount());

        return "board";
    }
}
