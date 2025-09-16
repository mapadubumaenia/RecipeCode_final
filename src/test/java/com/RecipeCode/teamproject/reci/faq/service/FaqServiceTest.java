package com.RecipeCode.teamproject.reci.faq.service;

import com.RecipeCode.teamproject.reci.faq.dto.FaqDto;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Log4j2
@SpringBootTest
@EnableJpaAuditing
class FaqServiceTest {

    @Autowired
    FaqService faqService;

    @Test
    void selectFaqList() {
        String searchKeyword = "";
        Pageable pageable = PageRequest.of(0, 3);
        Page<FaqDto> page = faqService.selectFaqList(searchKeyword, pageable);
        log.info("테스트 : " + page.getContent());  // 패이지 안에 content 에 dept 객체가 있습니다.
    }

    @Test
    void save() {
        //		1) 테스트 조건
        FaqDto faqDto = new FaqDto();
        faqDto.setFaqAnswer("테스트1");
        faqDto.setFaqQuestion("테스트1");
        faqDto.setFaqTag("테스트1");
//		2) 실제 메소드실행
        faqService.save(faqDto);
//		3) 검증(확인):  DB 확인
    }

    @Test
    void findById() {
        //		1) 테스트 조건:
        long faq_num = 2;
//		2) 실제 메소드실행
        FaqDto faqDto = faqService.findById(faq_num);
//		3) 검증(확인): 로그 , DB 확인, assert~ (DB확인)
        log.info(faqDto);
    }

    @Test
    void updateFromDto() {
        FaqDto faqDto = new FaqDto((long) 2, "테스트1-1", "테스트1-1", "테스트1-1");
//		2) 실제 메소드실행
        faqService.updateFromDto(faqDto);
//		3) 검증(확인): 로그 , DB 확인, assert~ (DB확인)
    }

    @Test
    void deleteById() throws  Exception {
        faqService.deleteById((long) 2);
    }
}


