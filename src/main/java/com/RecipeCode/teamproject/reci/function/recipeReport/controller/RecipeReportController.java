package com.RecipeCode.teamproject.reci.function.recipeReport.controller;

import com.RecipeCode.teamproject.reci.auth.dto.SecurityUserDto;
import com.RecipeCode.teamproject.reci.feed.recipes.service.RecipesService;
import com.RecipeCode.teamproject.reci.function.recipeReport.dto.RecipeReportDto;
import com.RecipeCode.teamproject.reci.function.recipeReport.service.RecipeReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
@Log4j2
@Controller
@RequiredArgsConstructor
public class RecipeReportController {

    private final RecipeReportService recipeReportService;
    private final RecipesService recipesService;

    // 전체 + 상태별 + 유형별 조회
    @GetMapping("/report")
    public String selectReportList(@RequestParam(required = false) Long status,
                                   @RequestParam(required = false) Long reportType,
                                   @PageableDefault(page = 0, size = 20) Pageable pageable,
                                   Model model) {
        Page<RecipeReportDto> pages;

        if (status != null && reportType != null) {
            pages = recipeReportService.findByStatusAndReportType(status, reportType, pageable);
        } else if (status != null) {
            pages = recipeReportService.findByReportStatus(status, pageable);
        } else if (reportType != null) {
            pages = recipeReportService.findByReportType(reportType, pageable);
        } else {
            pages = recipeReportService.findAll(pageable);
        }

        // 상태별 카운트
        Long processingCount = recipeReportService.countByReportStatus(1L);
        Long doneCount = recipeReportService.countByReportStatus(2L);

        model.addAttribute("reports", pages.getContent());
        model.addAttribute("pages", pages);
        model.addAttribute("processingCount", processingCount);
        model.addAttribute("doneCount", doneCount);
        model.addAttribute("status", status);
        model.addAttribute("reportType", reportType);

        return "function/recipeReport/report_all";
    }

    // 신고 저장 (사용자가 게시글 신고 버튼 눌렀을 때)
    @PostMapping("/report/add")
    @ResponseBody
    public Map<String, Object> insert(@ModelAttribute RecipeReportDto recipeReportDto,
                                      @AuthenticationPrincipal UserDetails user) {
        Map<String, Object> result = new HashMap<>();

        // 로그인 안된 경우
        if (user == null) {
            result.put("status", "fail");
            result.put("message", "로그인이 필요합니다.");
            return result;
        }

        // 1. 로그인된 사용자
        String loginEmail = user.getUsername();

        // 2. 신고 대상 레시피 작성자 조회
        String recipeOwnerEmail = recipesService.findById(recipeReportDto.getUuid())
                .getMember().getUserEmail();

        // 3. 자기 글이면 신고 불가
        if (loginEmail.equals(recipeOwnerEmail)) {
            result.put("status", "fail");
            result.put("message", "자신의 글은 신고할 수 없습니다.");
            return result;
        }

        // 4. 정상 저장
        recipeReportDto.setUserEmail(loginEmail);
        recipeReportService.save(recipeReportDto);

        result.put("status", "ok");
        result.put("uuid", recipeReportDto.getUuid());
        result.put("message", "신고가 접수되었습니다.");
        return result;
    }


    // 상태 변경 (수정)
    @PostMapping("/report/edit")
    public String update(@RequestParam Long reportId,
                         @RequestParam Long newStatus,
                         @RequestParam(required = false) String uuid,
                         @AuthenticationPrincipal SecurityUserDto admin) {

        String adminEmail = (admin != null) ? admin.getUserEmail() : "admin@lumeet.com";

        if (newStatus != 2L) {
            recipeReportService.updateStatus(reportId, newStatus);
            return "redirect:/admin/moderation/reports";
        }

        String action = (uuid != null) ? "DELETE" : "KEEP";
        if ("DELETE".equals(action)) {
            recipesService.softDeleteRecipe(uuid);
        }

        recipeReportService.processReport(reportId, action, adminEmail);
        return "redirect:/admin/moderation/reports";
    }

    // 신고 삭제
    @PostMapping("/report/delete")
    public String deleteById(@RequestParam Long reportId) {
        recipeReportService.deleteById(reportId);
        return "redirect:/admin/moderation/reports";
    }
}
