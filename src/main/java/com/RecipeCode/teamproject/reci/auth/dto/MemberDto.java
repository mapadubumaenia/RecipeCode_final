package com.RecipeCode.teamproject.reci.auth.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MemberDto {
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
    private String profileImageUrl;
    private byte[] profileImage;
}
