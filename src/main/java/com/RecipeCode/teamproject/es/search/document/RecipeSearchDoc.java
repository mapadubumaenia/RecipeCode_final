package com.RecipeCode.teamproject.es.search.document;

import com.RecipeCode.teamproject.es.config.InstantPropertyConverter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.ValueConverter;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "recipe") // 현재 alias/인덱스명 유지
public class RecipeSearchDoc {

    // 식별자
    @Id
    private String id;

    // 작성자/닉네임
    @Field(type = FieldType.Keyword)
    private String authorId;

    @Field(type = FieldType.Keyword)
    private String authorNick;

    // 제목/본문
    @Field(type = FieldType.Text)
    private String title;

    @Field(type = FieldType.Text)
    private String body;

    // 태그/카테고리
    @Field(type = FieldType.Keyword)
    private List<String> tags;

    @Field(type = FieldType.Keyword)
    private String category;

    // 재료(검색 가중치용)
    @Field(type = FieldType.Text)
    private List<String> ingredients;

    // 메트릭
    @Field(type = FieldType.Long)
    private Long likes;

    @Field(type = FieldType.Long)
    private Long comments;

    @Field(type = FieldType.Long)
    private Long views;

    // 공개 여부
    @Field(type = FieldType.Keyword)
    private String visibility;

    // 작성/수정 시각 (Instant) — 필드 단위 컨버터 적용
    @Field(type = FieldType.Date)
    @ValueConverter(InstantPropertyConverter.class)
    private Instant createdAt;

    @Field(type = FieldType.Date)
    @ValueConverter(InstantPropertyConverter.class)
    private Instant updatedAt;

    // 썸네일/동영상
    @Field(type = FieldType.Keyword)
    private String thumbUrl;

    @Field(type = FieldType.Keyword)
    private String videoUrl;

    @Field(type = FieldType.Text)
    private String videoText;

    // 타입 (IMAGE/VIDEO 등)
    @Field(type = FieldType.Keyword)
    private String recipeType;
}
