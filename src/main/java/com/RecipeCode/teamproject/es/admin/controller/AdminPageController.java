// src/main/java/com/RecipeCode/teamproject/es/admin/controller/AdminPageController.java
package com.RecipeCode.teamproject.es.admin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminPageController {

    @GetMapping
    public String overview() {                 // 오버뷰
        return "admin/layout";
    }

    @GetMapping("/analytics")
    public String analytics() {                // 차트
        return "admin/layout";
    }

    @GetMapping("/moderation/reports")
    public String moderationReports() {        // 신고/관리
        return "admin/layout";
    }

    @GetMapping("/faq")
    public String faq() {                      // FAQ
        return "admin/layout";
    }

}
