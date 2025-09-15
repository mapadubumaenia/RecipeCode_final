package com.RecipeCode.teamproject.reci.function.recipeReport.entity;

import com.RecipeCode.teamproject.common.BaseTimeEntity;
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
@EqualsAndHashCode(of = "", callSuper = false)
public class RecipeReport extends BaseTimeEntity {
//    REPORT_ID	NUMBER
//    ADMIN_EMAIL	VARCHAR2(100 BYTE)
//    USER_EMAIL	VARCHAR2(100 BYTE)
//    UUID VARCHAR2(36BYTE)
//    REASON	VARCHAR2(500 BYTE)
//    REPORT_STATUS	NUMBER
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE
            , generator = "SQ_RECIPE_REPORT_JPA")
    private Long reportId;

}
