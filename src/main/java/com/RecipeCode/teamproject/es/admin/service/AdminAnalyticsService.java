package com.RecipeCode.teamproject.es.admin.service;

import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.RecipeCode.teamproject.es.search.document.RecipeSearchDoc;
import com.RecipeCode.teamproject.es.search.document.SearchLogDoc;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.elasticsearch.ElasticsearchClient;

import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import org.springframework.data.elasticsearch.core.SearchHits;
import java.lang.reflect.Field;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * 인덱스 지정은 각 Doc(@Document#indexName)으로 자동 적용됨.
 * - search-logs: SearchLogDoc(indexName="rc-search-logs-000001")
 * - recipe    : RecipeSearchDoc(indexName="recipe")
 */
@Log4j2
@Service
public class AdminAnalyticsService {

    private final ElasticsearchClient esClient;
    private final ElasticsearchOperations es;
    public AdminAnalyticsService(ElasticsearchOperations es, ElasticsearchClient esClient) {
        this.es = es;
        this.esClient = esClient;}

    /* -------------------------------------------
     * 1) 제로결과 키워드 Top-N (기간 필터)
     *    source: rc-search-logs-000001 (SearchLogDoc)
     *    bucket: terms(q) + sub-agg max(at)
     * ------------------------------------------- */
        public List<Map<String, Object>> zeroResultKeywords(Instant from, Instant to, int size) {
            final Instant fromF = (from == null) ? Instant.now().minus(30, ChronoUnit.DAYS) : from;
            final Instant toF   = (to   == null) ? Instant.now()                          : to;
            final int sizeF     = Math.min(Math.max(size, 1), 100);

            log.info("[ZERO] (direct) fromF={}, toF={}, sizeF={}", fromF, toF, sizeF);

            try {
                var req = new co.elastic.clients.elasticsearch.core.SearchRequest.Builder()
                        .index("rc-search-logs-000001") // SearchLogDoc 인덱스명
                        .size(0) // 문서 필요없고 집계만
                        .query(q -> q.bool(b -> b
                                .filter(f -> f.range(r -> r.field("at")
                                        .gte(co.elastic.clients.json.JsonData.of(fromF.toString()))
                                        .lte(co.elastic.clients.json.JsonData.of(toF.toString()))
                                ))
                                .filter(f -> f.term(t -> t.field("zeroHit").value(true)))
                                .mustNot(m -> m.term(t -> t.field("q").value("")))      // 빈 문자열
                                .mustNot(m -> m.prefix(p -> p.field("q").value("@")))   // @로 시작
                        ))
                        .aggregations("by_keyword", a -> a
                                .terms(t -> t.field("q").size(sizeF))
                                .aggregations("last_at", aa -> aa.max(m -> m.field("at")))
                        )
                        .build();

                var resp = esClient.search(req, com.RecipeCode.teamproject.es.search.document.SearchLogDoc.class);

                var aggs = resp.aggregations();
                if (aggs == null || !aggs.containsKey("by_keyword")) {
                    log.warn("[ZERO] (direct) aggregations empty or missing 'by_keyword'");
                    return java.util.List.of();
                }

                var byKw = aggs.get("by_keyword");
                var buckets = byKw.sterms().buckets();
                java.util.List<java.util.Map<String,Object>> out = new java.util.ArrayList<>();

                if (buckets != null && buckets.isArray()) {
                    for (var b : buckets.array()) {
                        Instant lastAt = null;
                        var sub = b.aggregations();
                        if (sub != null && sub.containsKey("last_at")) {
                            var lastAgg = sub.get("last_at");
                            if (lastAgg != null && lastAgg.isMax() && lastAgg.max() != null) {
                                String s = lastAgg.max().valueAsString();
                                if (s != null && !s.isBlank()) {
                                    try {
                                        lastAt = Instant.parse(s);
                                    } catch (Exception ignore) {
                                        double v = lastAgg.max().value();
                                        lastAt = Instant.ofEpochMilli((long) v);
                                    }
                                } else {
                                    double v = lastAgg.max().value();
                                    lastAt = Instant.ofEpochMilli((long) v);
                                }
                            }
                        }


                        // 기존 try { keyStr = b.keyAsString(); } ... 전부 삭제
                        String keyStr = null;

                        // StringTermsBucket이면 key() 자체가 String입니다.
                        try {
                            keyStr = (String) b.getClass().getMethod("key").invoke(b);
                        } catch (Exception ignore) {
                            // 혹시 다른 버킷 타입/버전 대비 – generic fallback
                            Object k = b.key();
                            if (k instanceof co.elastic.clients.elasticsearch._types.FieldValue fv) {
                                if (fv.isString())      keyStr = fv.stringValue();
                                else if (fv.isLong())   keyStr = Long.toString(fv.longValue());
                                else if (fv.isDouble()) keyStr = Double.toString(fv.doubleValue());
                                else if (fv.isBoolean())keyStr = Boolean.toString(fv.booleanValue());
                                else                    keyStr = String.valueOf(k);
                            } else {
                                keyStr = String.valueOf(k);
                            }
                        }
                        if (keyStr == null) keyStr = "";

                        out.add(Map.of(
                                "keyword", keyStr,
                                "count", b.docCount(),
                                "lastAt", lastAt
                        ));
                    }
                }
                log.info("[ZERO] (direct) buckets={}", out.size());
                return out;
            } catch (Exception e) {
                log.error("[ZERO] (direct) ES error", e);
                return java.util.List.of();
            }
        }

    /* -------------------------------------------
     * 2) 최근 많이 본 게시물 (Views Top-N, 기간 필터)
     *    source: recipe (RecipeSearchDoc)
     * ------------------------------------------- */
    public List<Map<String, Object>> topViewed(int days, int size) {
        final int daysF = (days <= 0) ? 7 : days;
        final int sizeF = Math.min(Math.max(size, 1), 50);
        final Instant fromF = Instant.now().minus(daysF, ChronoUnit.DAYS);

        NativeQueryBuilder qb = NativeQuery.builder()
                .withQuery(Query.of(b -> b.bool(bb -> bb
                        .filter(f -> f.range(r -> r.field("createdAt")
                                .gte(JsonData.of(fromF.toString()))
                        ))
                        .filter(f -> f.term(t -> t.field("visibility").value("PUBLIC")))
                        .mustNot(m -> m.term(t -> t.field("deleted").value(true)))
                )))
                .withSort(s -> s.field(f -> f.field("views").order(SortOrder.Desc)))
                .withSort(s -> s.field(f -> f.field("createdAt").order(SortOrder.Desc)))
                .withPageable(PageRequest.of(0, sizeF));

        SearchHits<RecipeSearchDoc> hits = es.search(qb.build(), RecipeSearchDoc.class);

        List<Map<String, Object>> out = new ArrayList<>();
        for (SearchHit<RecipeSearchDoc> h : hits) {
            RecipeSearchDoc d = h.getContent();
            out.add(Map.of(
                    "id", h.getId(),
                    "title", d.getTitle(),
                    "authorNick", d.getAuthorNick(),
                    "views", d.getViews() == null ? 0 : d.getViews(),
                    "likes", d.getLikes() == null ? 0 : d.getLikes(),
                    "createdAt", d.getCreatedAt()
            ));
        }
        return out;
    }

    /* -------------------------------------------
     * 3) 최근 많이 업로드된 태그 (Trending Tags)
     * ------------------------------------------- */
    public List<Map<String, Object>> trendingTags(int days, int size) {
        final int daysF = (days <= 0) ? 30 : days;
        final int sizeF = Math.min(Math.max(size, 1), 100);
        final Instant fromF = Instant.now().minus(daysF, ChronoUnit.DAYS);

        NativeQueryBuilder qb = NativeQuery.builder()
                .withQuery(Query.of(b -> b.bool(bb -> bb
                        .filter(f -> f.range(r -> r.field("createdAt")
                                .gte(co.elastic.clients.json.JsonData.of(fromF.toString()))
                        ))
                        .filter(f -> f.term(t -> t.field("visibility").value("PUBLIC")))
                        .mustNot(m -> m.term(t -> t.field("deleted").value(true)))
                )))
                // 🔴 기존 "tags.keyword" → 🟢 "tags" 로 변경
                .withAggregation("tags", Aggregation.of(a -> a
                        .terms(t -> t.field("tags").size(sizeF))
                ))
                .withPageable(org.springframework.data.domain.PageRequest.of(0, 1));

        var hits = es.search(qb.build(), RecipeSearchDoc.class);
        Map<String, Aggregate> aggs = safeAggs(hits);

        List<Map<String, Object>> out = new ArrayList<>();
        Aggregate tags = aggs.get("tags");
        if (tags != null && tags.isSterms() && tags.sterms().buckets().isArray()) {
            tags.sterms().buckets().array().forEach(b ->
                    out.add(Map.of("tag", b.key(), "count", b.docCount()))
            );
        }
        return out;
    }

    /* -------------------------------------------
     * 4) 최근 좋아요 많이 받은 게시물 (Likes Top-N)
     * ------------------------------------------- */
    public List<Map<String, Object>> topLiked(int days, int size) {
        final int daysF = (days <= 0) ? 7 : days;
        final int sizeF = Math.min(Math.max(size, 1), 50);
        final Instant fromF = Instant.now().minus(daysF, ChronoUnit.DAYS);

        NativeQueryBuilder qb = NativeQuery.builder()
                .withQuery(Query.of(b -> b.bool(bb -> bb
                        .filter(f -> f.range(r -> r.field("createdAt")
                                .gte(JsonData.of(fromF.toString()))
                        ))
                        .filter(f -> f.term(t -> t.field("visibility").value("PUBLIC")))
                        .mustNot(m -> m.term(t -> t.field("deleted").value(true)))
                )))
                .withSort(s -> s.field(f -> f.field("likes").order(SortOrder.Desc)))
                .withSort(s -> s.field(f -> f.field("createdAt").order(SortOrder.Desc)))
                .withPageable(PageRequest.of(0, sizeF));

        SearchHits<RecipeSearchDoc> hits = es.search(qb.build(), RecipeSearchDoc.class);

        List<Map<String, Object>> out = new ArrayList<>();
        for (SearchHit<RecipeSearchDoc> h : hits) {
            RecipeSearchDoc d = h.getContent();
            out.add(Map.of(
                    "id", h.getId(),
                    "title", d.getTitle(),
                    "authorNick", d.getAuthorNick(),
                    "likes", d.getLikes() == null ? 0 : d.getLikes(),
                    "views", d.getViews() == null ? 0 : d.getViews(),
                    "createdAt", d.getCreatedAt()
            ));
        }
        return out;
    }

    /* -------------------------------------------
     * 5) 일자별 신규 업로드 수 (최근 N일)
     *    extended_bounds + min_doc_count=0 로 제로 집계 보장
     * ------------------------------------------- */
    // ---------- 5) 일자별 신규 업로드 수 (최근 N일, 자바에서 0 채우기) ----------
    public List<Map<String, Object>> uploadsByDay(int days) {
        final int daysF = (days <= 0) ? 30 : days;

        // [fromF, toF) : from은 오늘 00:00 - daysF, to는 내일 00:00
        final Instant toF   = Instant.now().truncatedTo(ChronoUnit.DAYS).plus(1, ChronoUnit.DAYS);
        final Instant fromF = toF.minus(daysF, ChronoUnit.DAYS);

        NativeQueryBuilder qb = NativeQuery.builder()
                .withQuery(Query.of(b -> b.bool(bb -> bb
                        .filter(f -> f.term(t -> t.field("visibility").value("PUBLIC")))
                        .mustNot(m -> m.term(t -> t.field("deleted").value(true)))
                        // 집계 대상 문서 범위를 제한 (버킷 갯수 줄여 성능도 이득)
                        .filter(f -> f.range(r -> r.field("createdAt")
                                .gte(co.elastic.clients.json.JsonData.of(fromF.toString()))
                                .lte(co.elastic.clients.json.JsonData.of(toF.toString()))
                        ))
                )))
                .withAggregation("by_day", Aggregation.of(a -> a.dateHistogram(h -> h
                        .field("createdAt")
                        .fixedInterval(fi -> fi.time("1d"))
                        .minDocCount(0) // 0 허용
                )))
                .withPageable(org.springframework.data.domain.PageRequest.of(0, 1));

        var hits = es.search(qb.build(), RecipeSearchDoc.class);

        // 1) ES가 준 버킷을 (LocalDate -> count) 맵으로
        java.util.Map<java.time.LocalDate, Long> raw = new java.util.HashMap<>();
        var aggs = safeAggs(hits);
        var dh = aggs.get("by_day");
        if (dh != null && dh.isDateHistogram() && dh.dateHistogram().buckets().isArray()) {
            dh.dateHistogram().buckets().array().forEach(b -> {
                // keyAsString: "2025-09-23T00:00:00.000Z" 형태
                String s = b.keyAsString();
                java.time.LocalDate day;
                try {
                    day = java.time.OffsetDateTime.parse(s).toLocalDate();
                } catch (Exception e) {
                    // 혹시 몰라 millis 키도 처리
                    day = java.time.Instant.ofEpochMilli(b.key())
                            .atOffset(java.time.ZoneOffset.UTC).toLocalDate();
                }
                raw.put(day, b.docCount());
            });
        }

        // 2) fromF~toF-1d 까지 하루 단위로 0 채우며 List 생성
        java.time.LocalDate fromD = fromF.atOffset(java.time.ZoneOffset.UTC).toLocalDate();
        java.time.LocalDate toD   = toF.atOffset(java.time.ZoneOffset.UTC).toLocalDate(); // exclusive
        java.util.List<java.util.Map<String,Object>> out = new java.util.ArrayList<>();
        for (java.time.LocalDate d = fromD; d.isBefore(toD); d = d.plusDays(1)) {
            long cnt = raw.getOrDefault(d, 0L);
            out.add(java.util.Map.of(
                    "date", d.toString(), // "yyyy-MM-dd"
                    "count", cnt
            ));
        }
        return out;
    }

    /* -------------------------------------------
     * 6) 상위 크리에이터 (최근 N일)
     *    terms(authorNick.keyword) + sum(likes/views)
     * ------------------------------------------- */
    public List<Map<String, Object>> topCreators(int days, int size) {
        final int daysF = (days <= 0) ? 30 : days;
        final int sizeF = Math.min(Math.max(size, 1), 50);
        final Instant fromF = Instant.now().minus(daysF, ChronoUnit.DAYS);

        NativeQueryBuilder qb = NativeQuery.builder()
                .withQuery(Query.of(b -> b.bool(bb -> bb
                        .filter(f -> f.range(r -> r.field("createdAt")
                                .gte(JsonData.of(fromF.toString()))
                        ))
                        .filter(f -> f.term(t -> t.field("visibility").value("PUBLIC")))
                        .mustNot(m -> m.term(t -> t.field("deleted").value(true)))
                )))
                .withAggregation("by_creator", Aggregation.of(a -> a
                        .terms(t -> t.field("authorNick.keyword").size(sizeF))
                        .aggregations("sum_likes", Aggregation.of(aa -> aa.sum(s -> s.field("likes"))))
                        .aggregations("sum_views", Aggregation.of(aa -> aa.sum(s -> s.field("views"))))
                ))
                .withPageable(PageRequest.of(0, 1));

        SearchHits<RecipeSearchDoc> hits = es.search(qb.build(), RecipeSearchDoc.class);
        Map<String, Aggregate> aggs = safeAggs(hits);

        List<Map<String, Object>> out = new ArrayList<>();
        Aggregate creators = aggs.get("by_creator");
        if (creators != null && creators.isSterms() && creators.sterms().buckets().isArray()) {
            creators.sterms().buckets().array().forEach(b -> {
                long posts = b.docCount();
                double sumLikes = 0, sumViews = 0;
                Map<String, Aggregate> sub = b.aggregations();
                if (sub != null) {
                    Aggregate sl = sub.get("sum_likes");
                    if (sl != null && sl.isSum()) {
                        double v = sl.sum().value();           // primitive double
                        if (!Double.isNaN(v)) sumLikes = v;    // 문서가 없으면 NaN일 수 있음
                    }
                    Aggregate sv = sub.get("sum_views");
                    if (sv != null && sv.isSum()) {
                        double v = sv.sum().value();
                        if (!Double.isNaN(v)) sumViews = v;
                    }
                }

                out.add(Map.of(
                        "authorNick", b.key(),
                        "posts", posts,
                        "sumLikes", (long) sumLikes,
                        "sumViews", (long) sumViews
                ));
            });
        }
        return out;
    }


    private static Map<String, co.elastic.clients.elasticsearch._types.aggregations.Aggregate> safeAggs(
            org.springframework.data.elasticsearch.core.SearchHits<?> hits) {

        var container = hits.getAggregations();
        if (container == null) return java.util.Map.of();

        // SDE 버전마다 Aggregations 구현이 달라서 최대한 폭넓게 처리
        Object raw = container;

        // 1) 가장 흔한 케이스: Aggregations에 public Map 메서드가 존재
        for (String mName : new String[]{"aggregations", "asMap", "getAsMap"}){
            try {
                var m = raw.getClass().getMethod(mName);
                Object maybeMap = m.invoke(raw);
                if (maybeMap instanceof java.util.Map<?, ?> map) {
                    java.util.Map<String, co.elastic.clients.elasticsearch._types.aggregations.Aggregate> out = new java.util.HashMap<>();
                    extractFromMap(map, out);
                    return out;
                }
            } catch (Exception ignore) {}
        }

        // 2) 혹시 container가 곧바로 Map인 경우 (드물지만 방어)
        if (raw instanceof java.util.Map<?, ?> map) {
            java.util.Map<String, co.elastic.clients.elasticsearch._types.aggregations.Aggregate> out = new java.util.HashMap<>();
            extractFromMap(map, out);
            return out;
        }

        // 3) 최후 방어: 내부에 map 비슷한 필드가 있는지 스캔
        try {
            for (var f : raw.getClass().getDeclaredFields()) {
                f.setAccessible(true);
                Object v = f.get(raw);
                if (v instanceof java.util.Map<?, ?> map) {
                    java.util.Map<String, co.elastic.clients.elasticsearch._types.aggregations.Aggregate> out = new java.util.HashMap<>();
                    extractFromMap(map, out);
                    if (!out.isEmpty()) return out;
                }
            }
        } catch (Exception ignore) {}

        return java.util.Map.of();
    }

    @SuppressWarnings("unchecked")
    private static void extractFromMap(java.util.Map<?, ?> map,
                                       java.util.Map<String, co.elastic.clients.elasticsearch._types.aggregations.Aggregate> out) {

        for (var e : map.entrySet()) {
            String key = String.valueOf(e.getKey());
            Object v = e.getValue();

            // 1) 이미 Aggregate
            if (v instanceof co.elastic.clients.elasticsearch._types.aggregations.Aggregate a) {
                out.put(key, a);
                continue;
            }

            // 2) Spring 래퍼(예: org.springframework.data.elasticsearch.client.elc.Aggregation) → 퍼블릭 getter 우선
            for (String mName : new String[]{"aggregate", "getAggregate", "aggregation", "getAggregation"}) {
                try {
                    var m = v.getClass().getMethod(mName);
                    Object inner = m.invoke(v);
                    if (inner instanceof co.elastic.clients.elasticsearch._types.aggregations.Aggregate a) {
                        out.put(key, a);
                        v = null;
                    }
                    break; // 위 메서드들 중 하나만 시도
                } catch (Exception ignore) {}
            }
            if (out.containsKey(key)) continue;

            // 3) private 필드 fallback (aggregate / aggregation)
            for (String fName : new String[]{"aggregate", "aggregation"}) {
                try {
                    var f = v.getClass().getDeclaredField(fName);
                    f.setAccessible(true);
                    Object inner = f.get(v);
                    if (inner instanceof co.elastic.clients.elasticsearch._types.aggregations.Aggregate a) {
                        out.put(key, a);
                        break;
                    }
                } catch (Exception ignore) {}
            }
        }
    }
}
