package com.RecipeCode.teamproject.reci.function.recipeReport.service;

import com.RecipeCode.teamproject.reci.function.recipeReport.dto.RecipeReportDto;
import com.RecipeCode.teamproject.reci.function.recipeReport.entity.RecipeReport;
import com.RecipeCode.teamproject.reci.function.recipeReport.repository.RecipeReportRepository;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static org.junit.jupiter.api.Assertions.*;
@Log4j2
@SpringBootTest
class RecipeReportServiceTest {

    @Autowired
    private RecipeReportService recipeReportService;
    @Autowired
    private RecipeReportRepository recipeReportRepository;

    @Test
    void save() {
        RecipeReportDto recipeReportDto = new RecipeReportDto();
        recipeReportDto.setUuid("0bc68ceb-a747-41fc-9ef3-2f8c530fef8f");
        recipeReportDto.setUserEmail("asdf1234@naver.com");
        recipeReportDto.setAdminEmail("ad123@naver.com");
        recipeReportDto.setReason("스팸입니다.");
        recipeReportDto.setReportType(0L);
        recipeReportDto.setReportStatus(1L);
        recipeReportService.save(recipeReportDto);
    }

    @Test
    void findById() {
        //		1) 테스트 조건: Dept(dno,dname,loc)
        long RecipeId=3;
//		2) 실제 메소드실행
        RecipeReportDto recipeReportDto=recipeReportService.findById(RecipeId);
//		3) 검증(확인): 로그 , DB 확인, assert~ (DB확인)
        log.info(recipeReportDto);
    }

    @Test
    void deleteById() throws  Exception{
        recipeReportService.deleteById(3L);
    }

    @Test
    void findByReportStatus() {
        // 1) given: 대기중(0) 상태의 신고가 있다고 가정
        Long status = 1L;

        // 2) when: 서비스 호출
        Page<RecipeReportDto> result = recipeReportService.findByReportStatus(status, PageRequest.of(0, 10));

        // 3) then: 검증
        log.info("조회 결과: {}", result.getContent());
    }

//    @Test
//    void countByRecipesUuid() {
//        // 1) given: DB에 이미 존재하는 레시피 uuid
//        String uuid = "0bc68ceb-a747-41fc-9ef3-2f8c530fef8f"; 
//
//        // 2) when: 카운트 실행
//        Long count = recipeReportService.countByRecipesUuid(uuid);
//
//        // 3) then: 결과 확인
//        log.info("레시피 {} 신고 건수 = {}", uuid, count);
//    }

    @Test
    void findByReportType() {
        // given
        Long reportType = 0L; // 스팸
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<RecipeReportDto> page = recipeReportService.findByReportType(reportType, pageable);

        // then
        log.info("조회 결과 개수 = {}", page.getTotalElements());
    }

    @Test
    void updateStatus() {
        // 1) DB에 이미 있는 신고 ID
        Long reportId = 4L;

        // 2) 상태 업데이트 (대기중 → 처리중)
        recipeReportService.updateStatus(reportId, 1L);

        // 3) 다시 조회
        RecipeReport updated = recipeReportRepository.findById(reportId).orElseThrow();
        log.info("변경된 신고 상태={}", updated.getReportStatus());
    }

    @Test
    void findAll() {
        // 1) 페이징 조건 설정 (0페이지, 10개씩)
        Pageable pageable = PageRequest.of(0, 10);

        // 2) 서비스 메소드 실행
        Page<RecipeReportDto> page = recipeReportService.findAll(pageable);

        // 3) 결과 출력
        log.info("전체 신고 건수 = {}", page.getTotalElements());
    }
}