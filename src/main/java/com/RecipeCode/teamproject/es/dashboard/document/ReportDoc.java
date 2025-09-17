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
@Document(indexName = "reports")
public class ReportDoc {
    @Id
    private String id;
    private String targetType;
    private String targetId;
    private String userId;
    private String reason;
    private String status;
    @Field(type = FieldType.Date) private Instant createdAt;
}
