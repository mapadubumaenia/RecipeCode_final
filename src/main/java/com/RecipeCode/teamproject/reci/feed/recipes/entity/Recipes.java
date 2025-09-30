package com.RecipeCode.teamproject.reci.feed.recipes.entity;

import com.RecipeCode.teamproject.common.BaseTimeEntity;
import com.RecipeCode.teamproject.common.BooleanToYNConverter;
import com.RecipeCode.teamproject.reci.auth.entity.Member;
import com.RecipeCode.teamproject.reci.feed.recipeTag.entity.RecipeTag;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode(of = "UUID", callSuper = false)
@Entity
@Table(name = "RECIPES")

@SQLDelete(sql="UPDATE RECIPES SET DELETED='Y',  DELETE_DATE = SYSDATE WHERE UUID=?")
@Where(clause = "DELETED='N'")
public class Recipes extends BaseTimeEntity {

    @Id
    private String uuid;                        // 기본키

    private String recipeTitle;
    private String recipeIntro;
    private String recipeCategory;
    private Long cookingTime;

    @Lob
    private byte[] thumbnail;
    private String thumbnailUrl;
    private String recipeType;
    private String videoUrl;
    private String videoText;

    private String postStatus;            // 공개여부
    private Long viewCount;               // 조회수
    private Long likeCount;               // 좋아요
    private Long reportCount;             // 신고수
    private Long commentCount;            // 댓글수

    private String difficulty;                  // 난이도


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_EMAIL", nullable = false)
    private Member member;

    //    TODO: Tag 테이블 추가
    @OneToMany(mappedBy = "recipes", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<RecipeTag> recipeTag = new ArrayList<>();

    /* TODO : 소프트 삭제 추가! */
    //    기본값(null) -> DB에 "N"
    @Convert(converter = BooleanToYNConverter.class)
    private boolean deleted;

    private LocalDateTime deleteDate;

    public void incrementCommentCount() {
        if (this.commentCount == null) this.commentCount = 0L;
        this.commentCount++;
    }


    /*
     *   단방향 매핑
     *   재료는 단방향: Ingredient → Recipes (ManyToOne)
     *   조리단계도 단방향: RecipeContent → Recipes (ManyToOne)
     *
     * */

}