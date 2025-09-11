package com.RecipeCode.teamproject.reci.faq.service;

import com.RecipeCode.teamproject.common.ErrorMsg;
import com.RecipeCode.teamproject.common.MapStruct;
import com.RecipeCode.teamproject.reci.faq.dto.FaqDto;
import com.RecipeCode.teamproject.reci.faq.entity.Faq;
import com.RecipeCode.teamproject.reci.faq.repository.FaqRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FaqService {
    private final FaqRepository faqRepository;
    private final MapStruct mapStruct;
    private final ErrorMsg errorMsg;

    public Page<FaqDto> selectFaqList(String searchKeyword, Pageable pageable) {
        Page<Faq> page= faqRepository.selectFaqList(searchKeyword, pageable);
        return page.map(faq -> mapStruct.toDto(faq));
    }
    public void save(FaqDto faqDto) {
        Faq faq=mapStruct.toEntity(faqDto);
        faqRepository.save(faq);
    }
    public FaqDto findById(long faq_num) {
//        JPA 상세조회 함수 실행
        Faq faq = faqRepository.findById(faq_num)
                .orElseThrow(() -> new RuntimeException(errorMsg.getMessage("errors.not.found")));

        return mapStruct.toDto(faq);
    }
    @Transactional
    public void updateFromDto(FaqDto faqDto) {
//        JPA 저장 함수 실행 : return 값 : 저장된 객체
        Faq faq=faqRepository.findById(faqDto.getFaq_num())
                .orElseThrow(() -> new RuntimeException("정보를 찾을 수 없습니다."));

        mapStruct.updateFromDto(faqDto, faq);
    }
    //    삭제 함수
    public void deleteById(long faq_num) {
        faqRepository.deleteById(faq_num);
    }
}
