package com.RecipeCode.teamproject.reci.admin.entity;

import com.RecipeCode.teamproject.common.BaseTimeEntity;
import com.RecipeCode.teamproject.reci.auth.membertag.entity.MemberTag;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ADMIN")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(of = "adminEmail", callSuper = false)
public class Admin extends BaseTimeEntity {
    @Id
    private String adminEmail;   // PK
    private String adminId;
    private String nickname;
    private String password;
    @Lob
    private byte[] profileImage;
    private String profileImageUrl;
    private String adminLocation;
    private String adminIntroduce;
    private String adminWebsite;
    private String adminInsta;
    private String adminYoutube;
    private String adminBlog;
    private String profileStatus;   // PUBLIC, FOLLOW, PRIVATE
    private String role;
    private String provider;      // local, google, kakao
    private String providerId;


    // 삭제
    private String deleted;
    private LocalDateTime deletedAt;

}
