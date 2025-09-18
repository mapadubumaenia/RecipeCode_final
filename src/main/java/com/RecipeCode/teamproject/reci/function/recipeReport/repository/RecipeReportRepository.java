package com.RecipeCode.teamproject.reci.function.recipeReport.repository;

import com.RecipeCode.teamproject.reci.function.recipeReport.entity.RecipeReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecipeReportRepository extends JpaRepository<RecipeReport, Long> {
    // 상태별 조회 (1=처리중, 2=처리완료) 상태별로 신고 필터링용
    Page<RecipeReport> findByReportStatus(Long reportStatus, Pageable pageable);

    // 상태별 총 개수
    Long countByReportStatus(Long reportStatus);

    // 게시글 별 신고 내역 확인
    List<RecipeReport> findByRecipesUuid(String uuid);

    // 특정 유저가 한 신고 내역 조회
    List<RecipeReport> findByMemberUserEmail(String userEmail);

    // 특정 신고 유형만 조회 (0=욕설, 1=스팸, 2=저작권)
    Page<RecipeReport> findByReportType(Long reportType, Pageable pageable);

    // 특정 게시글의 총 신고 건수
    Long countByRecipesUuid(String uuid);

    // 동시조건용
    Page<RecipeReport> findByReportStatusAndReportType(Long status, Long reportType, Pageable pageable);

}
