package com.RecipeCode.teamproject.reci.recipeTag.controller;

import com.RecipeCode.teamproject.reci.recipeTag.dto.RecipeTagDto;
import com.RecipeCode.teamproject.reci.recipeTag.service.RecipeTagService;
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
public class RecipeTagController {

    private final RecipeTagService recipeTagService;

    //	전체조회
    @GetMapping("/recipeTag")
    public String selectRecipeTagList(@RequestParam(defaultValue = "") String searchKeyword,
                                      @PageableDefault(page = 0, size = 3) Pageable pageable,
                                      Model model) {
//		전체조회 서비스 메소드 실행
        Page<RecipeTagDto> pages = recipeTagService.selectRecipeTagList(searchKeyword, pageable);
        log.info("테스트 : " + pages);
        model.addAttribute("recipeTags", pages.getContent());
        model.addAttribute("pages", pages);

        return "recipeTag/recipeTag_all";
    }

    //	추가 페이지 열기
    @GetMapping("/recipeTag/addition")
    public String createRecipeTagView() {
        return "recipeTag/add_recipeTag";
    }

    //	insert : 저장 버튼 클릭시
//   TODO: 현재 RecipeTagDto에는 uuid(레시피 PK)가 필요합니다.
//    뷰단 <form>에서 <input type="hidden" name="uuid" value="..."> 식으로 레시피의 uuid를 함께 보내야 DB에 저장됩니다.
//    만약 uuid를 안 보내면 recipes가 null → ORA-01400 같은 에러가 또 발생합니다.
    @PostMapping("/recipeTag/add")
    public String insert(@ModelAttribute RecipeTagDto recipeTagDto) {
//		내용 확인
        log.info("테스트3 :" + recipeTagDto);
//		서비스의 insert 실행
        recipeTagService.save(recipeTagDto);

        return "redirect:/recipeTag";
    }

}
