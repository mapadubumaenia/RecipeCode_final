package com.RecipeCode.teamproject.es.search.service;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
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
     * 통합검색 v1
     * - q:
     *   - "#태그"  -> tags(keyword) term 정확일치
     *   - "@아이디" -> authorId(keyword) term 정확일치
     *   - 그외     -> title/body/authorNick multi_match
     * - 공통 필터: visibility == PUBLIC
     * - 정렬: new(최신), hot(인기), 기본은 _score
     * - 집계: tags terms / createdAt day 히스토그램
     */
    public Map<String, Object> searchAndLog(String q, List<String> tags, String sort, int page, int size) {
        // size 가드(1~50)
        size = Math.min(Math.max(size, 1), 50);
        page = Math.max(page, 0);

        // 1) 메인 쿼리 분기
        Query main = buildMainQuery(q);

        // 2) 공통/부가 필터 구성
        List<Query> filters = new ArrayList<>();
        // visibility = PUBLIC 고정
        filters.add(Query.of(b -> b.term(t -> t.field("visibility").value("PUBLIC"))));

        // tags 파라미터로 필터가 넘어올 경우 (여러 태그 AND 필터가 있다면 terms 로 in-list)
        if (tags != null && !tags.isEmpty()) {
            List<FieldValue> vals = tags.stream().map(FieldValue::of).toList();
            filters.add(Query.of(b -> b.terms(t -> t.field("tags").terms(v -> v.value(vals)))));
        }

        // 3) 최종 Bool 조립 (must: main, filter: 공통/부가)
        Query boolQuery = Query.of(b -> b.bool(bb -> bb.must(main).filter(filters)));

        // 4) NativeQuery 빌더
        NativeQueryBuilder qb = NativeQuery.builder()
                .withQuery(boolQuery)
                .withPageable(PageRequest.of(page, size));

        // 5) 집계 (상위 태그 / 일자 히스토그램)
        qb.withAggregation("tags",
                Aggregation.of(a -> a.terms(t -> t.field("tags").size(20)))
        );
        qb.withAggregation("by_day",
                Aggregation.of(a -> a.dateHistogram(h -> h
                        .field("createdAt")
                        .fixedInterval(fi -> fi.time("1d"))   // 1일 간격(버전 호환 안전)
                ))
        );

        // 6) 정렬
        if ("new".equalsIgnoreCase(sort)) {
            qb.withSort(s -> s.field(f -> f.field("createdAt").order(SortOrder.Desc)));
        } else if ("hot".equalsIgnoreCase(sort)) {
            qb.withSort(s -> s.field(f -> f.field("likes").order(SortOrder.Desc)));
            qb.withSort(s -> s.field(f -> f.field("createdAt").order(SortOrder.Desc)));
        } // 기본은 _score (rel)

        // 7) 검색 실행
        SearchHits<RecipeSearchDoc> hits = es.search(qb.build(), RecipeSearchDoc.class);

        // 8) 결과 매핑 (필요 필드만 노출)
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

        var res = new LinkedHashMap<String, Object>();
        res.put("total", hits.getTotalHits());
        res.put("items", items);
        // (집계 응답까지 API로 내리고 싶다면, 아래처럼 꺼내서 넣어도 됨)
        // res.put("aggs", hits.getAggregations());

        // 9) 검색 로그 적재 (실패 없어야 하므로 예외는 서비스 내부에서 삼켜도 OK)
        logs.log(q, tags, sort, page, size, hits.getTotalHits(), null);

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

        // 일반 텍스트
        return Query.of(b -> b.multiMatch(mm -> mm
                .query(qv)
                .fields("title", "body", "authorNick")
                .type(TextQueryType.BestFields)));
    }
}