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

@Service
@RequiredArgsConstructor
public class RecipeReportService {
    private final RecipeReportRepository recipeReportRepository;
    private final MapStruct mapStruct;
    private final ErrorMsg errorMsg;

    // 전체 신고 목록 조회 (페이징 포함)
    public Page<RecipeReportDto> findAll(Pageable pageable) {
        Page<RecipeReport> page = recipeReportRepository.findAll(pageable);
        return page.map(recipeReport -> {
            RecipeReportDto dto = mapStruct.toDto(recipeReport);
            dto.setDuplicateCount(
                    recipeReportRepository.countByRecipesUuid(dto.getUuid())
            );
            return dto;
        });
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

    // 상태별 신고 목록 조회 (0=대기중, 1=처리중, 2=답변완료)
    public Page<RecipeReportDto> findByReportStatus(Long reportStatus, Pageable pageable) {
        Page<RecipeReport> page = recipeReportRepository.findByReportStatus(reportStatus, pageable);
        return page.map(recipeReport -> mapStruct.toDto(recipeReport));
    }


    // 특정 신고 유형별 조회 (0=욕설, 1=스팸, 2=저작권)
    public Page<RecipeReportDto> findByReportType(Long reportType, Pageable pageable) {
        Page<RecipeReport> page = recipeReportRepository.findByReportType(reportType, pageable);
        return page.map(recipeReport -> mapStruct.toDto(recipeReport));
    }

    // 상태 변경 (예: 대기중 → 처리중)
    @Transactional
    public void updateStatus(Long reportId, Long newStatus) {
        RecipeReport recipeReport = recipeReportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException(errorMsg.getMessage("errors.not.found")));
        recipeReport.setReportStatus(newStatus);
    }

}
