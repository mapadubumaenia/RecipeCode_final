package com.RecipeCode.teamproject.reci.function.commentsReport.service;

import com.RecipeCode.teamproject.reci.feed.comments.entity.Comments;
import com.RecipeCode.teamproject.reci.feed.comments.repository.CommentsRepository;
import com.RecipeCode.teamproject.reci.function.commentsReport.dto.CommentReportDto;
import com.RecipeCode.teamproject.reci.function.commentsReport.entity.CommentReport;
import com.RecipeCode.teamproject.reci.function.commentsReport.repository.CommentReportRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommentReportService {
    private final CommentReportRepository commentReportRepository;
    private final CommentsRepository commentsRepository;

    private CommentReportDto toDtoWithExtras(CommentReport report) {
        CommentReportDto dto = new CommentReportDto();
        Comments comment = report.getComments();

        dto.setReportId(report.getReportId());
        dto.setCommentsId(comment != null ? comment.getCommentsId() : null);
        dto.setCommentContent(comment != null ? comment.getCommentsContent() : "[삭제된 댓글]");
        dto.setUserEmail(report.getMember() != null ? report.getMember().getUserEmail() : null);
        dto.setReason(report.getReason());
        dto.setReportStatus(report.getReportStatus());
        dto.setReportType(report.getReportType());
        dto.setAdminEmail(report.getAdminEmail());
        dto.setInsertTime(report.getInsertTime());
        dto.setUpdateTime(report.getUpdateTime());

        dto.setDuplicateCount(comment != null ?
                commentReportRepository.countByComments_CommentsId(comment.getCommentsId()) : 0L);

        return dto;
    }

    // 신고 저장 + reportcount 증가
    @Transactional
    public CommentReportDto saveReport(CommentReport report) {
        Comments comment = report.getComments();
        if (comment != null) {
            commentsRepository.findById(comment.getCommentsId()).ifPresent(c -> {
                report.setComments(c);
                Long currentCount = c.getReportCount() != null ? c.getReportCount() : 0L;
                c.setReportCount(currentCount + 1);
                commentsRepository.save(c);
            });
        }
        CommentReport saved = commentReportRepository.save(report);
        return toDtoWithExtras(saved);
    }

    public boolean hasUserReported(Long commentsId, String userEmail) {
        return commentReportRepository.existsByComments_CommentsIdAndMember_UserEmail(commentsId, userEmail);
    }

    // CommentReportService
    public long countAllReports() {
        return commentReportRepository.count();
    }

    public long countByStatus(Long status) {
        return commentReportRepository.countByReportStatus(status);
    }


    // 페이징
    public Page<CommentReportDto> getAllReports(Pageable pageable) {
        return commentReportRepository.findAll(pageable)
                .map(this::toDtoWithExtras);
    }

    // 상태별 조회
    public Page<CommentReportDto> getReportsByStatus(Long status, Pageable pageable) {
        return commentReportRepository.findByReportStatus(status, pageable)
                .map(this::toDtoWithExtras);
    }

    // 유형별 조회
    public Page<CommentReportDto> getReportsByType(Long Type, Pageable pageable) {
        return commentReportRepository.findByReportType(Type, pageable)
                .map(this::toDtoWithExtras);
    }

    // 상태 + 유형 동시 조회
    public Page<CommentReportDto> getReportsByStatusAndType(Long status, Long type, Pageable pageable) {
        return commentReportRepository.findByReportStatusAndReportType(status, type, pageable)
                .map(this::toDtoWithExtras);
    }

    // 단건 조회
    public CommentReportDto findById(Long reportId) {
        CommentReport report = commentReportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("신고를 찾을 수 없습니다."));
        return toDtoWithExtras(report);
    }

    // 상태 변경
    @Transactional
    public void updateStatus(Long reportId, Long newReportStatus, String adminEmail) {
        CommentReport report = commentReportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("신고를 찾을 수 없습니다."));
        report.setReportStatus(newReportStatus);
        report.setAdminEmail(adminEmail);
    }

    // 삭제
    public void deleteById(Long reportId) {
        commentReportRepository.deleteById(reportId);
    }
}