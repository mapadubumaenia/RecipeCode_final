package com.RecipeCode.teamproject.reci.faq.entity;


import com.RecipeCode.teamproject.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "FAQ")
@SequenceGenerator(
        name = "SQ_FAQ_JPA",
        sequenceName = "FAQ_KEY",
        allocationSize = 1)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(of = "faq_num", callSuper = false)
public class Faq extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE
            , generator = "SQ_FAQ_JPA"
    )
    private Long faq_num; // 기본키
    private String faq_question;
    private String faq_answer;
    private String faq_tag;
}
