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

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class FeedService {

    private final ElasticsearchOperations es;
    private final SearchService searchService;

    public FeedPageDto hot(String after, int size) {
        size = Math.min(Math.max(size, 1), 50);
        Map<String, Object> hot = searchService.searchAndLog(null, List.of(), "hot", after, size);
        return mapHotToDto(hot);
    }

    public FeedPageDto personalFeed(String userEmail, String after, int size) {
        size = Math.min(Math.max(size, 1), 50);

        if (userEmail == null || userEmail.isBlank()) {
            Map<String, Object> hot = searchService.searchAndLog(null, List.of(), "hot", after, size);
            return mapHotToDto(hot);
        }

        String key = userEmail.trim().toLowerCase();
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

        List<RecipeCardDto> items = new ArrayList<>();
        for (UserRecsDoc.Item it : page) {
            RecipeSearchDoc d = byId.get(it.getRecipeId());
            if (d == null) continue;
            items.add(new RecipeCardDto(
                    d.getId(),
                    d.getTitle(),
                    d.getAuthorNick() == null ? "" : d.getAuthorNick(),
                    d.getLikes(),
                    d.getCreatedAt() == null ? "" : d.getCreatedAt().toString(),
                    d.getTags() == null ? List.of() : d.getTags(),
                    it.getScore()
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
            String authorNick = Objects.toString(m.get("authorNick"), "");
            long likes;
            Object lk = m.get("likes");
            if (lk instanceof Number) likes = ((Number) lk).longValue();
            else likes = parseLongOrZero(Objects.toString(lk, "0"));
            String createdAt = "";
            Object ts = m.get("createdAt");
            if (ts != null) createdAt = ts.toString();
            List<String> tags = new ArrayList<>();
            Object tg = m.get("tags");
            if (tg instanceof List<?>) {
                for (Object o : (List<?>) tg) tags.add(Objects.toString(o, ""));
            }
            items.add(new RecipeCardDto(id, title, authorNick, likes, createdAt, tags, 0.0));
        }

        int total = (hot.get("total") instanceof Number)
                ? ((Number) hot.get("total")).intValue()
                : items.size();

        String next = (hot.get("next") == null) ? null : Objects.toString(hot.get("next"), null);

        return new FeedPageDto(total, items, next);
    }

    private static long parseLongOrZero(String s) {
        try { return Long.parseLong(s); } catch (Exception e) { return 0L; }
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