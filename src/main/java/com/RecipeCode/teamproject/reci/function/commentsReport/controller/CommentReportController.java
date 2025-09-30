package com.RecipeCode.teamproject.reci.function.commentsReport.controller;

import com.RecipeCode.teamproject.common.SecurityUtil;
import com.RecipeCode.teamproject.reci.auth.dto.SecurityUserDto;
import com.RecipeCode.teamproject.reci.auth.entity.Member;
import com.RecipeCode.teamproject.reci.auth.repository.MemberRepository;
import com.RecipeCode.teamproject.reci.feed.comments.entity.Comments;
import com.RecipeCode.teamproject.reci.feed.comments.repository.CommentsRepository;
import com.RecipeCode.teamproject.reci.function.commentsReport.dto.CommentReportDto;
import com.RecipeCode.teamproject.reci.function.commentsReport.entity.CommentReport;
import com.RecipeCode.teamproject.reci.function.commentsReport.repository.CommentReportRepository;
import com.RecipeCode.teamproject.reci.function.commentsReport.service.CommentReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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
    private final MemberRepository memberRepository;
    private final SecurityUtil securityUtil;
    private final CommentReportRepository commentReportRepository;

    @PostMapping("/save")
    @ResponseBody
    public ResponseEntity<?> saveReport(@RequestBody CommentReportDto dto,
                                       @AuthenticationPrincipal UserDetails user) {
        Comments comments = commentsRepository.findById(dto.getCommentsId())
                .orElseThrow(() -> new RuntimeException("댓글 존재하지않음"));

        SecurityUserDto loginUser = securityUtil.getLoginUser();
        if (loginUser == null) {
            throw new RuntimeException("로그인 후 이용 가능합니다.");
        }

        // 본인 댓글 신고 금지
        if (comments.getMember().getUserEmail().equals(loginUser.getUserEmail())) {
            return ResponseEntity.badRequest().body("본인 댓글에는 신고할 수 없습니다.");
        }

        Member member = memberRepository.findByUserEmail(loginUser.getUsername())
                .orElseThrow(()->new RuntimeException("사용자를 찾을 수 없습니다."));

        boolean alreadyReported = commentReportService.hasUserReported(comments.getCommentsId(), loginUser.getUserEmail());
        if (alreadyReported) {
            throw new RuntimeException("이미 신고한 댓글입니다.");
        }

        // CommentReport 엔티티 생성
        CommentReport report = new CommentReport();
        report.setComments(comments);
        report.setMember(member);
        report.setReason(dto.getReason());
        report.setReportType(dto.getReportType());
        report.setReportStatus(0L); // 대기중

        CommentReportDto savedDto = commentReportService.saveReport(report);
        return ResponseEntity.ok(savedDto); // 정상 신고 시 DTO 반환
    }

    @PostMapping("/updateStatus")
    public String updateStatus(@RequestParam Long reportId,
                             @RequestParam Long newReportStatus,
                             @RequestParam String adminEmail) {
        commentReportService.updateStatus(reportId, newReportStatus, adminEmail);
        return "redirect:/admin/moderation/reports";
    }

    @PostMapping("/delete")
    public String deleteReport(@RequestParam Long reportId) {
        commentReportService.softDeleteCommentByReport(reportId);
        return "redirect:/admin/moderation/reports";
    }

    @GetMapping("/list")
    public String listReport(Model model,
                             @RequestParam(required = false) Long reportStatus,
                             @RequestParam(required = false) Long reportType,
                             Pageable pageable) {
        Page<CommentReportDto> page;

        if (reportStatus != null && reportType != null) {
            page = commentReportService.getReportsByStatusAndType(reportStatus, reportType, pageable);
        }else if (reportStatus != null) {
            page = commentReportService.getReportsByStatus(reportStatus, pageable);
        }else if (reportType != null) {
            page = commentReportService.getReportsByType(reportType, pageable);
        }else {
            page = commentReportService.getAllReports(pageable);
        }

        model.addAttribute("reports", page.getContent());
        model.addAttribute("page", page);

        model.addAttribute("total", commentReportService.countAllReports());
        model.addAttribute("pending", commentReportService.countByStatus(0L));
        model.addAttribute("processing", commentReportService.countByStatus(1L));
        model.addAttribute("completed",commentReportService.countByStatus(2L));
        return "function/commentsReport/commentReport_all"; // JSP 경로
    }
}
