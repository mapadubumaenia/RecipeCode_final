package com.RecipeCode.teamproject.es.search.service;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import com.RecipeCode.teamproject.common.CursorUtil; // ★ 커서 유틸 import
import com.RecipeCode.teamproject.es.search.document.RecipeSearchDoc;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class SearchService {

    private final ElasticsearchOperations es;
    private final SearchLogService logs;

    public SearchService(ElasticsearchOperations es, SearchLogService logs) {
        this.es = es;
        this.logs = logs;
    }

    /**
     * 통합검색 v1 (하위호환)
     * - page 기반. 내부적으로 v2(after=null) 호출
     */
    public Map<String, Object> searchAndLog(String q, List<String> tags, String sort, int page, int size) {
        size = Math.min(Math.max(size, 1), 50);
        return searchAndLog(q, tags, sort, /*after*/ null, size);
    }

    /**
     * 통합검색 v2 (커서 기반)
     * - q:
     *   - "#태그"  -> tags(keyword) term 정확일치
     *   - "@아이디" -> authorId(keyword) term 정확일치
     *   - 그외     -> title/body/authorNick multi_match
     * - 공통 필터: visibility == PUBLIC
     * - 정렬: new(최신), hot(인기) → search_after 지원
     *   (rel/_score 모드는 score 기반이라 search_after 미지원: 이때 after는 무시)
     * - 집계: tags terms / createdAt day 히스토그램
     */
    public Map<String, Object> searchAndLog(String q, List<String> tags, String sort, String after, int size) {
        // size 가드(1~50)
        size = Math.min(Math.max(size, 1), 50);

        // 1) 메인 쿼리 분기
        Query main = buildMainQuery(q);

        // 2) 공통/부가 필터
        List<Query> filters = new ArrayList<>();
        filters.add(Query.of(b -> b.term(t -> t.field("visibility").value("PUBLIC"))));
        if (tags != null && !tags.isEmpty()) {
            List<FieldValue> vals = tags.stream().map(FieldValue::of).toList();
            filters.add(Query.of(b -> b.terms(t -> t.field("tags").terms(v -> v.value(vals)))));
        }

        Query boolQuery = Query.of(b -> b.bool(bb -> bb.must(main).filter(filters)));

        // 3) NativeQuery
        NativeQueryBuilder qb = NativeQuery.builder()
                .withQuery(boolQuery)
                .withPageable(PageRequest.of(0, size)); // 커서 모드의 첫 페이지는 항상 0

        // 4) 집계
        qb.withAggregation("tags", Aggregation.of(a -> a.terms(t -> t.field("tags").size(20))));
        qb.withAggregation("by_day", Aggregation.of(a -> a.dateHistogram(h -> h
                .field("createdAt")
                .fixedInterval(fi -> fi.time("1d")))));

        // 5) 정렬(커서 키와 순서 반드시 일치)
        boolean cursorSortable = false;
        if ("hot".equalsIgnoreCase(sort)) {
            qb.withSort(s -> s.field(f -> f.field("likes").order(SortOrder.Desc)));
            qb.withSort(s -> s.field(f -> f.field("createdAt").order(SortOrder.Desc)));
            qb.withSort(s -> s.field(f -> f.field("id").order(SortOrder.Desc))); // tie-breaker
            cursorSortable = true;
        } else if ("new".equalsIgnoreCase(sort) || !StringUtils.hasText(sort)) {
            qb.withSort(s -> s.field(f -> f.field("createdAt").order(SortOrder.Desc)));
            qb.withSort(s -> s.field(f -> f.field("id").order(SortOrder.Desc))); // tie-breaker
            cursorSortable = true;
        } else {
            // rel/_score 모드: 정렬 미설정(ES 기본 score desc)
            // → score 기반 search_after 미지원이므로 after는 무시됨
        }

        // 6) after 적용
        if (cursorSortable) {
            List<Object> afterValues = CursorUtil.decode(after);
            if (afterValues != null) {
                qb.withSearchAfter(afterValues);
            }
        }

        // 7) 검색 실행
        SearchHits<RecipeSearchDoc> hits = es.search(qb.build(), RecipeSearchDoc.class);

        // 8) 결과 매핑
        var items = hits.getSearchHits().stream().map(h -> {
            var d = h.getContent();
            return Map.<String, Object>of(
                    "id", d.getId(),
                    "title", d.getTitle(),
                    "tags", d.getTags(),
                    "authorNick", d.getAuthorNick(),
                    "likes", d.getLikes(),
                    "createdAt", d.getCreatedAt(),
                    "score", h.getScore()
            );
        }).toList();

        // 9) next 커서 생성
        String next = null;
        if (cursorSortable && !hits.getSearchHits().isEmpty()) {
            var last = hits.getSearchHits().get(hits.getSearchHits().size() - 1);
            var sortVals = last.getSortValues(); // List<Object>
            if (sortVals != null && !sortVals.isEmpty()) {
                next = CursorUtil.encode(sortVals);
            }
        }

        var res = new LinkedHashMap<String, Object>();
        res.put("total", hits.getTotalHits());
        res.put("items", items);
        res.put("next", next); // 없으면 FE는 더보기 버튼 숨김/자동로딩 중단

        // 10) 검색 로그 적재
        logs.log(q, tags, sort, /*page=*/0, size, hits.getTotalHits(), null);

        return res;
    }

    /**
     * q 규칙:
     *  - "#비건"  => term(tags == "비건")
     *  - "@u1234" => term(authorId == "u1234")
     *  - 그외     => multi_match(title, body, authorNick)
     *  - 빈값     => match_all
     *
     * 주의: 태그/아이디 케이스에서는 multi_match를 함께 넣지 않는다(AND로 0건 위험).
     */
    private Query buildMainQuery(String q) {
        String qv = (q == null) ? null : q.trim();
        if (!StringUtils.hasText(qv)) {
            return Query.of(b -> b.matchAll(m -> m));
        }
        if (qv.startsWith("#") && qv.length() > 1) {
            String tag = qv.substring(1).trim();
            return Query.of(b -> b.term(t -> t.field("tags").value(tag)));
        }
        if (qv.startsWith("@") && qv.length() > 1) {
            String uid = qv.substring(1).trim();
            return Query.of(b -> b.term(t -> t.field("authorId").value(uid)));
        }
        // 🔽 일반 텍스트: 정확 매치 + 접두(prefix) 매치 병행
        return Query.of(b -> b.bool(bb -> bb
                .should(s -> s.multiMatch(mm -> mm
                        .query(qv)
                        .fields("title^3", "body", "authorNick")
                        .type(TextQueryType.BestFields)       // 정확도 위주
                ))
                .should(s -> s.multiMatch(mm -> mm
                        .query(qv)
                        .fields("title^3", "body", "authorNick")
                        .type(TextQueryType.BoolPrefix)       // 마지막 토큰 접두 매치 (as-you-type)
                ))
                .minimumShouldMatch("1")                  // 둘 중 하나만 맞아도 통과
        ));
    }
}
