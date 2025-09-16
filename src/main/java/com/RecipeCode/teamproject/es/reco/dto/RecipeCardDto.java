package com.RecipeCode.teamproject.es.reco.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class RecipeCardDto {

    private String id;
    private String title;
    private String author;
    private long likes;
    private String createdAt;
    private List<String> tags;
    private double recScore;

}
