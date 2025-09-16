package com.RecipeCode.teamproject.es.dashboard.repository;

import com.RecipeCode.teamproject.es.dashboard.document.ReportDoc;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.time.Instant;

public interface ReportRepository extends ElasticsearchRepository<ReportDoc, String> {
    Long countByStatusAndCreatedAtBefore(String status, Instant end);
}
