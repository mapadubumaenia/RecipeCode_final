package com.RecipeCode.teamproject.es.reco.dto;


import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class FeedPageDto {

    private int total;
    private List<RecipeCardDto> items;
    private String next;            // 커서
}
