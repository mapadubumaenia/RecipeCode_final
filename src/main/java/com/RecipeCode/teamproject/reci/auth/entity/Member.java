package com.RecipeCode.teamproject.reci.auth.entity;

import com.RecipeCode.teamproject.common.BaseTimeEntity;
import com.RecipeCode.teamproject.reci.admin.entity.Admin;
import com.RecipeCode.teamproject.reci.auth.membertag.dto.MemberTagDto;
import com.RecipeCode.teamproject.reci.auth.membertag.entity.MemberTag;
import com.RecipeCode.teamproject.reci.tag.dto.TagDto;
import com.RecipeCode.teamproject.reci.tag.entity.Tag;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
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
    private String profileStatus;
    private String role;
    private String provider;      // local, google, kakao
    private String providerId;

    @Lob
    private byte[] profileImage;
    private String profileImageUrl;

    // 회원 ↔ 관심태그
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<MemberTag> memberTags = new ArrayList<>();

    // 삭제
    private String deleted;
    private LocalDateTime deletedAt;

    public static Member fromAdmin(Admin admin) {
        Member member = new Member();
        member.setUserEmail(admin.getAdminEmail());
        member.setUserId(admin.getAdminId());
        member.setNickname(admin.getNickname());
        member.setPassword(admin.getPassword());
        member.setProfileImage(admin.getProfileImage());
        member.setProfileImageUrl(admin.getProfileImageUrl());
        member.setRole(admin.getRole());
        member.setUserBlog(admin.getAdminBlog());
        member.setUserInsta(admin.getAdminInsta());
        member.setUserIntroduce(admin.getAdminIntroduce());
        member.setUserLocation(admin.getAdminLocation());
        member.setUserWebsite(admin.getAdminWebsite());
        member.setUserYoutube(admin.getAdminYoutube());
        member.setProfileStatus(admin.getProfileStatus());
        member.setDeleted(admin.getDeleted());
        member.setDeletedAt(admin.getDeletedAt());
        member.setProvider(admin.getProvider());
        member.setProviderId(admin.getProviderId());
        return member;
    }
}
