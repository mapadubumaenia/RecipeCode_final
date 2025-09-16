package com.RecipeCode.teamproject.reci.admin.entity;

import com.RecipeCode.teamproject.common.BaseTimeEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.*;

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
    private String adminInterestTag;
    private String profileStatus;   // PUBLIC, FOLLOW, PRIVATE
    private String role;
}
