package com.RecipeCode.teamproject.reci.comments.entity;

import com.RecipeCode.teamproject.common.BaseTimeEntity;
import com.RecipeCode.teamproject.reci.auth.entity.Member;
import com.RecipeCode.teamproject.reci.recipes.entity.Recipes;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode(of = "commentsId", callSuper = false)
@Builder
@Entity
@Table(name = "COMMENTS")
@SequenceGenerator(name = "COMMENTS_KEY_JPA",
                   sequenceName = "COMMENTS_KEY",
                   allocationSize = 1)
public class Comments extends BaseTimeEntity {

//  댓글이 달린 레시피
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipes_uuid", nullable = false)
    private Recipes uuid;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
                    generator = "COMMENTS_KEY_JPA")
    private Long commentsId;                                // 댓글 PK

    //    public Users userEmail;

    @Column(length = 100, nullable = false)
    private String commentsContent;

    @Column(nullable = false)
    private Long likeCount = 0L;
    @Column(nullable = false)
    private Long reportCount = 0L;

//  대댓글용(자기참조객체)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parentId_commentsId")
    private Comments parentId;

//  댓글 작성자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_userEmail", nullable = false)
    private Member userEmail;

    @OneToMany(mappedBy = "parentId",
               cascade = CascadeType.ALL,
               orphanRemoval = true)
    private List<Comments> children = new ArrayList<>();
//    TODO: mappedBy = "parent" : parent 필드에 의해 양방향 매핑됨.
//                                하나의 필드는 여러 개의 대댓글(자식)을 가질 수 있음
//          cascade = CascadeType.ALL : 부모 댓글 저장/삭제 시, 연관된 자식 댓글도 같이 적용
//          orphanRemoval = true : 자식 엔티티가 부모랑 관계 끊어지면 자동으로 삭제
//                                 예) DB에서 parent_id = null

}
