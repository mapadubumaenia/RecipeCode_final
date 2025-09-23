package com.RecipeCode.teamproject.reci.function.commentsReport.service;

import com.RecipeCode.teamproject.reci.auth.entity.Member;
import com.RecipeCode.teamproject.reci.auth.repository.MemberRepository;
import com.RecipeCode.teamproject.reci.feed.comments.entity.Comments;
import com.RecipeCode.teamproject.reci.feed.comments.repository.CommentsRepository;
import com.RecipeCode.teamproject.reci.function.commentsReport.dto.CommentReportDto;
import com.RecipeCode.teamproject.reci.function.commentsReport.entity.CommentReport;
import com.RecipeCode.teamproject.reci.function.commentsReport.repository.CommentReportRepository;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Service;

import static org.junit.jupiter.api.Assertions.*;

@Log4j2
@SpringBootTest
class CommentReportServiceTest {

    @Autowired
    private CommentReportService commentReportService;

    @Autowired
    private CommentReportRepository commentReportRepository;

    @Autowired
    private CommentsRepository commentsRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    void saveReport() {
        // DB에 존재하는 댓글 가져오기 (없으면 테스트용 댓글 생성 가능)
        Comments comment = commentsRepository.findById(1L).orElseThrow();

        Member member = Member.builder()
                .userEmail("test@test.com")
                .userId("test001")
                .nickname("유저")
                .password("1234")
                .profileStatus("PRIVATE")
                .build();
        memberRepository.save(member);

        CommentReport report = new CommentReport();
        report.setComments(comment);
        report.setMember(member);
        report.setReason("테스트 신고");
        report.setReportStatus(0L); // 대기중
        report.setReportType(1L);   // 스팸

        CommentReportDto dto = commentReportService.saveReport(report);

        assertNotNull(dto.getReportId());
        assertEquals("테스트 신고", dto.getReason());

        // 댓글 신고 카운트 증가 확인
        Comments updatedComment = commentsRepository.findById(comment.getCommentsId()).orElseThrow();
        assertEquals(comment.getReportCount() + 1, updatedComment.getReportCount());
        log.info("저장된 신고 DTO: {}", dto);
    }

//    @Test
//    void getReportsByStatus() {
//    }
//
//    @Test
//    void getReportsByStatusAndType() {
//    }
//
//    @Test
//    void findById() {
//    }
//
//    @Test
//    void updateStatus() {
//    }
//
//    @Test
//    void deleteById() {
//    }
}