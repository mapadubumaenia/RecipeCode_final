package com.RecipeCode.teamproject.reci.function.follow.entity;

import com.RecipeCode.teamproject.common.BaseTimeEntity;
import com.RecipeCode.teamproject.reci.auth.entity.Member;
import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "FOLLOW",
        uniqueConstraints = @UniqueConstraint(columnNames = {"FOLLOWER_ID", "FOLLOWING_ID"}))
@SequenceGenerator(
        name = "SQ_FOLLOW_JPA",
        sequenceName = "FOLLOW_KEY",
        allocationSize = 1
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(of = "FOLLOW_ID", callSuper = false)
public class Follow extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE
            , generator = "SQ_FOLLOW_JPA")
    private Long followId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FOLLOWER_ID", nullable = false)
    private Member follower;   // 팔로우 하는 사람
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FOLLOWING_ID", nullable = false)
    private Member following;  // 팔로우 당하는 사람
}