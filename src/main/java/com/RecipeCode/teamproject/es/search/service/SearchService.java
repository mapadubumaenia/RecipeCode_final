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
import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class SearchService {

    private final ElasticsearchOperations es;
    private final SearchLogService logs;

    // ✅ 해시태그 추출 패턴 (한글/영문/숫자/언더스코어/하이픈)
    private static final Pattern HASHTAG = Pattern.compile("#([\\p{L}\\p{N}_-]+)");

    // ✅ tags 정확일치에 사용할 후보 필드들(매핑 차이 안전 대비)
    private static final String TAGS_FIELD_KEYWORD = "tags.keyword";
    private static final String TAGS_FIELD = "tags";

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
        filters.add(Query.of(b -> b.bool(bb -> bb.mustNot(mn -> mn.term(t -> t.field("deleted").value(true))))));
        if (tags != null && !tags.isEmpty()) {
            List<String> cleaned = tags.stream()
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(s -> s.startsWith("#") ? s.substring(1) : s)
                    .toList();
            filters.add(orTermsOnTagFields(cleaned));
        }
        Query boolQuery = Query.of(b -> b.bool(bb -> bb.must(main).filter(filters)));

        // 3) NativeQuery
        NativeQueryBuilder qb = NativeQuery.builder()
                .withQuery(boolQuery)
                .withPageable(PageRequest.of(0, size));

        // 4) 집계
        qb.withAggregation("tags", Aggregation.of(a -> a.terms(t -> t.field(TAGS_FIELD_KEYWORD).size(20))));
        qb.withAggregation("by_day", Aggregation.of(a -> a.dateHistogram(h -> h
                .field("createdAt")
                .fixedInterval(fi -> fi.time("1d")))));

        // 5) 정렬 + 커서
        boolean cursorSortable = false;
        if ("hot".equalsIgnoreCase(sort)) {
            qb.withSort(s -> s.field(f -> f.field("likes").order(SortOrder.Desc)));
            qb.withSort(s -> s.field(f -> f.field("createdAt").order(SortOrder.Desc)));
            qb.withSort(s -> s.field(f -> f.field("id").order(SortOrder.Desc)));
            cursorSortable = true;
        } else if ("new".equalsIgnoreCase(sort) || !StringUtils.hasText(sort)) {
            qb.withSort(s -> s.field(f -> f.field("createdAt").order(SortOrder.Desc)));
            qb.withSort(s -> s.field(f -> f.field("id").order(SortOrder.Desc)));
            cursorSortable = true;
        }

        if (cursorSortable) {
            List<Object> afterValues = CursorUtil.decode(after);
            if (afterValues != null) qb.withSearchAfter(afterValues);
        }

        // 6) 실행
        var nq = qb.build();
        SearchHits<RecipeSearchDoc> hits = es.search(nq, RecipeSearchDoc.class);

// 7) 매핑
        var items = hits.getSearchHits().stream().map(h -> {
            var d = h.getContent();
            var m = new LinkedHashMap<String, Object>(16);
            m.put("id", h.getId());
            m.put("title", d.getTitle() != null ? d.getTitle() : "");
            m.put("tags", d.getTags() != null ? d.getTags() : List.of());
            m.put("authorId",  d.getAuthorId()  != null ? d.getAuthorId()  : "");
            m.put("authorEmail", d.getAuthorEmail() != null ? d.getAuthorEmail() : "");  // ★ 추가
            m.put("likes", d.getLikes() != null ? d.getLikes() : 0L);
            m.put("createdAt", d.getCreatedAt());
            m.put("score", h.getScore());
            m.put("thumbUrl", resolveThumb(d)); // 레거시/폴백
            m.put("comments", d.getComments() != null ? d.getComments() : 0L);
            m.put("views", d.getViews() != null ? d.getViews() : 0L);

            // ⭐ 라이트 유튜브용 메타
            Media media = buildMedia(d);
            m.put("mediaKind", media.kind());   // youtube | video | image
            m.put("mediaSrc", media.src());     // youtube: embed URL, video: mp4 URL, image: img URL
            m.put("poster", media.poster());    // 썸네일/포스터

            return m;
        }).toList();

// 8) next
        String next = null;
        if (cursorSortable && !hits.getSearchHits().isEmpty()) {
            var last = hits.getSearchHits().get(hits.getSearchHits().size() - 1);
            var sortVals = last.getSortValues();
            if (sortVals != null && !sortVals.isEmpty()) {
                next = CursorUtil.encode(sortVals);
            }
        }

/* ✅ 8.5) 집계 추출 (제로 집계 보장)
   - 결과가 0이어도 아래 구조를 항상 내려줌:
     "aggs": { "tags": [], "by_day": [] }
*/
        // ✅ 집계 추출 (제로 집계 보장)
        Map<String, Object> aggsOut = new LinkedHashMap<>();
        try {
            var aggContainer = hits.getAggregations(); // Spring Data ES의 AggregationsContainer
            @SuppressWarnings("unchecked")
            Map<String, Aggregate> aggs =
                    (aggContainer != null) ? (Map<String, Aggregate>) aggContainer.aggregations() : null;

            // terms: tags
            List<Map<String, Object>> tagsBuckets = new ArrayList<>();
            Aggregate tagsAgg = (aggs != null) ? aggs.get("tags") : null;
            if (tagsAgg != null && tagsAgg.isSterms()) {
                var buckets = tagsAgg.sterms().buckets();
                if (buckets != null && buckets.isArray()) {
                    buckets.array().forEach(b -> {
                        tagsBuckets.add(Map.of(
                                "key", b.key(),
                                "docCount", b.docCount()
                        ));
                    });
                }
            }
            aggsOut.put("tags", tagsBuckets); // 비어도 []

            // date_histogram: by_day
            List<Map<String, Object>> dayBuckets = new ArrayList<>();
            Aggregate dayAgg = (aggs != null) ? aggs.get("by_day") : null;
            if (dayAgg != null && dayAgg.isDateHistogram()) {
                var buckets = dayAgg.dateHistogram().buckets();
                if (buckets != null && buckets.isArray()) {
                    buckets.array().forEach(b -> {
                        dayBuckets.add(Map.of(
                                "key", b.key(),                 // epoch millis
                                "keyAsString", b.keyAsString(), // ISO 문자열
                                "docCount", b.docCount()
                        ));
                    });
                }
            }
            aggsOut.put("by_day", dayBuckets); // 비어도 []
        } catch (Exception ignore) {
            aggsOut.put("tags", List.of());
            aggsOut.put("by_day", List.of());
        }

// ✅ 응답에 포함
        var res = new LinkedHashMap<String, Object>();
        res.put("total", hits.getTotalHits());
        res.put("items", items);
        res.put("next", next);
        res.put("aggs", aggsOut);

// 10) 로그
        logs.log(q, tags, sort, 0, size, hits.getTotalHits(), null);

        return res;

    }

    /** q 규칙 */
    private Query buildMainQuery(String q) {
        String qv = (q == null) ? null : q.trim();
        if (!StringUtils.hasText(qv)) {
            return Query.of(b -> b.matchAll(m -> m));
        }

        // 해시태그
        List<String> hashtags = extractHashtags(qv);
        if (!hashtags.isEmpty()) {
            return Query.of(b -> b.bool(bb -> {
                for (String tagCore : hashtags) {
                    final String val = tagCore;
                    bb.must(m -> m.bool(sb -> sb
                            .should(s1 -> s1.term(t1 -> t1.field(TAGS_FIELD_KEYWORD).value(val)))
                            .should(s2 -> s2.term(t2 -> t2.field(TAGS_FIELD).value(val)))
                    ));
                }
                return bb;
            }));
        }

        if (qv.startsWith("@") && qv.length() > 1) {
            String tokenRaw = qv.substring(1).trim(); // 사용자가 친 @제거한 코어
            String tokenWithAt = "@" + tokenRaw;      // ES에 저장된 값(@ 포함)

            // 둘 다 시도 (저장 규칙 바뀌더라도 안전)
            return Query.of(b -> b.bool(bb -> bb
                    .should(s -> s.term(t -> t.field("authorId").value(tokenWithAt))) // "@mafa"
                    .should(s -> s.term(t -> t.field("authorId").value(tokenRaw)))    // "mafa"
                    .minimumShouldMatch("1")
            ));
        }

        // 일반 검색
        return Query.of(b -> b.bool(bb -> bb
                .should(s -> s.multiMatch(mm -> mm
                        .query(qv)
                        .fields("title^3", "body", "ingredients^2")
                        .type(TextQueryType.BestFields)))
                .should(s -> s.multiMatch(mm -> mm
                        .query(qv)
                        .fields("title^3", "body")
                        .type(TextQueryType.BoolPrefix)))
                .minimumShouldMatch("1")));
    }

    private static List<String> extractHashtags(String q) {
        if (!StringUtils.hasText(q)) return List.of();
        Matcher m = HASHTAG.matcher(q);
        List<String> out = new ArrayList<>();
        while (m.find()) {
            String core = m.group(1).trim();
            if (!core.isEmpty()) out.add(core);
        }
        return out;
    }

    private static Query orTermsOnTagFields(List<String> plainTags) {
        List<FieldValue> vals = plainTags.stream().map(FieldValue::of).toList();
        return Query.of(b -> b.bool(bb -> bb
                .should(s -> s.terms(t -> t.field(TAGS_FIELD_KEYWORD).terms(v -> v.value(vals))))
                .should(s -> s.terms(t -> t.field(TAGS_FIELD).terms(v -> v.value(vals))))
        ));
    }

    /** 쇼츠(트렌딩) */
    public Map<String, Object> trending(String after, int size) {
        size = Math.min(Math.max(size, 1), 20);

        var qb = NativeQuery.builder()
                .withQuery(Query.of(b -> b.bool(bb -> bb
                        .filter(f -> f.term(t -> t.field("visibility").value("PUBLIC")))
                        .filter(f -> f.bool(b2 -> b2.mustNot(mn -> mn.term(t -> t.field("deleted").value(true)))))
                )))
                .withPageable(PageRequest.of(0, size))
                .withSort(s -> s.field(f -> f.field("likes").order(SortOrder.Desc)))
                .withSort(s -> s.field(f -> f.field("createdAt").order(SortOrder.Desc)))
                .withSort(s -> s.field(f -> f.field("id").order(SortOrder.Desc)));

        var afterValues = CursorUtil.decode(after);
        if (afterValues != null) qb.withSearchAfter(afterValues);

        SearchHits<RecipeSearchDoc> hits = es.search(qb.build(), RecipeSearchDoc.class);

        var items = hits.getSearchHits().stream().map(h -> {
            var d = h.getContent();
            var m = new LinkedHashMap<String, Object>(16);
            m.put("id", h.getId());
            m.put("title", d.getTitle() != null ? d.getTitle() : "");
            m.put("authorId", d.getAuthorId() != null ? d.getAuthorId() : "");
            m.put("authorEmail", d.getAuthorEmail() != null ? d.getAuthorEmail() : "");  // ★ 추가
            m.put("likes", d.getLikes() != null ? d.getLikes() : 0L);
            m.put("createdAt", d.getCreatedAt());
            m.put("tags", d.getTags() != null ? d.getTags() : List.of());
            m.put("thumbUrl", resolveThumb(d)); // 레거시/폴백

            // ⭐ 라이트 유튜브용 메타
            Media media = buildMedia(d);
            m.put("mediaKind", media.kind());
            m.put("mediaSrc", media.src());
            m.put("poster", media.poster());

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

    // ===============================
    // 썸네일/미디어 보정 유틸
    // ===============================

    /** 레거시: 이미지 썸네일 폴백 */
    private String resolveThumb(RecipeSearchDoc d) {
        String t = d.getThumbUrl();
        if (StringUtils.hasText(t) && !looksLikeYouTubeUrl(t)) {
            return t;
        }
        String vid = extractYouTubeId(d.getVideoUrl());
        if (vid != null) {
            return "https://i.ytimg.com/vi/" + vid + "/hqdefault.jpg";
        }
        return (t != null) ? t : "";
    }

    private boolean looksLikeYouTubeUrl(String url) {
        if (!StringUtils.hasText(url)) return false;
        String u = url.toLowerCase();
        return u.contains("youtube.com") || u.contains("youtu.be");
    }

    /** 다양한 유튜브 URL에서 videoId 추출 (watch, youtu.be, shorts, embed 등) */
    private String extractYouTubeId(String url) {
        if (!StringUtils.hasText(url)) return null;
        Matcher m;
        m = Pattern.compile("[?&]v=([A-Za-z0-9_-]{11})").matcher(url);
        if (m.find()) return m.group(1);
        m = Pattern.compile("youtu\\.be/([A-Za-z0-9_-]{11})").matcher(url);
        if (m.find()) return m.group(1);
        m = Pattern.compile("/(shorts|embed)/([A-Za-z0-9_-]{11})").matcher(url);
        if (m.find()) return m.group(2);
        return null;
    }

    // ⭐ 라이트 유튜브/비디오/이미지 메타
    private record Media(String kind, String src, String poster) {}

    private Media buildMedia(RecipeSearchDoc d) {
        String thumb = d.getThumbUrl();
        String video = d.getVideoUrl();

        // 동영상 우선
        if (StringUtils.hasText(video)) {
            // YouTube
            String vid = extractYouTubeId(video);
            if (vid != null) {
                String embed = "https://www.youtube.com/embed/" + vid
                        + "?playsinline=1&modestbranding=1&rel=0";
                String poster = "https://i.ytimg.com/vi/" + vid + "/hqdefault.jpg";
                return new Media("youtube", embed, poster);
            }
            // 파일형
            String v = video.toLowerCase();
            if (v.endsWith(".mp4") || v.endsWith(".webm") || v.endsWith(".mov") || v.endsWith(".m4v")) {
                String poster = (StringUtils.hasText(thumb) && !looksLikeYouTubeUrl(thumb)) ? thumb : null;
                return new Media("video", video, poster);
            }
        }

        // 이미지
        if (StringUtils.hasText(thumb) && !looksLikeYouTubeUrl(thumb)) {
            return new Media("image", thumb, null);
        }

        // 유튜브 섬네일 폴백
        String vid = extractYouTubeId(video);
        if (vid != null) {
            String poster = "https://i.ytimg.com/vi/" + vid + "/hqdefault.jpg";
            return new Media("image", poster, null);
        }
        return new Media("image", "", null);
    }
}
