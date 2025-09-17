package com.RecipeCode.teamproject.reci.function.recipeReport.service;

import com.RecipeCode.teamproject.common.ErrorMsg;
import com.RecipeCode.teamproject.common.MapStruct;
import com.RecipeCode.teamproject.reci.function.recipeReport.dto.RecipeReportDto;
import com.RecipeCode.teamproject.reci.function.recipeReport.entity.RecipeReport;
import com.RecipeCode.teamproject.reci.function.recipeReport.repository.RecipeReportRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class RecipeReportService {
    private final RecipeReportRepository recipeReportRepository;
    private final MapStruct mapStruct;
    private final ErrorMsg errorMsg;

    // 헬퍼 메서드(중복 + SLA 계산)
    private RecipeReportDto toDtoWithExtras(RecipeReport report) {
        RecipeReportDto dto = mapStruct.toDto(report);

        // 중복 카운트
        dto.setDuplicateCount(
                recipeReportRepository.countByRecipesUuid(dto.getUuid())
        );

        // SLA 남은 시간
        if (report.getInsertTime() != null) {
            long hoursLeft = ChronoUnit.HOURS.between(
                    LocalDateTime.now(),
                    report.getInsertTime().plusHours(24)
            );
            dto.setRemainingHours(hoursLeft);
        }

        return dto;
    }
    //  조회 메서드에 전부 헬퍼 씀
    // 전체 신고 목록 조회 (페이징 포함)
    public Page<RecipeReportDto> findAll(Pageable pageable) {
        Page<RecipeReport> page = recipeReportRepository.findAll(pageable);
        return recipeReportRepository.findAll(pageable)
                .map(this::toDtoWithExtras);
    }

    // 상태별 신고 목록 조회 (1=처리중, 2=처리완료)
    public Page<RecipeReportDto> findByReportStatus(Long reportStatus, Pageable pageable) {
        return recipeReportRepository.findByReportStatus(reportStatus, pageable)
                .map(this::toDtoWithExtras);
    }

    // 특정 신고 유형별 조회 (0=욕설, 1=스팸, 2=저작권)
    public Page<RecipeReportDto> findByReportType(Long reportType, Pageable pageable) {
        return recipeReportRepository.findByReportType(reportType, pageable)
                .map(this::toDtoWithExtras);
    }

    // 필터링용
    public Page<RecipeReportDto> findByStatusAndReportType(Long status, Long reportType, Pageable pageable) {
        return recipeReportRepository.findByReportStatusAndReportType(status, reportType, pageable)
                .map(this::toDtoWithExtras);
    }

    // 신고 저장
    public void save(RecipeReportDto recipeReportDto) {
        RecipeReport report = mapStruct.toEntity(recipeReportDto);
        recipeReportRepository.save(report);
    }

    // ID로 신고 단건 조회
    public RecipeReportDto findById(Long reportId) {
        RecipeReport recipeReport = recipeReportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException(errorMsg.getMessage("errors.not.found")));
        return mapStruct.toDto(recipeReport);
    }

    // 신고 삭제
    public void deleteById(Long reportId) {
        recipeReportRepository.deleteById(reportId);
    }

    // 상태별 개수 (추가)
    public Long countByReportStatus(Long reportStatus) {
        return recipeReportRepository.countByReportStatus(reportStatus);
    }

    // 상태 변경 (예: 대기중 → 처리중)
    @Transactional
    public void updateStatus(Long reportId, Long newStatus) {
        RecipeReport recipeReport = recipeReportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException(errorMsg.getMessage("errors.not.found")));
        recipeReport.setReportStatus(newStatus);
    }



}
