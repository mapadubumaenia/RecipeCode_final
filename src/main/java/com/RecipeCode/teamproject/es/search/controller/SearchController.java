package com.RecipeCode.teamproject.es.search.controller;

import com.RecipeCode.teamproject.es.search.service.SearchService;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/search")
public class SearchController {
    private final SearchService service;

    public SearchController(SearchService service) {
        this.service = service;
    }

    @GetMapping
    public Map<String, Object> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false, name = "tags") String tagsCsv,
            @RequestParam(defaultValue = "rel") String sort,
            @RequestParam(required = false) String after,   // 커서
            @RequestParam(defaultValue = "0") int page,     // rel에서만 사용
            @RequestParam(defaultValue = "20") int size     // 권장 기본 20
    ) {
        List<String> tags = (tagsCsv == null || tagsCsv.isBlank())
                ? List.of()
                : Arrays.stream(tagsCsv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        if ("rel".equalsIgnoreCase(sort)) {
            // 관련도 정렬은 ES 점수 기반이라 search_after 불가 → page 방식 유지
            return service.searchAndLog(q, tags, sort, page, size);
        } else {
            // new/hot 는 커서 우선(after). 첫 페이지면 after=null 로 호출
            return service.searchAndLog(q, tags, sort, after, size);
        }
    }
}