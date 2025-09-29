package com.RecipeCode.teamproject.es.reco.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@lombok.Builder
public class RecipeCardDto {
    private String id;
    private String title;
    private String authorNick;
    private long likes;
    private String createdAt;
    private List<String> tags;
    private double recScore;
    private String thumbUrl;

    private String authorEmail;   // ★ 이메일

    // 라이트 미디어
    private String mediaKind;
    private String mediaSrc;
    private String poster;
}
