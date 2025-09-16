package com.RecipeCode.teamproject.reci.faq.controller;

import com.RecipeCode.teamproject.reci.faq.dto.FaqDto;
import com.RecipeCode.teamproject.reci.faq.service.FaqService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Log4j2
@Controller
@RequiredArgsConstructor
public class FaqController {
    private final FaqService faqService;

    //	전체조회
    @GetMapping("/faq")
    public String selectFaqList(@RequestParam(defaultValue = "") String searchKeyword,
                                @PageableDefault(page = 0, size = 20) Pageable pageable,
                                Model model) {
//		전체조회 서비스 메소드 실행
        Page<FaqDto> pages = faqService.selectFaqList(searchKeyword, pageable);
        log.info("FAQ 목록 조회 : " + pages);
        model.addAttribute("faqs", pages.getContent()); //FAQ 목록
        model.addAttribute("pages", pages);  // 페이징 정보

        return "faq/faq_all";
    }

    // 카테고리별 조회
    @GetMapping("/faq/category")
    public String selectFaqListByTag(@RequestParam String tag,
                                     @PageableDefault(page = 0, size = 20) Pageable pageable,
                                     Model model) {
//	카테고리 조회 서비스 메소드 실행
        Page<FaqDto> pages = faqService.selectFaqListByTag(tag, pageable);
        log.info("FAQ 카테고리별 목록 조회 : " + pages);
        model.addAttribute("faqs", pages.getContent()); // FAQ 목록
        model.addAttribute("pages", pages);  // 페이징 정보

        return "faq/faq_all";  //
    }


    //	추가 페이지 열기
    @GetMapping("/faq/addition")
    public String createFaqView() {
        return "faq/add_faq";
    }

    //	insert : 저장 버튼 클릭시
    @PostMapping("/faq/add")
    public String insert(@ModelAttribute FaqDto faqDto) {
//		Dept 내용 확인
        log.info("저장 로그 :" + faqDto);
//		서비스의 insert 실행
        faqService.save(faqDto);
        return "redirect:/faq/addition";
    }

    //	수정페이지 열기(상세조회)
    @GetMapping("/faq/edition")
    public String updateFaqView(@RequestParam long faqNum, Model model) {
//		서비스의 상세조회
        FaqDto faqDto = faqService.findById(faqNum);
        model.addAttribute("faq", faqDto);
        return "faq/update_faq";
    }

    //	수정: 버튼 클릭시 실행
    @PostMapping("/faq/edit")
    public String update(@ModelAttribute FaqDto faqDto) {
//		서비스의 수정 실행
        faqService.save(faqDto);
        return "redirect:/faq/edition?faqNum=" + faqDto.getFaqNum();
    }

    //	삭제
    @PostMapping("/faq/delete")
    public String deleteById(@RequestParam long faqNum) {
//		서비스의 삭제 실행
        faqService.deleteById(faqNum);
        return "redirect:/faq";
    }

}
