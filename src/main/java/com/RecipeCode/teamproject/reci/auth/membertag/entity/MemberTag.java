package com.RecipeCode.teamproject.reci.auth.membertag.entity;

import com.RecipeCode.teamproject.common.BaseTimeEntity;
import com.RecipeCode.teamproject.reci.auth.entity.Member;
import com.RecipeCode.teamproject.reci.feed.recipes.entity.Recipes;
import com.RecipeCode.teamproject.reci.tag.entity.Tag;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "MEMBER_TAG")
@SequenceGenerator(
        name = "MEMBER_TAG_JPA",
        sequenceName = "MEMBER_TAG_KEY",
        allocationSize = 1)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(of = "memberTagId", callSuper = false)
public class MemberTag extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE
            , generator = "MEMBER_TAG_JPA")
    private Long memberTagId;               //pk

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_EMAIL")
    private Member member;                // fk

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TAG_ID")
    private Tag tag;
}
