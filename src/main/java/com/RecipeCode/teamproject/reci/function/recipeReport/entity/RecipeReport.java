package com.RecipeCode.teamproject.reci.function.recipeReport.entity;

import com.RecipeCode.teamproject.common.BaseTimeEntity;
import com.RecipeCode.teamproject.reci.admin.entity.Admin;
import com.RecipeCode.teamproject.reci.auth.entity.Member;
import com.RecipeCode.teamproject.reci.feed.recipes.entity.Recipes;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "RECIPE_REPORT")
@SequenceGenerator(
        name = "SQ_RECIPE_REPORT_JPA",
        sequenceName = "RECIPE_REPORT_KEY",
        allocationSize = 1
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(of = "reportId", callSuper = false)
public class RecipeReport extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE
            , generator = "SQ_RECIPE_REPORT_JPA")
    private Long reportId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "adminEmail")
    private Admin admin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userEmail")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uuid")
    private Recipes recipes;
    private String reason;
    private Long reportStatus; // 신고 상태 (1=처리중, 2=처리완료)
    private Long reportType;   // 신고 유형 (0=욕설, 1=스팸, 2=저작권)

}
