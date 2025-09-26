package com.RecipeCode.teamproject.es.reco.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecipeCardDto {
    private String id;
    private String title;
    private String authorNick;
    private long likes;
    private String createdAt;
    private List<String> tags;
    private double recScore;   // 개인화 점수(없으면 0)

    private String thumbUrl;   // 레거시/폴백용 이미지

    private String authorEmail;

    // 👇 신규: 라이트 유튜브/비디오/이미지 메타
    private String mediaKind;  // "youtube" | "video" | "image"
    private String mediaSrc;   // youtube: embed URL, video: 파일 URL, image: 이미지 URL
    private String poster;     // 썸네일/포스터(없으면 null)
}
