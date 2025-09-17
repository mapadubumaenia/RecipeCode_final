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


    // mypage userId 조회용 생성자
    public MemberDto (
            String userEmail,
            String userId,
            String nickname,
            String profileImageUrl,
            String userLocation,
            String userIntroduce,
            String userBlog,
            String userInterestTag,
            String userYoutube,
            String userInsta,
            String userWebsite){
        this.userEmail = userEmail;
        this.userId = userId;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.userLocation = userLocation;
        this.userIntroduce = userIntroduce;
        this.userBlog = userBlog;
        this.userInterestTag = userInterestTag;
        this.userYoutube = userYoutube;
        this.userInsta = userInsta;
        this.userWebsite = userWebsite;
    }

}
