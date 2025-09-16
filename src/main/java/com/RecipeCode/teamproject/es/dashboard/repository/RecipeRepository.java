package com.RecipeCode.teamproject.es.dashboard.repository;

import com.RecipeCode.teamproject.es.dashboard.document.RecipeDoc;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.time.Instant;

public interface RecipeRepository extends ElasticsearchRepository <RecipeDoc, String> {
    Long countByCreatedAtBetween(Instant start, Instant end);
}

