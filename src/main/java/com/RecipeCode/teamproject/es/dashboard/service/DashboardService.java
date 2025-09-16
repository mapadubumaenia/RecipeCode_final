package com.RecipeCode.teamproject.es.dashboard.service;

import com.RecipeCode.teamproject.es.dashboard.document.RecipeDoc;
import com.RecipeCode.teamproject.es.dashboard.document.ReportDoc;
import com.RecipeCode.teamproject.es.dashboard.repository.RecipeRepository;
import com.RecipeCode.teamproject.es.dashboard.repository.ReportRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

@Service
public class DashboardService {

    private final RecipeRepository recipeRepo;
    private final ReportRepository reportRepo;

    public DashboardService(RecipeRepository recipeRepo, ReportRepository reportRepo) {
        this.recipeRepo = recipeRepo;
        this.reportRepo = reportRepo;
    }

    // 신규 게시글 저장
    public void saveNewPost(String title, String userId) {
        RecipeDoc doc = new RecipeDoc();
        doc.setTitle(title);
        doc.setUserId(userId);
        doc.setCreatedAt(Instant.now());
        doc.setIsDeleted(false);
        recipeRepo.save(doc);
    }

    // 신고 저장
    public void saveReport(String targetType, String targetId, String userId, String reason, String status) {
        ReportDoc doc = new ReportDoc();
        doc.setTargetType(targetType);
        doc.setTargetId(targetId);
        doc.setUserId(userId);
        doc.setReason(reason);
        doc.setStatus(status);
        doc.setCreatedAt(Instant.now());
        reportRepo.save(doc);
    }

    // 오늘 작성 글 개수
    public Long getTodayRecipeCount(){
        Instant start = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant end = LocalDate.now().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();

        return recipeRepo.countByCreatedAtBetween(start,end);
    }

    // 미처리 개수
    public Long getPendingReportCount(){
        Instant now = Instant.now();

        return reportRepo.countByStatusAndCreatedAtBefore("PENDING",now);
    }
}
