package com.RecipeCode.teamproject.reci.recipeTag.service;

import com.RecipeCode.teamproject.reci.recipeTag.dto.RecipeTagDto;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.UUID;

@Log4j2
@SpringBootTest
class RecipeTagServiceTest {
    @Autowired
    RecipeTagService recipeTagService;

    @Test
    void selectRecipeTagList() {
        String searchKeyword = "";
        Pageable pageable = PageRequest.of(0, 3);
        Page<RecipeTagDto> page = recipeTagService.selectRecipeTagList(searchKeyword, pageable);
        log.info("테스트 : " + page.getContent());
    }

    @Test
    void save() {
        RecipeTagDto recipeTagDto = new RecipeTagDto();
        recipeTagDto.setUuid("11");
        recipeTagDto.setTagName("한식");
//		2) 실제 메소드실행
        recipeTagService.save(recipeTagDto);
//		3) 검증(확인):DB 확인
    }

    @Test
    void findById() {
        //		1) 테스트 조건:
        long tagId=1;
//		2) 실제 메소드실행
        RecipeTagDto recipeTagDto=recipeTagService.findById(tagId);
//		3) 검증(확인): DB 확인
        log.info(recipeTagDto);
    }
}


