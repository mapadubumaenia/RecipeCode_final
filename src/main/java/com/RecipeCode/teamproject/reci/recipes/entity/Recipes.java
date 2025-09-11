package com.RecipeCode.teamproject.reci.recipes.entity;

import com.RecipeCode.teamproject.common.BaseTimeEntity;
import com.RecipeCode.teamproject.reci.recipecontent.entity.RecipeContent;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.processing.Pattern;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode(of = "uuid", callSuper = false)
@Builder
@Entity
@Table(name = "RECIPES")
public class Recipes extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36)
    private String uuid;                        // 기본키

//  @ManyToOne(optional=false) private Member author;
//  @Column(nullable = false)
//  private User userEmail;

    @Column(length = 100, nullable = false)
    private String recipeTitle;
    @Column(length = 500)
    private String recipeIntro;
    @Column(nullable = false)
    private String recipeCategory;
    private Long cookingTime;

    @Lob
    private byte[] thumbnail;
    private String thumbnailUrl;

    @Column(nullable = false)
    private String postStatus;                  // 공개여부
    @Column(nullable = false)
    private Long viewCount = 0L;                // 조회수
    @Column(nullable = false)
    private Long likeCount = 0L;                // 좋아요
    @Column(nullable = false)
    private Long reportCount = 0L;              // 신고수
    @Column(nullable = false)
    private Long commentCount = 0L;             // 댓글수

    @Column(length = 10)
    private String difficulty;                  // 난이도

//  @ElementCollection : JPA가 자동으로 별도 테이블을 만듬
//  @CollectionTable : recipes_uuid와 조인 + 리스트 원소 저장할 컬럼들 + sort_order 컬럼 자동 생성
    @ElementCollection
    @CollectionTable(name = "INQUIRY", joinColumns = @JoinColumn(name = "recipes_uuid"))
    @OrderColumn(name = "sort_order")
    private List<Inquiry> inquiry = new ArrayList<>();

}
