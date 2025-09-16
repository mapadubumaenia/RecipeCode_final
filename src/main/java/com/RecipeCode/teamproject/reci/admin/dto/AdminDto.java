package com.RecipeCode.teamproject.reci.admin.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AdminDto {
    private String adminEmail;      // PK
    private String adminId;
    private String nickname;
    private String password;
    private byte[] profileImage;
    private String profileImageUrl;
    private String adminLocation;
    private String adminIntroduce;
    private String adminWebsite;
    private String adminInsta;
    private String adminYoutube;
    private String adminBlog;
    private String role;

    private String adminInterestTag;
    private String profileStatus;   // PUBLIC, FOLLOW, PRIVATE
}