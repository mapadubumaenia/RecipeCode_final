package com.RecipeCode.teamproject.es.dashboard.document;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.Instant;
import java.time.LocalDateTime;

@Data
@Document(indexName = "recipes")
public class RecipeDoc {
    @Id
    private String id;
    private String title;
    private String userId;
    @Field(type = FieldType.Date) private Instant createdAt;
    private Boolean isDeleted;
}
