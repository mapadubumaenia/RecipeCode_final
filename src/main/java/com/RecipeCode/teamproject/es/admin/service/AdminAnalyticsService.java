package com.RecipeCode.teamproject.es.admin.service;

import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.aggregations.CalendarInterval;
import co.elastic.clients.elasticsearch._types.aggregations.FieldDateMath;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchResponse;
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



import java.io.IOException;

import org.springframework.util.StringUtils;

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
                        .filter(f -> f.range(r -> r.field("updatedAt")
                                .gte(JsonData.of(fromF.toString()))
                        ))
                        .filter(f -> f.term(t -> t.field("visibility").value("PUBLIC")))
                        .mustNot(m -> m.term(t -> t.field("deleted").value(true)))
                )))
                .withSort(s -> s.field(f -> f.field("views").order(SortOrder.Desc)))
                .withSort(s -> s.field(f -> f.field("updatedAt").order(SortOrder.Desc)))
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

    // 3) 최근 많이 업로드된 태그 (Trending Tags)  -- 메서드 전체 교체
    public List<Map<String, Object>> trendingTags(int days, int size) {
        final int daysF = (days <= 0) ? 30 : days;
        final int sizeF = Math.min(Math.max(size, 1), 100);
        final Instant fromF = Instant.now().minus(daysF, ChronoUnit.DAYS);

        try {
            // ---- 진단: 문서 개수(기간/가시성/삭제조건) ----
            var baseBool = new co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery.Builder()
                    .filter(f -> f.term(t -> t.field("visibility").value("PUBLIC")))
                    .mustNot(m -> m.term(t -> t.field("deleted").value(true)))
                    .filter(f -> f.range(r -> r.field("createdAt")
                            .gte(co.elastic.clients.json.JsonData.of(fromF.toString()))))
                    .build();

            long total = esClient.count(c -> c.index("recipe").query(q -> q.bool(baseBool))).count();
            long hasTags = esClient.count(c -> c.index("recipe").query(q -> q.bool(b -> b
                    .filter(baseBool.filter())
                    .must(baseBool.must())
                    .mustNot(baseBool.mustNot())
                    .filter(f -> f.exists(e -> e.field("tags")))
            ))).count();
            long hasTagsCsv = esClient.count(c -> c.index("recipe").query(q -> q.bool(b -> b
                    .filter(baseBool.filter())
                    .must(baseBool.must())
                    .mustNot(baseBool.mustNot())
                    .filter(f -> f.exists(e -> e.field("TAGSCSV")))
            ))).count();

            log.info("[TRENDS] total={}, has(tags)={}, has(TAGSCSV)={}, from={}", total, hasTags, hasTagsCsv, fromF);

            // ---- 집계 실행 함수 ----
            java.util.function.Function<String, List<Map<String,Object>>> runAgg = (String field) -> {
                var req = new co.elastic.clients.elasticsearch.core.SearchRequest.Builder()
                        .index("recipe")
                        .size(0)
                        .query(q -> q.bool(baseBool))
                        .aggregations("tags", a -> a.terms(t -> t.field(field).size(sizeF)))
                        .build();

                SearchResponse<RecipeSearchDoc> resp = null;
                try {
                    resp = esClient.search(req, RecipeSearchDoc.class);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                var agg = resp.aggregations().get("tags");
                var buckets = (agg == null || agg.sterms() == null) ? null : agg.sterms().buckets();

                var out = new ArrayList<Map<String,Object>>();
                if (buckets != null && buckets.isArray()) {
                    for (var b : buckets.array()) {
                        String tag = null;

                        // 1) 키 추출: StringTermsBucket의 key()는 FieldValue 인 경우가 많음
                        Object k = b.key();
                        if (k instanceof co.elastic.clients.elasticsearch._types.FieldValue fv) {
                            if (fv.isString())      tag = fv.stringValue();
                            else if (fv.isLong())   tag = Long.toString(fv.longValue());
                            else if (fv.isDouble()) tag = Double.toString(fv.doubleValue());
                            else if (fv.isBoolean())tag = Boolean.toString(fv.booleanValue());
                            else                    tag = String.valueOf(k);
                        } else if (k instanceof String s) {
                            tag = s;
                        } else if (k != null) {
                            tag = k.toString();
                        }

                        // 2) 필터: 빈값/아이디 스타일 제외
                        if (tag == null || tag.isBlank() || tag.charAt(0) == '@') continue;

                        out.add(Map.of("tag", tag, "count", b.docCount()));
                    }
                }
                log.info("[TRENDS] field='{}' buckets={}", field, out.size());
                return out;
            };

            // 1차: tags
            List<Map<String,Object>> out = runAgg.apply("tags");
            if (out.isEmpty()) {
                // 2차: TAGSCSV.keyword (과거 CSV 필드가 실제로 들어있는지 확인)
                out = runAgg.apply("TAGSCSV.keyword");
            }
            return out;
        } catch (Exception e) {
            log.error("[TRENDS] ES error", e);
            return List.of();
        }
    }


    /* -------------------------------------------
     * 4) 최근 좋아요 많이 받은 게시물 (Likes Top-N)
     * ------------------------------------------- */
    // AdminAnalyticsService.java
    public List<Map<String, Object>> topLiked(int days, int size) {
        final int daysF = (days <= 0) ? 7 : days;
        final int sizeF = Math.min(Math.max(size, 1), 50);
        final Instant fromF = Instant.now().minus(daysF, ChronoUnit.DAYS);


        NativeQueryBuilder qb = NativeQuery.builder()
                .withQuery(Query.of(b -> b.bool(bb -> bb
                        .filter(f -> f.range(r -> r.field("updatedAt").gte(JsonData.of(fromF.toString()))))
                        .filter(f -> f.term(t -> t.field("visibility").value("PUBLIC")))
                        .mustNot(m -> m.term(t -> t.field("deleted").value(true)))
                )))
                .withSort(s -> s.field(f -> f.field("likes").order(SortOrder.Desc)))
                .withSort(s -> s.field(f -> f.field("updatedAt").order(SortOrder.Desc)))
                .withPageable(PageRequest.of(0, sizeF));

        SearchHits<RecipeSearchDoc> hits = es.search(qb.build(), RecipeSearchDoc.class);

        List<Map<String, Object>> out = new ArrayList<>();
        for (SearchHit<RecipeSearchDoc> h : hits) {
            RecipeSearchDoc d = h.getContent();
            Media media = buildMedia(d); // 👈 아래 유틸 추가

            out.add(Map.of(
                    "id", h.getId(),
                    "title", nvl(d.getTitle()),
                    "authorNick", nvl(d.getAuthorNick()),
                    "likes", d.getLikes() == null ? 0L : d.getLikes(),
                    "comments", d.getComments() == null ? 0L : d.getComments(),  // ✅ 댓글 포함
                    "views", d.getViews() == null ? 0L : d.getViews(),
                    "createdAt", d.getCreatedAt() == null ? "" : d.getCreatedAt().toString(),
                    // 미디어 메타 (라이트 유튜브/비디오/이미지)
                    "mediaKind", media.kind,
                    "mediaSrc",  media.src,
                    "poster",    media.poster == null ? "" : media.poster
            ));
        }
        return out;
    }

    /* ====== 아래 유틸(FeedService와 동일한 로직 요약본) ====== */
    private static class Media { final String kind, src, poster; Media(String k,String s,String p){kind=k;src=s;poster=p;} }
    private Media buildMedia(RecipeSearchDoc d) {
        String thumb = d.getThumbUrl();
        String video = d.getVideoUrl();

        if (StringUtils.hasText(video)) {
            String vid = extractYouTubeId(video);
            if (vid != null) {
                String embed = "https://www.youtube.com/embed/" + vid + "?playsinline=1&modestbranding=1&rel=0";
                String poster = "https://i.ytimg.com/vi/" + vid + "/hqdefault.jpg";
                return new Media("youtube", embed, poster);
            }
            String v = video.toLowerCase();
            if (v.endsWith(".mp4") || v.endsWith(".webm") || v.endsWith(".mov") || v.endsWith(".m4v")) {
                String poster = (StringUtils.hasText(thumb) && !looksLikeYouTubeUrl(thumb)) ? thumb : null;
                return new Media("video", video, poster);
            }
        }
        if (StringUtils.hasText(thumb) && !looksLikeYouTubeUrl(thumb)) {
            return new Media("image", thumb, null);
        }
        String vid = extractYouTubeId(video);
        if (vid != null) {
            String poster = "https://i.ytimg.com/vi/" + vid + "/hqdefault.jpg";
            return new Media("image", poster, null);
        }
        return new Media("image", "", null);
    }
    private String extractYouTubeId(String url) {
        if (!StringUtils.hasText(url)) return null;
        var m = java.util.regex.Pattern.compile("[?&]v=([A-Za-z0-9_-]{11})").matcher(url);
        if (m.find()) return m.group(1);
        m = java.util.regex.Pattern.compile("youtu\\.be/([A-Za-z0-9_-]{11})").matcher(url);
        if (m.find()) return m.group(1);
        m = java.util.regex.Pattern.compile("/(shorts|embed)/([A-Za-z0-9_-]{11})").matcher(url);
        if (m.find()) return m.group(2);
        return null;
    }
    private boolean looksLikeYouTubeUrl(String url){
        if (!StringUtils.hasText(url)) return false;
        String u = url.toLowerCase();
        return u.contains("youtube.com") || u.contains("youtu.be");
    }
    private static String nvl(String s){ return (s==null) ? "" : s; }


    // ---------- 5) 일자별 신규 업로드 수 (최근 N일, KST 기준, 자바에서 0 채우기) ----------
    public List<Map<String, Object>> uploadsByDay(int days) {
        final int daysF = (days <= 0) ? 30 : days;

        // KST(Asia/Seoul) 기준 자정 경계 계산
        final java.time.ZoneId KST = java.time.ZoneId.of("Asia/Seoul");
        final java.time.ZonedDateTime nowKst = java.time.ZonedDateTime.now(KST).truncatedTo(java.time.temporal.ChronoUnit.DAYS);
        final java.time.ZonedDateTime toKst   = nowKst.plusDays(1);          // 내일 00:00 (exclusive)
        final java.time.ZonedDateTime fromKst = toKst.minusDays(daysF);      // 시작일 00:00 (inclusive)
        final java.time.Instant fromI = fromKst.toInstant();
        final java.time.Instant toI   = toKst.toInstant();

        try {
            // createdAt ∈ [fromI, toI) + 공개/삭제 필터
            var baseBool = new co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery.Builder()
                    .filter(f -> f.term(t -> t.field("visibility").value("PUBLIC")))
                    .mustNot(m -> m.term(t -> t.field("deleted").value(true)))
                    .filter(f -> f.range(r -> r.field("createdAt")
                            .gte(co.elastic.clients.json.JsonData.of(fromI.toString()))
                            .lt (co.elastic.clients.json.JsonData.of(toI.toString()))
                    ))
                    .build();

            var req = new co.elastic.clients.elasticsearch.core.SearchRequest.Builder()
                    .index("recipe")
                    .size(0) // 문서는 불필요, 집계만
                    .query(q -> q.bool(baseBool))
                    .aggregations("by_day", a -> a.dateHistogram(h -> h
                                    .field("createdAt")
                                    .calendarInterval(co.elastic.clients.elasticsearch._types.aggregations.CalendarInterval.Day)
                                    .timeZone("+09:00") // KST 경계로 버킷팅
                            // minDocCount(0)은 extended_bounds 없으면 빈 버킷을 만들지 않으니
                            // 우리는 아래에서 자바로 0을 채움
                    ))
                    .build();

            var resp = esClient.search(req, com.RecipeCode.teamproject.es.search.document.RecipeSearchDoc.class);

            // ES 버킷 -> (LocalDate -> count) 매핑
            java.util.Map<java.time.LocalDate, Long> raw = new java.util.HashMap<>();
            var agg = resp.aggregations().get("by_day");
            if (agg != null && agg.dateHistogram() != null && agg.dateHistogram().buckets().isArray()) {
                for (var b : agg.dateHistogram().buckets().array()) {
                    java.time.LocalDate day;
                    String ks = b.keyAsString(); // 예: "2025-09-24T00:00:00.000+09:00"
                    try {
                        day = java.time.OffsetDateTime.parse(ks).toLocalDate();
                    } catch (Exception e) {
                        // 혹시 문자열 파싱 실패 시 epoch millis로 복구
                        day = java.time.Instant.ofEpochMilli(b.key())
                                .atZone(KST).toLocalDate();
                    }
                    raw.put(day, b.docCount());
                }
            }

            // [fromKst, toKst) 구간을 하루씩 돌며 0 채워서 리스트 생성
            java.util.List<java.util.Map<String, Object>> out = new java.util.ArrayList<>();
            for (java.time.LocalDate d = fromKst.toLocalDate(); d.isBefore(toKst.toLocalDate()); d = d.plusDays(1)) {
                long cnt = raw.getOrDefault(d, 0L);
                out.add(java.util.Map.of("date", d.toString(), "count", cnt));
            }
            return out;

        } catch (Exception e) {
            log.error("[UPLOADS_BY_DAY] ES error", e);
            // 에러 시에도 요청 구간만큼 0으로 채워 반환 (UI 안정성)
            java.util.List<java.util.Map<String, Object>> fallback = new java.util.ArrayList<>();
            for (java.time.LocalDate d = fromKst.toLocalDate(); d.isBefore(toKst.toLocalDate()); d = d.plusDays(1)) {
                fallback.add(java.util.Map.of("date", d.toString(), "count", 0L));
            }
            return fallback;
        }
    }


    // 6) 상위 크리에이터 (최근 N일)  -- 메서드 전체 교체
    public List<Map<String, Object>> topCreators(int days, int size) {
        final int daysF = (days <= 0) ? 30 : days;
        final int sizeF = Math.min(Math.max(size, 1), 50);
        final Instant fromF = Instant.now().minus(daysF, ChronoUnit.DAYS);

        try {
            // 공통 필터
            var baseBool = new co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery.Builder()
                    .filter(f -> f.term(t -> t.field("visibility").value("PUBLIC")))
                    .mustNot(m -> m.term(t -> t.field("deleted").value(true)))
                    .filter(f -> f.range(r -> r.field("createdAt")
                            .gte(co.elastic.clients.json.JsonData.of(fromF.toString()))))
                    .build();

            // 진단 로그(선택)
            long hasAuthor = esClient.count(c -> c.index("recipe")
                    .query(q -> q.bool(b -> b
                            .filter(baseBool.filter())
                            .must(baseBool.must())
                            .mustNot(baseBool.mustNot())
                            .filter(f -> f.exists(e -> e.field("authorNick")))))).count();
            log.info("[CREATORS] from={}, days={}, docsWithAuthorNick={}", fromF, daysF, hasAuthor);

            // 집계 실행 함수 (필드명 바꿔 재시도 가능)
            java.util.function.Function<String, List<Map<String,Object>>> runAgg = (String field) -> {
                var req = new co.elastic.clients.elasticsearch.core.SearchRequest.Builder()
                        .index("recipe")
                        .size(0)
                        .query(q -> q.bool(baseBool))
                        .aggregations("by_creator", a -> a
                                .terms(t -> t.field(field).size(sizeF))
                                .aggregations("sum_likes", aa -> aa.sum(s -> s.field("likes")))
                                .aggregations("sum_views", aa -> aa.sum(s -> s.field("views")))
                        )
                        .build();

                SearchResponse<RecipeSearchDoc> resp = null;
                try {
                    resp = esClient.search(req, RecipeSearchDoc.class);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                var agg  = resp.aggregations().get("by_creator");
                var buckets = (agg == null || agg.sterms() == null) ? null : agg.sterms().buckets();

                var out = new ArrayList<Map<String,Object>>();
                if (buckets != null && buckets.isArray()) {
                    for (var b : buckets.array()) {
                        // 키 안전 추출 (FieldValue 대응)
                        String author = null;
                        Object k = b.key();
                        if (k instanceof co.elastic.clients.elasticsearch._types.FieldValue fv) {
                            if (fv.isString())      author = fv.stringValue();
                            else if (fv.isLong())   author = Long.toString(fv.longValue());
                            else if (fv.isDouble()) author = Double.toString(fv.doubleValue());
                            else if (fv.isBoolean())author = Boolean.toString(fv.booleanValue());
                            else                    author = String.valueOf(k);
                        } else if (k instanceof String s) {
                            author = s;
                        } else if (k != null) {
                            author = k.toString();
                        }
                        if (author == null || author.isBlank()) continue;

                        // 합계 파싱 (sum은 double일 수 있음 → long으로 캐스팅)
                        double sumLikesD = 0, sumViewsD = 0;
                        var sub = b.aggregations();
                        if (sub != null) {
                            var sl = sub.get("sum_likes");
                            if (sl != null && sl.isSum() && !Double.isNaN(sl.sum().value()))
                                sumLikesD = sl.sum().value();
                            var sv = sub.get("sum_views");
                            if (sv != null && sv.isSum() && !Double.isNaN(sv.sum().value()))
                                sumViewsD = sv.sum().value();
                        }

                        out.add(Map.of(
                                "authorNick", author,
                                "posts", b.docCount(),
                                "sumLikes", (long) sumLikesD,
                                "sumViews", (long) sumViewsD
                        ));
                    }
                }
                log.info("[CREATORS] field='{}' buckets={}", field, out.size());
                return out;
            };

            // 1차: 매핑 그대로 (authorNick은 keyword라 .keyword 불필요)
            List<Map<String,Object>> out = runAgg.apply("authorNick");

            // 혹시나 비어있으면 레거시 매핑 대비로 한 번 더 시도
            if (out.isEmpty()) out = runAgg.apply("authorNick.keyword");

            return out;
        } catch (Exception e) {
            log.error("[CREATORS] ES error", e);
            return List.of();
        }
    }



    /**  7 트래픽(검색 로그 수) 스파크라인 */
    public List<Map<String, Object>> traffic(Instant from, Instant to, String intervalRaw) {
        final Instant toF   = (to   == null) ? Instant.now() : to;
        final Instant fromF = (from == null) ? toF.minus(24, ChronoUnit.HOURS) : from;

        // 캘린더 간격만 사용 (Minute / Hour / Day)
        CalendarInterval cal = parseCalendarInterval(intervalRaw);

        var req = new co.elastic.clients.elasticsearch.core.SearchRequest.Builder()
                .index("rc-search-logs-000001")
                .size(0)
                .query(q -> q.range(r -> r
                        .field("at")
                        .gte(co.elastic.clients.json.JsonData.of(fromF.toString()))
                        .lte(co.elastic.clients.json.JsonData.of(toF.toString()))
                ))
                .aggregations("by_time", a -> a.dateHistogram(dh -> dh
                        .field("at")
                        .calendarInterval(cal)   // ← calendar interval만 사용
                        .minDocCount(0)
                        .timeZone("+09:00")     // KST 경계로 보고 싶으면 유지
                ))
                .build();

        try {
            var resp  = esClient.search(req, com.RecipeCode.teamproject.es.search.document.SearchLogDoc.class);
            var agg   = resp.aggregations().get("by_time");
            var bucks = (agg != null && agg.dateHistogram() != null) ? agg.dateHistogram().buckets() : null;

            List<Map<String,Object>> out = new ArrayList<>();
            if (bucks != null && bucks.isArray()) {
                for (var b : bucks.array()) {
                    long count = b.docCount();                       // primitive long
                    String iso  = Instant.ofEpochMilli(b.key()).toString(); // epoch → ISO
                    out.add(Map.of("ts", iso, "views", count));
                }
            }
            return out;
        } catch (Exception e) {
            log.error("[TRAFFIC] ES error", e);
            return List.of();
        }
    }

    /** interval 문자열 → CalendarInterval 로만 매핑 */
    private CalendarInterval parseCalendarInterval(String raw) {
        if (raw == null || raw.isBlank()) return CalendarInterval.Hour;
        String s = raw.trim().toLowerCase();
        switch (s) {
            case "minute":
            case "min":
            case "1m":   return CalendarInterval.Minute;
            case "hour":
            case "hr":
            case "1h":   return CalendarInterval.Hour;
            case "day":
            case "d":
            case "1d":   return CalendarInterval.Day;
            default:
                // 15m, 30m, 2h 같은 고정 간격은 A안에서는 지원하지 않음 → 가장 가까운 캘린더 간격으로 폴백
                if (s.endsWith("m")) return CalendarInterval.Minute;
                if (s.endsWith("h")) return CalendarInterval.Hour;
                if (s.endsWith("d")) return CalendarInterval.Day;
                return CalendarInterval.Hour;
        }
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
