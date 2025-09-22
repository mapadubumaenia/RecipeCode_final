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
            // 🔁 필터로 넘어온 tagsCsv도 #가 있을 수 있으니 제거 후 정확일치(OR) — 필요시 AND로 변경 가능
            List<String> cleaned = tags.stream()
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(s -> s.startsWith("#") ? s.substring(1) : s)
                    .toList();

            // OR(terms) — keyword 필드 우선, 실패 대비로 tags(그대로)도 함께 should로 묶자
            filters.add(orTermsOnTagFields(cleaned));
        }
        Query boolQuery = Query.of(b -> b.bool(bb -> bb.must(main).filter(filters)));

        // 3) NativeQuery
        NativeQueryBuilder qb = NativeQuery.builder()
                .withQuery(boolQuery)
                .withPageable(PageRequest.of(0, size));

        // 4) 집계 (keyword 우선, 없으면 tags로도 동작)
        qb.withAggregation("tags", Aggregation.of(a -> a.terms(t -> t.field(TAGS_FIELD_KEYWORD).size(20))));
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
        var items = hits.getSearchHits().stream().map(h -> {
            var d = h.getContent();
            var m = new LinkedHashMap<String, Object>(12);
            m.put("id", h.getId());
            m.put("title", d.getTitle() != null ? d.getTitle() : "");
            m.put("tags", d.getTags() != null ? d.getTags() : List.of());
            m.put("authorId",  d.getAuthorId()  != null ? d.getAuthorId()  : "");
            m.put("authorNick", d.getAuthorNick() != null ? d.getAuthorNick() : "");
            m.put("likes", d.getLikes() != null ? d.getLikes() : 0L);
            m.put("createdAt", d.getCreatedAt());
            m.put("score", h.getScore());
            m.put("thumbUrl", resolveThumb(d)); // 👈 핵심
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

        // ✅ 사용자가 입력한 #태그들을 추출 → 인덱스는 해시 없이 저장되므로 해시 제거(핵심)
        List<String> hashtags = extractHashtags(qv); // ["간단","매운"] 등
        if (!hashtags.isEmpty()) {
            // 각 태그에 대해 (tags.keyword:간단 OR tags:간단)를 MUST AND
            return Query.of(b -> b.bool(bb -> {
                for (String tagCore : hashtags) {
                    final String val = tagCore; // 해시 제거된 핵심 값
                    bb.must(m -> m.bool(sb -> sb
                            .should(s1 -> s1.term(t1 -> t1.field(TAGS_FIELD_KEYWORD).value(val)))
                            .should(s2 -> s2.term(t2 -> t2.field(TAGS_FIELD).value(val)))
                    ));
                }
                return bb;
            }));
        }

        if (qv.startsWith("@") && qv.length() > 1) {
            String nick = qv.substring(1).trim();
            return Query.of(b -> b.term(t -> t.field("authorNick").value(nick)));
        }

        // 일반 검색 (기존 유지)
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

    // ✅ 여러 해시태그 추출 유틸 (해시 제거해서 반환)
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

    // ✅ 필터(tagsCsv) OR terms를 keyword/tags 양쪽에 안전하게 거는 헬퍼
    private static Query orTermsOnTagFields(List<String> plainTags) {
        // keyword 필드 terms OR 원 필드 terms를 should로 묶음
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
                        .filter(f -> f.bool(b2 -> b2.mustNot(mn -> mn.term(t -> t.field("deleted").value(true))))) // ✅ 추가
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
            var m = new LinkedHashMap<String, Object>(10);
            m.put("id", h.getId());
            m.put("title", d.getTitle() != null ? d.getTitle() : "");
            m.put("authorId", d.getAuthorId() != null ? d.getAuthorId() : "");
            m.put("authorNick", d.getAuthorNick() != null ? d.getAuthorNick() : "");
            m.put("likes", d.getLikes() != null ? d.getLikes() : 0L);
            m.put("createdAt", d.getCreatedAt());
            m.put("tags", d.getTags() != null ? d.getTags() : List.of());
            m.put("thumbUrl", resolveThumb(d)); // 👈 핵심
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
    // 썸네일 보정 유틸 (핵심)
    // ===============================

    /** ES 문서 기반으로 '항상 이미지 URL'이 되도록 보정 */
    private String resolveThumb(RecipeSearchDoc d) {
        String t = d.getThumbUrl();
        // 이미지로 보기에 안전하면 그대로 사용
        if (StringUtils.hasText(t) && !looksLikeYouTubeUrl(t)) {
            return t;
        }
        // 유튜브거나 비어있으면 videoUrl에서 ID 추출 → i.ytimg.com
        String vid = extractYouTubeId(d.getVideoUrl());
        if (vid != null) {
            return "https://i.ytimg.com/vi/" + vid + "/hqdefault.jpg";
        }
        // 마지막 폴백
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
}
