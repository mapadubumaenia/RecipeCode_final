
package com.RecipeCode.teamproject.es.search.controller;

import com.RecipeCode.teamproject.es.reco.dto.FeedPageDto;
import com.RecipeCode.teamproject.es.reco.service.FeedService;
import com.RecipeCode.teamproject.es.search.service.SearchService;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/search")
public class SearchController {
    private final SearchService service;
    private final FeedService feedService;

    public SearchController(SearchService service, FeedService feedService) {
        this.service = service;
        this.feedService = feedService;
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
            return service.searchAndLog(q, tags, sort, page, size);
        } else {
            return service.searchAndLog(q, tags, sort, after, size);
        }
    }

    /** 쇼츠(트렌딩) */
    @GetMapping("/trending")
    public Map<String, Object> trending(
            @RequestParam(required = false) String after,
            @RequestParam(defaultValue = "8") int size
    ) {
        return service.trending(after, size);
    }

    /** 사이드 For You(개인화 피드 재활용). userId 없으면 hot 폴백됨 */
    @GetMapping("/foryou")
    public FeedPageDto forYou(
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String after,
            @RequestParam(defaultValue = "6") int size
    ) {
        return feedService.personalFeed(userId, after, size);
    }
}
