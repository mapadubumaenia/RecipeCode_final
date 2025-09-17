package com.RecipeCode.teamproject.es.search.service;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import com.RecipeCode.teamproject.common.CursorUtil;
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

    /** 통합검색 v1 (하위호환: page 기반) */
    public Map<String, Object> searchAndLog(String q, List<String> tags, String sort, int page, int size) {
        size = Math.min(Math.max(size, 1), 50);
        return searchAndLog(q, tags, sort, null, size);
    }

    /** 통합검색 v2 (커서 기반) */
    public Map<String, Object> searchAndLog(String q, List<String> tags, String sort, String after, int size) {
        size = Math.min(Math.max(size, 1), 50);

        // 1) 메인 쿼리
        Query main = buildMainQuery(q);

        // 2) 필터
        List<Query> filters = new ArrayList<>();
        filters.add(Query.of(b -> b.term(t -> t.field("visibility").value("PUBLIC"))));
        filters.add(Query.of(b -> b.bool(bb -> bb
                .mustNot(mn -> mn.term(t -> t.field("deleted").value(true)))
        )));
        if (tags != null && !tags.isEmpty()) {
            List<FieldValue> vals = tags.stream().map(FieldValue::of).toList();
            filters.add(Query.of(b -> b.terms(t -> t.field("tags").terms(v -> v.value(vals)))));
        }
        Query boolQuery = Query.of(b -> b.bool(bb -> bb.must(main).filter(filters)));

        // 3) NativeQuery
        NativeQueryBuilder qb = NativeQuery.builder()
                .withQuery(boolQuery)
                .withPageable(PageRequest.of(0, size));

        // 4) 집계
        qb.withAggregation("tags", Aggregation.of(a -> a.terms(t -> t.field("tags").size(20))));
        qb.withAggregation("by_day", Aggregation.of(a -> a.dateHistogram(h -> h
                .field("createdAt")
                .fixedInterval(fi -> fi.time("1d")))));

        // 5) 정렬 + 커서 지원 여부
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
        } // rel 모드는 ES 기본 score desc, search_after 미지원

        // 6) after 적용
        if (cursorSortable) {
            List<Object> afterValues = CursorUtil.decode(after);
            if (afterValues != null) qb.withSearchAfter(afterValues);
        }

        // 7) 검색 실행
        var nq = qb.build();
        SearchHits<RecipeSearchDoc> hits = es.search(nq, RecipeSearchDoc.class);

        // 8) 결과 매핑
        // 변경
        var items = hits.getSearchHits().stream().map(h -> {
            var d = h.getContent();
            var m = new LinkedHashMap<String, Object>(12);
            m.put("id", h.getId()); // ✅ 핵심 수정
            m.put("title", d.getTitle() != null ? d.getTitle() : "");
            m.put("tags", d.getTags() != null ? d.getTags() : List.of());
            m.put("authorId",  d.getAuthorId()  != null ? d.getAuthorId()  : "");
            m.put("authorNick", d.getAuthorNick() != null ? d.getAuthorNick() : "");
            m.put("likes", d.getLikes() != null ? d.getLikes() : 0L);
            m.put("createdAt", d.getCreatedAt());
            m.put("score", h.getScore());
            m.put("thumbUrl", d.getThumbUrl() != null ? d.getThumbUrl() : "");
            m.put("comments", d.getComments() != null ? d.getComments() : 0L);
            m.put("views", d.getViews() != null ? d.getViews() : 0L);
            return m;
        }).toList();

        // 9) next 커서
        String next = null;
        if (cursorSortable && !hits.getSearchHits().isEmpty()) {
            var last = hits.getSearchHits().get(hits.getSearchHits().size() - 1);
            var sortVals = last.getSortValues();
            if (sortVals != null && !sortVals.isEmpty()) {
                next = CursorUtil.encode(sortVals);
            }
        }

        var res = new LinkedHashMap<String, Object>();
        res.put("total", hits.getTotalHits());
        res.put("items", items);
        res.put("next", next);

        // 10) 검색 로그
        logs.log(q, tags, sort, 0, size, hits.getTotalHits(), null);

        return res;
    }

    /** q 규칙 */
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
            String nick = qv.substring(1).trim();
            // ✅ @검색을 authorNick으로 정확 일치
            return Query.of(b -> b.term(t -> t.field("authorNick").value(nick)));
        }
        return Query.of(b -> b.bool(bb -> bb
                .should(s -> s.multiMatch(mm -> mm
                        .query(qv)
                        .fields("title^3", "body", "authorNick", "ingredients^2")
                        .type(TextQueryType.BestFields)))
                .should(s -> s.multiMatch(mm -> mm
                        .query(qv)
                        .fields("title^3", "body", "authorNick")
                        .type(TextQueryType.BoolPrefix)))
                .minimumShouldMatch("1")));
    }

    /** 쇼츠(트렌딩) */
    public Map<String, Object> trending(String after, int size) {
        size = Math.min(Math.max(size, 1), 20);

        var qb = NativeQuery.builder()
                .withQuery(Query.of(b -> b.bool(bb -> bb
                        .filter(f -> f.term(t -> t.field("visibility").value("PUBLIC")))
                        .filter(f -> f.bool(b2 -> b2.mustNot(mn -> mn.term(t -> t.field("deleted").value(true))))) // ✅ 추가
                )))

                .withPageable(PageRequest.of(0, size))
                .withSort(s -> s.field(f -> f.field("likes").order(SortOrder.Desc)))
                .withSort(s -> s.field(f -> f.field("createdAt").order(SortOrder.Desc)))
                .withSort(s -> s.field(f -> f.field("id").order(SortOrder.Desc)));


        var afterValues = CursorUtil.decode(after);
        if (afterValues != null) qb.withSearchAfter(afterValues);

        SearchHits<RecipeSearchDoc> hits = es.search(qb.build(), RecipeSearchDoc.class);

        // 변경
        var items = hits.getSearchHits().stream().map(h -> {
            var d = h.getContent();
            var m = new LinkedHashMap<String, Object>(10);
            m.put("id", h.getId());
            m.put("title", d.getTitle() != null ? d.getTitle() : "");
            m.put("authorId", d.getAuthorId() != null ? d.getAuthorId() : "");
            m.put("authorNick", d.getAuthorNick() != null ? d.getAuthorNick() : "");
            m.put("likes", d.getLikes() != null ? d.getLikes() : 0L);
            m.put("createdAt", d.getCreatedAt());
            m.put("tags", d.getTags() != null ? d.getTags() : List.of());
            m.put("thumbUrl", d.getThumbUrl() != null ? d.getThumbUrl() : "");
            return m;
        }).toList();

        String next = null;
        if (!hits.getSearchHits().isEmpty()) {
            var last = hits.getSearchHits().get(hits.getSearchHits().size() - 1);
            if (last.getSortValues() != null && !last.getSortValues().isEmpty()) {
                next = CursorUtil.encode(last.getSortValues());
            }
        }

        return Map.of("items", items, "next", next);
    }
}
