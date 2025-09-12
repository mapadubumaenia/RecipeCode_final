package com.RecipeCode.teamproject.es.reco.doc;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;

@Data
@NoArgsConstructor
@Document(indexName = "user-recs")
public class UserRecsDoc {
    @Id
    private String userId;
    @Field(type = FieldType.Nested)// ES 문서 ID = userId
    private List<Item> items;          // [{recipeId, score}, ...]
    private String updatedAt;

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class Item {
        @Field(type = FieldType.Keyword)
        private String recipeId;
        private double score;
    }
}