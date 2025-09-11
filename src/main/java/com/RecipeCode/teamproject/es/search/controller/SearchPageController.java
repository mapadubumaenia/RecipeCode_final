package com.RecipeCode.teamproject.es.search.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SearchPageController {
    @GetMapping("/search")
    public String searchPage() {
        // /WEB-INF/views/search.jsp 로 resolve
        return "search";
    }
}
