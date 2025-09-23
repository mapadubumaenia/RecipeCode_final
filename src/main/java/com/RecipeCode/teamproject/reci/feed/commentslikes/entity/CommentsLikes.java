package com.RecipeCode.teamproject.reci.feed.commentslikes.entity;

import com.RecipeCode.teamproject.common.BaseTimeEntity;
import com.RecipeCode.teamproject.reci.auth.entity.Member;
import com.RecipeCode.teamproject.reci.feed.comments.entity.Comments;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "LIKE_ID", callSuper = false)
@Entity
@Table (name = "COMMENTS_LIKES",
        uniqueConstraints = @UniqueConstraint(columnNames = {
                "USER_EMAIL","COMMENTS_ID"}) )
@SequenceGenerator(name = "LIKES_KEY_JPA",
        sequenceName = "LIKES_KEY",
        allocationSize = 1)
public class CommentsLikes extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
            generator = "LIKES_KEY_JPA")
    private Long likeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_EMAIL", nullable = false)
    private Member member;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "COMMENTS_ID", nullable = false)
    private Comments comments;
}
