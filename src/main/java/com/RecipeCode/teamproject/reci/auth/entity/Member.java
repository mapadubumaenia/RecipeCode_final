package com.RecipeCode.teamproject.reci.auth.entity;

import com.RecipeCode.teamproject.common.BaseTimeEntity;
import com.RecipeCode.teamproject.reci.auth.membertag.entity.MemberTag;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="USERS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@EqualsAndHashCode(of = "userEmail", callSuper = false)
public class Member extends BaseTimeEntity {
    @Id
    private String userEmail;
    private String userId;
    private String nickname;
    private String password;
    private String userLocation;
    private String userIntroduce;
    private String userWebsite;
    private String userInsta;
    private String userYoutube;
    private String userBlog;
    private String userInterestTag;
    private String profileStatus;
    private String role;
    private String provider;      // local, google, kakao
    private String providerId;

    @Lob
    private byte[] profileImage;
    private String profileImageUrl;

    // 회원 ↔ 관심태그 (1:N)
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MemberTag> memberTags = new ArrayList<>();

}
