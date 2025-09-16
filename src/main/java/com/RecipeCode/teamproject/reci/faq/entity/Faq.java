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
@EqualsAndHashCode(of = "faqNum", callSuper = false)
public class Faq extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE
            , generator = "SQ_FAQ_JPA"
    )
    private Long faqNum; // 기본키
    private String faqQuestion;
    private String faqAnswer;
    private String faqTag;
}
