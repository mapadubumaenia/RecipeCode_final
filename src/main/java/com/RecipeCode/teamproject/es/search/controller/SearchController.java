package com.RecipeCode.teamproject.es.search.controller;

import com.RecipeCode.teamproject.es.search.service.SearchService;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/search")
public class SearchController {
    private final SearchService service;
    public SearchController(SearchService service) { this.service = service; }

    @GetMapping
    public Map<String,Object> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false, name = "tags") String tagsCsv,
            @RequestParam(defaultValue = "rel") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        List<String> tags = (tagsCsv == null || tagsCsv.isBlank())
                ? List.of()
                : Arrays.stream(tagsCsv.split(",")).map(String::trim)
                .filter(s -> !s.isEmpty()).collect(Collectors.toList());

        return service.searchAndLog(q, tags, sort, page, size);
    }
}
