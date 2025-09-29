// src/main/java/com/RecipeCode/teamproject/es/admin/controller/AdminPageController.java
package com.RecipeCode.teamproject.es.admin.controller;

import com.RecipeCode.teamproject.reci.faq.dto.FaqDto;
import com.RecipeCode.teamproject.reci.faq.entity.Faq;
import com.RecipeCode.teamproject.reci.faq.repository.FaqRepository;
import com.RecipeCode.teamproject.reci.faq.service.FaqService;
import com.RecipeCode.teamproject.reci.function.commentsReport.dto.CommentReportDto;
import com.RecipeCode.teamproject.reci.function.commentsReport.service.CommentReportService;
import com.RecipeCode.teamproject.reci.function.recipeReport.dto.RecipeReportDto;
import com.RecipeCode.teamproject.reci.function.recipeReport.entity.RecipeReport;
import com.RecipeCode.teamproject.reci.function.recipeReport.service.RecipeReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminPageController {

    private final RecipeReportService recipeReportService;
    private final CommentReportService commentReportService;
    private final FaqRepository faqRepository;
    private final FaqService faqService;

    @GetMapping
    public String overview() {                 // 오버뷰
        return "admin/layout";
    }

    @GetMapping("/analytics")
    public String analytics() {                // 차트
        return "admin/layout";
    }

    @GetMapping("/moderation/reports")
    public String moderationReports(@PageableDefault(size = 20) Pageable pageable,
                                    Model model) {
        // 댓글 신고
        Page<CommentReportDto> commentPage = commentReportService.getAllReports(pageable);
        model.addAttribute("commentReports", commentPage.getContent());
        model.addAttribute("commentPages", commentPage);

        // 레시피 신고
        Page<RecipeReportDto> recipePage = recipeReportService.findAll(pageable);
        model.addAttribute("recipeReports", recipePage.getContent());
        model.addAttribute("recipePages", recipePage);
        return "admin/layout";
    }

    @GetMapping("/faq")
    public String faq(@PageableDefault(size = 20) Pageable pageable,
                      @RequestParam(value="search", required=false) String search,
                      @RequestParam(value="tag", required=false) String tag,
                      Model model) {
        Page<FaqDto> faqPage;

        if(tag != null && !tag.isEmpty()) {
            faqPage = faqService.selectFaqListByTag(tag, pageable);
        } else if(search != null && !search.isEmpty()) {
            faqPage = faqService.selectFaqList(search, pageable);
        } else {
            faqPage = faqService.selectFaqList("", pageable); // 전체
        }

        model.addAttribute("faqs", faqPage.getContent());
        model.addAttribute("faqPages", faqPage);
        return "admin/layout";
    }

}
