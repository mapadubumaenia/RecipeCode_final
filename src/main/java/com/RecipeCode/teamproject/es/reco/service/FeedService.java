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

    /** 비로그인: HOT 피드 */
    public FeedPageDto hot(String after, int size) {
        size = Math.min(Math.max(size, 1), 50);
        Map<String, Object> hot = searchService.searchAndLog(null, List.of(), "hot", after, size);
        return mapHotToDto(hot);
    }

    /** 로그인: 개인화 피드만 (HOT 보강 없음, 좋아요한 글도 노출) */
    public FeedPageDto personalFeed(String userEmail, String after, int size) {
        size = Math.min(Math.max(size, 1), 50);

        // 비로그인: HOT만
        if (userEmail == null || userEmail.isBlank()) {
            Map<String, Object> hot = searchService.searchAndLog(null, List.of(), "hot", after, size);
            return mapHotToDto(hot);
        }

        final String key = userEmail.trim().toLowerCase();

        // 커서(오프셋) 디코딩 — 과거 형식(idx:, 숫자) 호환
        int off = decodeLegacy(after);

        // 개인화 문서 조회 (없거나 비어도 에러없이 빈 피드 반환)
        UserRecsDoc rec = null;
        try {
            rec = es.get(key, UserRecsDoc.class);
        } catch (Exception ignore) {}

        if (rec == null || rec.getItems() == null || rec.getItems().isEmpty()) {
            return new FeedPageDto(0, List.of(), null);
        }

        List<UserRecsDoc.Item> all = rec.getItems();
        final int totalPersonal = all.size();
        if (off >= totalPersonal) {
            return new FeedPageDto(totalPersonal, List.of(), null);
        }

        // ===== 핵심 수정: 정확한 커서 전진 =====
        // idx: 현재 "검토 시작 위치" 포인터. 아이템 하나를 '검토'할 때마다 idx++.
        int idx = Math.max(0, off);

        List<RecipeCardDto> out = new ArrayList<>(size);
        Set<String> seenIds = new LinkedHashSet<>();

        // ES 배치 조회를 위해 청크 단위로 가져오되,
        // 실제 커서는 아이템 단위로만 전진시킨다.
        while (out.size() < size && idx < totalPersonal) {
            int chunkEnd = Math.min(idx + Math.max(size * 2, 20), totalPersonal);
            List<UserRecsDoc.Item> chunk = all.subList(idx, chunkEnd);

            // 이번 청크의 id들 조회
            List<String> ids = chunk.stream().map(UserRecsDoc.Item::getRecipeId).toList();
            if (ids.isEmpty()) break;

            Query q = Query.of(b -> b.bool(bb -> bb
                    .must(m -> m.ids(i -> i.values(ids)))
                    .filter(f -> f.term(t -> t.field("visibility").value("PUBLIC")))
                    .filter(f -> f.bool(b2 -> b2.mustNot(mn -> mn.term(t -> t.field("deleted").value(true)))))
            ));
            NativeQuery nq = NativeQuery.builder()
                    .withQuery(q)
                    .withPageable(PageRequest.of(0, ids.size()))
                    .build();

            SearchHits<RecipeSearchDoc> hits;
            try {
                hits = es.search(nq, RecipeSearchDoc.class);
            } catch (Exception ex) {
                // ES 일시 오류 → 이번 청크는 모두 '검토'한 것으로 간주하고 건너뜀
                idx = chunkEnd;
                continue;
            }

            Map<String, RecipeSearchDoc> byId = hits.getSearchHits().stream()
                    .map(SearchHit::getContent)
                    .collect(Collectors.toMap(RecipeSearchDoc::getId, r -> r, (a, b) -> a));

            // 청크 내부를 아이템 단위로 처리하며 idx를 '정확히' 전진
            for (int j = 0; j < chunk.size() && out.size() < size; j++) {
                UserRecsDoc.Item it = chunk.get(j);
                RecipeSearchDoc d = byId.get(it.getRecipeId());

                // 이 아이템은 '검토 완료' → 커서 한 칸 전진
                idx++;

                if (d == null) continue; // 비공개/삭제/미존재
                if (key.equalsIgnoreCase(nvl(d.getAuthorEmail()))) continue; // 내 글 제외
                if (!seenIds.add(d.getId())) continue; // 중복 방지

                Media media = buildMedia(d);
                out.add(new RecipeCardDto(
                        d.getId(),
                        nvl(d.getTitle()),
                        nvl(d.getAuthorId()),
                        nvlLong(d.getLikes()),
                        (d.getCreatedAt() == null) ? "" : d.getCreatedAt().toString(),
                        (d.getTags() == null) ? List.of() : d.getTags(),
                        it.getScore(),
                        nvl(d.getThumbUrl()),
                        nvl(d.getAuthorEmail()),
                        media.kind, media.src, media.poster
                ));
            }

            // 만약 out이 꽉 차지 않았고, 위 for 루프가 청크 끝까지 돌았다면
            // 남은 청크 아이템들은 모두 '검토'한 상태이므로 idx는 이미 그만큼 전진되어 있음.
            // (for 안에서 idx++를 했기 때문에 별도 처리 불필요)
            // 다음 while 루프로 넘어가면 idx부터 다음 청크를 만든다.
        }

        String next = (idx < totalPersonal) ? encodeIdx(idx) : null;

        return new FeedPageDto(totalPersonal, out, next);
    }

    // ======================
    // HOT 매핑
    // ======================
    @SuppressWarnings("unchecked")
    private FeedPageDto mapHotToDto(Map<String, Object> hot) {
        List<Map<String, Object>> list =
                (List<Map<String, Object>>) hot.getOrDefault("items", List.of());

        List<RecipeCardDto> items = new ArrayList<>();
        for (Map<String, Object> m : list) {
            String id = Objects.toString(m.get("id"), "");
            String title = Objects.toString(m.get("title"), "");

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

            String authorEmail = Objects.toString(m.getOrDefault("authorEmail",""), "");
            String thumbUrl = Objects.toString(m.getOrDefault("thumbUrl", ""), "");
            String mediaKind = Objects.toString(m.getOrDefault("mediaKind", "image"), "image");
            String mediaSrc  = Objects.toString(m.getOrDefault("mediaSrc", thumbUrl), thumbUrl);
            String poster    = Objects.toString(m.getOrDefault("poster", thumbUrl), thumbUrl);

            items.add(new RecipeCardDto(
                    id, title, authorForDisplay, likes, createdAt, tags, 0.0, thumbUrl,
                    authorEmail,
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
        final String kind;
        final String src;
        final String poster;
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
    // 커서 & 기타 유틸
    // ===============================
    /** 과거 커서(숫자/idx:n/base64 idx:) 지원 */
    private static int decodeLegacy(String after) {
        if (after == null || after.isBlank()) return 0;
        try {
            byte[] raw = Base64.getUrlDecoder().decode(after);
            String s = new String(raw, StandardCharsets.UTF_8);
            if (s.startsWith("idx:")) return Integer.parseInt(s.substring(4));
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            try {
                return Integer.parseInt(after.trim());
            } catch (Exception ignore) { return 0; }
        }
    }

    private static String encodeIdx(int off) {
        String s = "idx:" + off;
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(s.getBytes(StandardCharsets.UTF_8));
    }

    private static String nvl(String s){ return (s == null) ? "" : s; }
    private static long nvlLong(Long v){ return (v == null) ? 0L : v; }

    private static long coerceLong(Object o) {
        if (o instanceof Number) return ((Number) o).longValue();
        try { return Long.parseLong(String.valueOf(o)); } catch (Exception e) { return 0L; }
    }
}
