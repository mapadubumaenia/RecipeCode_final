package com.RecipeCode.teamproject.reci.function.recipeReport.service;

import com.RecipeCode.teamproject.common.ErrorMsg;
import com.RecipeCode.teamproject.common.MapStruct;
import com.RecipeCode.teamproject.reci.function.notification.enums.NotificationEvent;
import com.RecipeCode.teamproject.reci.function.notification.service.NotificationService;
import com.RecipeCode.teamproject.reci.function.recipeReport.dto.RecipeReportDto;
import com.RecipeCode.teamproject.reci.function.recipeReport.entity.RecipeReport;
import com.RecipeCode.teamproject.reci.function.recipeReport.repository.RecipeReportRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@Log4j2
public class RecipeReportService {

    private final RecipeReportRepository recipeReportRepository;
    private final MapStruct mapStruct;
    private final ErrorMsg errorMsg;
    private final NotificationService notificationService;

    /* -----------------------
     * 내부 헬퍼
     * ----------------------- */

    /**
     * 레시피 제목 안전 추출(소프트삭제/엔티티 미존재 대비)
     */
    private String resolveRecipeTitle(RecipeReport report) {
        String title = "[삭제된 레시피]";
        try {
            if (report.getRecipes() != null && report.getRecipes().getRecipeTitle() != null) {
                title = report.getRecipes().getRecipeTitle();
            }
        } catch (EntityNotFoundException ignore) {
            // 삭제된 경우 기본값 유지
        }
        return title;
    }

    /**
     * DTO 변환 시 추가 정보 채우기(소프트삭제/중복/SLA)
     */
    private RecipeReportDto toDtoWithExtras(RecipeReport report) {
        RecipeReportDto dto = mapStruct.toDto(report);

        // uuid
        try {
            if (report.getRecipes() != null) {
                dto.setUuid(report.getRecipes().getUuid());
            }
        } catch (EntityNotFoundException e) {
            dto.setUuid(null);
        }

        // 제목
        dto.setRecipeTitle(resolveRecipeTitle(report));

        // 중복 카운트
        dto.setDuplicateCount(recipeReportRepository.countByRecipesUuid(dto.getUuid()));

        // SLA 남은 시간(접수 후 24시간 기준)
        if (report.getInsertTime() != null) {
            long hoursLeft = ChronoUnit.HOURS.between(
                    LocalDateTime.now(),
                    report.getInsertTime().plusHours(24)
            );
            dto.setRemainingHours(hoursLeft);
        }

        return dto;
    }

    /* -----------------------
     * 조회 계열
     * ----------------------- */

    // 전체 신고 목록(페이징)
    public Page<RecipeReportDto> findAll(Pageable pageable) {
        return recipeReportRepository.findAll(pageable).map(this::toDtoWithExtras);
    }

    // 상태별 신고 목록 (예: 1=처리중, 2=처리완료)
    public Page<RecipeReportDto> findByReportStatus(Long reportStatus, Pageable pageable) {
        return recipeReportRepository.findByReportStatus(reportStatus, pageable)
                .map(this::toDtoWithExtras);
    }

    // 유형별 신고 목록 (예: 0=욕설, 1=스팸, 2=저작권)
    public Page<RecipeReportDto> findByReportType(Long reportType, Pageable pageable) {
        return recipeReportRepository.findByReportType(reportType, pageable)
                .map(this::toDtoWithExtras);
    }

    // 복합 필터
    public Page<RecipeReportDto> findByStatusAndReportType(Long status, Long reportType, Pageable pageable) {
        return recipeReportRepository.findByReportStatusAndReportType(status, reportType, pageable)
                .map(this::toDtoWithExtras);
    }

    // ID로 단건 조회
    public RecipeReportDto findById(Long reportId) {
        RecipeReport report = recipeReportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException(errorMsg.getMessage("errors.not.found")));
        return mapStruct.toDto(report);
    }

    /* -----------------------
     * 쓰기/갱신 계열
     * ----------------------- */

    // 신고 저장
    public void save(RecipeReportDto recipeReportDto) {
        RecipeReport report = mapStruct.toEntity(recipeReportDto);
        recipeReportRepository.save(report);
    }

    // 신고 삭제
    public void deleteById(Long reportId) {
        recipeReportRepository.deleteById(reportId);
    }

    // 상태별 개수
    public Long countByReportStatus(Long reportStatus) {
        return recipeReportRepository.countByReportStatus(reportStatus);
    }

    // 상태 변경만 수행 — 알림 없음
    @Transactional
    public void updateStatus(Long reportId, Long newStatus) {
        RecipeReport report = recipeReportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException(errorMsg.getMessage("errors.not.found")));
        report.setReportStatus(newStatus);
        log.info("신고 {} 가 상태 {} 로 변경됨", reportId, newStatus);
    }

    /* -----------------------
     * 신고 '처리 확정' + 결과 알림
     * ----------------------- */

    /**
     * 관리자에 의해 신고가 최종 처리될 때 호출.
     *
     * @param reportId   신고 PK
     * @param action     "DELETE" or "KEEP"
     * @param adminEmail 처리자(관리자) 이메일
     */
    @Transactional
    public void processReport(Long reportId, String action, String adminEmail) {
        RecipeReport report = recipeReportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException(errorMsg.getMessage("errors.not.found")));

        // 1) 도메인 규칙에 맞는 상태로 확정(예: 2 = 처리완료)
        report.setReportStatus(2L);

        // 2) 결과 텍스트 매핑
        String resultText = "DELETE".equalsIgnoreCase(action) ? "삭제" : "유지";

        // 3) 제목/신고자
        String recipeTitle = resolveRecipeTitle(report);
        String reporterEmail = report.getMember().getUserEmail(); // 신고자 이메일

// 4) 알림 전송 (신고자에게)
        String recipeUuid = null;
        if (report.getRecipes() != null) {
            try {
                recipeUuid = report.getRecipes().getUuid();
            } catch (Exception ignore) {
            }
        }

        if ("DELETE".equalsIgnoreCase(action)) {
            // 삭제: 이동 없음
            notificationService.createNotification(
                    adminEmail,
                    reporterEmail,
                    NotificationEvent.RECIPE_REPORT_RESULT,
                    "RECIPE_REPORT",                         // 링크 없음
                    String.valueOf(report.getReportId()),    // 신고 PK
                    recipeTitle, resultText                  // "%s, %s"
            );
        } else {
            // 유지: 해당 레시피로 이동
            notificationService.createNotification(
                    adminEmail,
                    reporterEmail,
                    NotificationEvent.RECIPE_REPORT_RESULT,
                    "RECIPE",                                // 링크 생성
                    recipeUuid,                              // 레시피 UUID (null이면 FE에서 이동 생략)
                    recipeTitle, resultText
            );
        }

        log.info("신고 {} 처리 완료 (action={}, result={}, uuid={})",
                reportId, action, resultText, recipeUuid);
    }
}