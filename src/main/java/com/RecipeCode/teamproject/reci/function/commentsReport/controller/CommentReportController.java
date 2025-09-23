package com.RecipeCode.teamproject.reci.function.commentsReport.controller;

import com.RecipeCode.teamproject.reci.auth.entity.Member;
import com.RecipeCode.teamproject.reci.feed.comments.entity.Comments;
import com.RecipeCode.teamproject.reci.feed.comments.repository.CommentsRepository;
import com.RecipeCode.teamproject.reci.function.commentsReport.dto.CommentReportDto;
import com.RecipeCode.teamproject.reci.function.commentsReport.entity.CommentReport;
import com.RecipeCode.teamproject.reci.function.commentsReport.service.CommentReportService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/comments/report")
@Log4j2
public class CommentReportController {
    private final CommentReportService commentReportService;
    private final CommentsRepository commentsRepository;

    @PostMapping("/save")
    @ResponseBody
    public CommentReportDto saveReport(@RequestBody CommentReportDto dto, HttpSession session) {
        Comments comments = commentsRepository.findById(dto.getCommentsId())
                .orElseThrow(() -> new RuntimeException("댓글 존재하지않음"));
        Member member = (Member) session.getAttribute("loginMember");

        // 테스트용


        if (member == null) {
            member = new Member();
            member.setUserEmail("test@test.com");
            member.setUserId("test001");
        }

        // CommentReport 엔티티 생성
        CommentReport report = new CommentReport();
        report.setComments(comments);
        report.setMember(member);
        report.setReason(dto.getReason());
        report.setReportType(dto.getReportType());
        report.setReportStatus(0L); // 대기중

        return commentReportService.saveReport(report);
    }

    @PostMapping("/updateStatus")
    @ResponseBody
    public void updateStatus(@RequestParam Long reportId,
                             @RequestParam Long newReportStatus,
                             @RequestParam String adminEmail) {
        commentReportService.updateStatus(reportId, newReportStatus, adminEmail);
    }

    @PostMapping("/delete")
    @ResponseBody
    public void deleteReport(@RequestParam Long reportId) {
        commentReportService.deleteById(reportId);
    }

    @GetMapping("/list")
    public String listReport(Model model,
                             @RequestParam(required = false) Long reportStatus,
                             @RequestParam(required = false) Long reportType,
                             Pageable pageable) {
        var page = (reportStatus != null && reportType != null)
                ? commentReportService.getReportsByStatusAndType(reportStatus, reportType, pageable)
                : (reportStatus != null)
                ? commentReportService.getReportsByStatus(reportStatus, pageable)
                : commentReportService.getReportsByStatus(0L, pageable); // 기본: 대기중
        model.addAttribute("page", page);
        return "admin/commentReportList"; // JSP 경로
    }
}
