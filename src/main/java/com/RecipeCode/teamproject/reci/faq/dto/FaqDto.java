package com.RecipeCode.teamproject.reci.faq.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class FaqDto {
    private Long faq_num;  //기본키
    private String faq_question;
    private String faq_answer;
    private String faq_tag;
}
