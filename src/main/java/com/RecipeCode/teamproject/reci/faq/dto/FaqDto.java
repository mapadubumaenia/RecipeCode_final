package com.RecipeCode.teamproject.reci.faq.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class FaqDto {
    private Long faqNum;  //기본키
    private String faqQuestion;
    private String faqAnswer;
    private String faqTag;
}
