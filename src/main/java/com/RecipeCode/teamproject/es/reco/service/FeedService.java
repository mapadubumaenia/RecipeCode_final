// src/main/java/com/RecipeCode/teamproject/es/feed/service/FeedService.java
package com.RecipeCode.teamproject.es.reco.service;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.RecipeCode.teamproject.es.reco.doc.UserRecsDoc;
import com.RecipeCode.teamproject.es.search.document.RecipeSearchDoc;
import com.RecipeCode.teamproject.es.search.service.SearchService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FeedService {

    private final ElasticsearchOperations es;
    private final SearchService searchService;

    public FeedService(ElasticsearchOperations es, SearchService searchService) {
        this.es = es;
        this.searchService = searchService;
    }

    public Map<String, Object> personalFeed(String userId, String after, int size) {
        size = Math.min(Math.max(size, 1), 50);

        // 1) 추천 문서 단건 조회 (문서ID = userId)
        UserRecsDoc rec = es.get(userId, UserRecsDoc.class);
        if (rec == null || rec.getItems() == null || rec.getItems().isEmpty()) {
            // 추천 없으면 인기순으로 폴백
            // ➊ v2(커서) 시그니처일 때:
            return searchService.searchAndLog(null, List.of(), "hot", null, size);
            // ➋ 만약 네 프로젝트가 아직 v1(page) 시그니처라면 아래로 바꿔:
            // return searchService.searchAndLog(null, List.of(), "hot", 0, size);
        }

        // 2) 커서(오프셋) 해석
        int offset = decodeOffset(after);
        List<UserRecsDoc.Item> all = rec.getItems();

        if (offset >= all.size()) {
            return Map.of("total", all.size(), "items", List.of(), "next", null);
        }

        List<UserRecsDoc.Item> page = all.subList(offset, Math.min(offset + size, all.size()));
        List<String> ids = page.stream().map(UserRecsDoc.Item::getRecipeId).toList();

        // 3) 레시피 상세 일괄 조회 + PUBLIC 필터
        Query q = Query.of(b -> b.bool(bb -> bb
                .must(m -> m.ids(i -> i.values(ids)))
                .filter(f -> f.term(t -> t.field("visibility").value("PUBLIC")))
        ));

        NativeQuery nq = NativeQuery.builder()
                .withQuery(q)
                .withPageable(PageRequest.of(0, ids.size()))
                .build();

        SearchHits<RecipeSearchDoc> hits = es.search(nq, RecipeSearchDoc.class);

        // 4) 추천 순서대로 정렬
        Map<String, RecipeSearchDoc> byId = hits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toMap(RecipeSearchDoc::getId, r -> r, (a,b)->a));

        List<Map<String, Object>> items = new ArrayList<>();
        for (UserRecsDoc.Item it : page) {
            RecipeSearchDoc d = byId.get(it.getRecipeId());
            if (d == null) continue; // 삭제/비공개 등 스킵
            items.add(Map.of(
                    "id", d.getId(),
                    "title", d.getTitle(),
                    "tags", d.getTags(),
                    "authorNick", d.getAuthorNick(),
                    "likes", d.getLikes(),
                    "createdAt", d.getCreatedAt(),
                    "recScore", it.getScore()
            ));
        }

        Integer nextIdx = (offset + page.size() < all.size()) ? (offset + page.size()) : null;
        String next = (nextIdx == null) ? null : encodeOffset(nextIdx);

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("total", all.size());
        res.put("items", items);
        res.put("next", next);
        return res;
    }

    // --- cursor utils (idx 기반) ---
    private static int decodeOffset(String after) {
        if (after == null || after.isBlank()) return 0;
        try {
            byte[] raw = Base64.getUrlDecoder().decode(after);
            String s = new String(raw, StandardCharsets.UTF_8);
            if (s.startsWith("idx:")) return Integer.parseInt(s.substring(4));
            return Integer.parseInt(s.trim()); // 숫자만 온 경우 호환
        } catch (Exception e) {
            return 0;
        }
    }
    private static String encodeOffset(int off) {
        String s = "idx:" + off;
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(s.getBytes(StandardCharsets.UTF_8));
    }
}

