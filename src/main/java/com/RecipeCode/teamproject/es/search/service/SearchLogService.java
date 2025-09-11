package com.RecipeCode.teamproject.es.search.service;

import com.RecipeCode.teamproject.es.search.document.SearchLogDoc;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.Instant;
import java.util.List;

@Service
public class SearchLogService {
    private final ElasticsearchOperations es;
    public SearchLogService(ElasticsearchOperations es) { this.es = es; }

    @PostConstruct
    void ensureIndex() {
        var ops = es.indexOps(SearchLogDoc.class);
        if (!ops.exists()) {
            ops.create();
            ops.putMapping(ops.createMapping());
            ops.refresh();
        }
    }

    public void log(String q, List<String> filters, String sort, int page, int size, long total, Integer tookMs) {
        SearchLogDoc doc = new SearchLogDoc();
        doc.setAt(Instant.now());
        doc.setUserId("ANON"); // 로그인 연동 시 실제 사용자 ID로
        doc.setQ(q == null ? "" : q);
        doc.setFilters(filters == null ? List.of() : filters);
        doc.setSort(sort);
        doc.setPage(page);
        doc.setSize(size);
        doc.setTotal((int) total);
        doc.setZeroHit(total == 0);
        es.save(doc);
    }
}
