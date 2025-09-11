package com.RecipeCode.teamproject.es.search.document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.Instant;
import java.util.List;


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Document(indexName = "recipe")
    public class RecipeSearchDoc {
        @Id private String id;
        @Field(type = FieldType.Text)    private String title;
        @Field(type = FieldType.Text)    private String body;
        @Field(type = FieldType.Keyword) private List<String> tags;
        @Field(type = FieldType.Keyword) private String authorId;
        @Field(type = FieldType.Keyword) private String authorNick;
        @Field(type = FieldType.Integer) private Integer likes;
        @Field(type = FieldType.Date)    private Instant createdAt;
        @Field(type = FieldType.Keyword) private String visibility;
    }

