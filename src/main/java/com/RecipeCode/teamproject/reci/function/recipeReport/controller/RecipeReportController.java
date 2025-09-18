package com.RecipeCode.teamproject.reci.function.recipeReport.controller;

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

        return "recipeReport/report_all";
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

        // 로그인 된 경우 -> 이메일 세팅 후 저장
        recipeReportDto.setUserEmail(user.getUsername());
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
                         @RequestParam(required = false) String uuid) {

        // 1. 서비스에 상태 전달
        recipeReportService.updateStatus(reportId, newStatus);

        // 2. uuid가 넘어왔다는 건 "삭제" 버튼을 누른 경우
        if (uuid != null && newStatus == 2L) {
            recipesService.softDeleteRecipe(uuid); // 게시글 소프트 삭제
        }

        return "redirect:/report";
    }
    // 신고 삭제
    @PostMapping("/report/delete")
    public String deleteById(@RequestParam Long reportId) {
        recipeReportService.deleteById(reportId);
        return "redirect:/report";
    }
}
