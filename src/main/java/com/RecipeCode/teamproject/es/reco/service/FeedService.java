package com.RecipeCode.teamproject.es.reco.service;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.RecipeCode.teamproject.es.reco.doc.UserRecsDoc;
import com.RecipeCode.teamproject.es.reco.dto.FeedPageDto;
import com.RecipeCode.teamproject.es.reco.dto.RecipeCardDto;
import com.RecipeCode.teamproject.es.search.document.RecipeSearchDoc;
import com.RecipeCode.teamproject.es.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class FeedService {

    private final ElasticsearchOperations es;
    private final SearchService searchService;

    /** HOT 피드 */
    public FeedPageDto hot(String after, int size) {
        size = Math.min(Math.max(size, 1), 50);
        Map<String, Object> hot = searchService.searchAndLog(null, List.of(), "hot", after, size);
        return mapHotToDto(hot);
    }

    /** 개인화 피드 */
    public FeedPageDto personalFeed(String userEmail, String after, int size) {
        size = Math.min(Math.max(size, 1), 50);

        if (userEmail == null || userEmail.isBlank()) {
            Map<String, Object> hot = searchService.searchAndLog(null, List.of(), "hot", after, size);
            return mapHotToDto(hot);
        }

        // 이메일 키 소문자 정규화
        String key = userEmail.trim().toLowerCase();

        // 개인 추천 문서 조회 (없으면 HOT 폴백)
        UserRecsDoc rec;
        try {
            rec = es.get(key, UserRecsDoc.class);
        } catch (Exception e) {
            Map<String, Object> hot = searchService.searchAndLog(null, List.of(), "hot", after, size);
            return mapHotToDto(hot);
        }

        if (rec == null || rec.getItems() == null || rec.getItems().isEmpty()) {
            Map<String, Object> hot = searchService.searchAndLog(null, List.of(), "hot", after, size);
            return mapHotToDto(hot);
        }

        int offset = decode(after);
        List<UserRecsDoc.Item> all = rec.getItems();
        if (offset >= all.size()) return new FeedPageDto(all.size(), List.of(), null);

        List<UserRecsDoc.Item> page = all.subList(offset, Math.min(offset + size, all.size()));
        List<String> ids = page.stream().map(UserRecsDoc.Item::getRecipeId).toList();
        if (ids.isEmpty()) return new FeedPageDto(all.size(), List.of(), null);

        // recipe-v2에서 해당 아이템만 조회(공개/미삭제만)
        Query q = Query.of(b -> b.bool(bb -> bb
                .must(m -> m.ids(i -> i.values(ids)))
                .filter(f -> f.term(t -> t.field("visibility").value("PUBLIC")))
                .filter(f -> f.bool(b2 -> b2.mustNot(mn -> mn.term(t -> t.field("deleted").value(true)))))
        ));

        NativeQuery nq = NativeQuery.builder()
                .withQuery(q)
                .withPageable(PageRequest.of(0, ids.size()))
                .build();

        SearchHits<RecipeSearchDoc> hits = es.search(nq, RecipeSearchDoc.class);
        Map<String, RecipeSearchDoc> byId = hits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toMap(RecipeSearchDoc::getId, r -> r, (a, b) -> a));

        // 추천 순서 유지 + 미디어 메타 포함 매핑
        List<RecipeCardDto> items = new ArrayList<>();
        for (UserRecsDoc.Item it : page) {
            RecipeSearchDoc d = byId.get(it.getRecipeId());
            if (d == null) continue; // 삭제/비공개/미존재 스킵

            Media media = buildMedia(d);

            items.add(new RecipeCardDto(
                    d.getId(),
                    nvl(d.getTitle()),
                    nvl(d.getAuthorId()),
                    nvlLong(d.getLikes()),
                    (d.getCreatedAt() == null) ? "" : d.getCreatedAt().toString(),
                    (d.getTags() == null) ? List.of() : d.getTags(),
                    it.getScore(),
                    nvl(d.getThumbUrl()),

                    // ★ 이메일은 media보다 먼저!
                    nvl(d.getAuthorEmail()),

                    // media
                    media.kind, media.src, media.poster
            ));
        }

        Integer nextIdx = (offset + items.size() < all.size()) ? (offset + items.size()) : null;
        String next = (nextIdx == null) ? null : encode(nextIdx);
        return new FeedPageDto(all.size(), items, next);
    }

    @SuppressWarnings("unchecked")
    private FeedPageDto mapHotToDto(Map<String, Object> hot) {
        List<Map<String, Object>> list =
                (List<Map<String, Object>>) hot.getOrDefault("items", List.of());

        List<RecipeCardDto> items = new ArrayList<>();
        for (Map<String, Object> m : list) {
            String id = Objects.toString(m.get("id"), "");
            String title = Objects.toString(m.get("title"), "");

            // ✅ authorId 우선, 없으면 authorNick
            String authorId  = Objects.toString(m.getOrDefault("authorId", ""), "");
            String authorNick = Objects.toString(m.getOrDefault("authorNick", ""), "");
            String authorForDisplay = !authorId.isBlank() ? authorId : authorNick;

            long likes = coerceLong(m.get("likes"));
            String createdAt = (m.get("createdAt") == null) ? "" : m.get("createdAt").toString();

            List<String> tags = new ArrayList<>();
            Object tg = m.get("tags");
            if (tg instanceof List<?>) {
                for (Object o : (List<?>) tg) tags.add(Objects.toString(o, ""));
            }

            String authorEmail = Objects.toString(m.getOrDefault("authorEmail",""), ""); // ★ 꺼내기
            String thumbUrl = Objects.toString(m.getOrDefault("thumbUrl", ""), "");
            String mediaKind = Objects.toString(m.getOrDefault("mediaKind", "image"), "image");
            String mediaSrc  = Objects.toString(m.getOrDefault("mediaSrc", thumbUrl), thumbUrl);
            String poster    = Objects.toString(m.getOrDefault("poster", thumbUrl), thumbUrl);

            // ✅ 표기 필드에 authorId(또는 대체) 주입
            items.add(new RecipeCardDto(
                    id, title, authorForDisplay, likes, createdAt, tags, 0.0, thumbUrl,

                    // ★ 이메일 먼저
                    authorEmail,

                    // media
                    mediaKind, mediaSrc, poster
            ));
        }
        int total = (hot.get("total") instanceof Number) ? ((Number) hot.get("total")).intValue() : items.size();
        String next = (hot.get("next") == null) ? null : Objects.toString(hot.get("next"), null);
        return new FeedPageDto(total, items, next);
    }

    // ===============================
    // 미디어 유틸
    // ===============================

    private static final Pattern YT_V = Pattern.compile("[?&]v=([A-Za-z0-9_-]{11})");
    private static final Pattern YT_SHORTS_EMBED = Pattern.compile("/(shorts|embed)/([A-Za-z0-9_-]{11})");
    private static final Pattern YT_BE = Pattern.compile("youtu\\.be/([A-Za-z0-9_-]{11})");

    private static class Media {
        final String kind;   // youtube | video | image
        final String src;    // youtube: embed URL, video: 파일 URL, image: 이미지 URL
        final String poster; // 이미지/포스터
        Media(String k, String s, String p){ this.kind=k; this.src=s; this.poster=p; }
    }

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
        Matcher m = YT_V.matcher(url);
        if (m.find()) return m.group(1);
        m = YT_BE.matcher(url);
        if (m.find()) return m.group(1);
        m = YT_SHORTS_EMBED.matcher(url);
        if (m.find()) return m.group(2);
        return null;
    }

    private boolean looksLikeYouTubeUrl(String url) {
        if (!StringUtils.hasText(url)) return false;
        String u = url.toLowerCase();
        return u.contains("youtube.com") || u.contains("youtu.be");
    }

    // ===============================
    // 기타 유틸
    // ===============================

    private static String nvl(String s){ return (s == null) ? "" : s; }
    private static long nvlLong(Long v){ return (v == null) ? 0L : v; }

    private static long coerceLong(Object o) {
        if (o instanceof Number) return ((Number) o).longValue();
        try { return Long.parseLong(String.valueOf(o)); } catch (Exception e) { return 0L; }
    }

    private static int decode(String after) {
        if (after == null || after.isBlank()) return 0;
        try {
            byte[] raw = Base64.getUrlDecoder().decode(after);
            String s = new String(raw, StandardCharsets.UTF_8);
            if (s.startsWith("idx:")) return Integer.parseInt(s.substring(4));
            return Integer.parseInt(s.trim());
        } catch (Exception e) { return 0; }
    }

    private static String encode(int off) {
        String s = "idx:" + off;
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(s.getBytes(StandardCharsets.UTF_8));
    }
}
