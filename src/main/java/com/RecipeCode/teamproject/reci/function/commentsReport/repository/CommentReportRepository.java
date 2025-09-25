package com.RecipeCode.teamproject.reci.function.commentsReport.repository;

import com.RecipeCode.teamproject.reci.function.commentsReport.entity.CommentReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface CommentReportRepository extends JpaRepository<CommentReport, Long> {
    // 상태별 조회 (대기중 = 0, 처리중 = 1, 답변완료 = 2)
    Page<CommentReport> findByReportStatus(Long reportStatus, Pageable pageable);

    // 상태별 총 개수
    Long countByReportStatus(Long reportStatus);

    // 댓글 별 신고 내역 확인
    List<CommentReport> findByComments_CommentsId(Long commentsId);

    // 특정 유저가 한 신고 내역 조회
    List<CommentReport> findByMemberUserEmail(String userEmail);

    boolean existsByComments_CommentsIdAndMember_UserEmail(Long commentsId, String userEmail);

    // 특정 신고 유형만 조회 (0=욕설, 1=스팸, 2=저작권)
    Page<CommentReport> findByReportType(Long reportType, Pageable pageable);

    // 특정 댓글의 총 신고 건수
    Long countByComments_CommentsId(Long commentsId);

    // 상태+유형 동시 조건 조회
    Page<CommentReport> findByReportStatusAndReportType(Long reportStatus, Long reportType, Pageable pageable);

}
