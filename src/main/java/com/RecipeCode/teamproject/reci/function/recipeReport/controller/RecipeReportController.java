package com.RecipeCode.teamproject.reci.function.recipeReport.controller;

import com.RecipeCode.teamproject.reci.function.recipeReport.dto.RecipeReportDto;
import com.RecipeCode.teamproject.reci.function.recipeReport.service.RecipeReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Log4j2
@Controller
@RequiredArgsConstructor
public class RecipeReportController {

    private final RecipeReportService recipeReportService;

    //  전체 신고 목록
    @GetMapping("/report")
    public String selectReportList(@PageableDefault(page = 0, size = 10) Pageable pageable,
                                   Model model) {
        Page<RecipeReportDto> pages = recipeReportService.findAll(pageable);
        model.addAttribute("reports", pages.getContent());
        model.addAttribute("pages", pages);
        return "recipeReport/report_all"; // 목록 뷰
    }

    // 신고 저장 (사용자가 게시글 신고 버튼 눌렀을 때)
    @PostMapping("/report/add")
    public String insert(@ModelAttribute RecipeReportDto recipeReportDto) {
        log.info("신고 요청: {}", recipeReportDto);
        recipeReportService.save(recipeReportDto);
        return "redirect:/recipes/" + recipeReportDto.getUuid();
    }

    // 상태 변경 (수정)
    @PostMapping("/report/edit")
    public String update(@RequestParam Long reportId,
                         @RequestParam Long newStatus) {
        recipeReportService.updateStatus(reportId, newStatus);
        return "redirect:/report";
    }

    // 신고 삭제
    @PostMapping("/report/delete")
    public String deleteById(@RequestParam Long reportId) {
        recipeReportService.deleteById(reportId);
        return "redirect:/report";
    }
}
