package com.RecipeCode.teamproject.reci.auth.dto;

import com.RecipeCode.teamproject.reci.auth.membertag.entity.MemberTag;
import com.RecipeCode.teamproject.reci.tag.dto.TagDto;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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
    private String profileStatus;
    private String role;
    private String provider;
    private String providerId;
    private List<TagDto> interestTags;

    private String profileImageUrl;
    private MultipartFile profileImage;

    // MemberDto
    private Boolean noti_FOLLOW;
    private Boolean noti_COMMENT;


    // mypage userId 조회용 생성자
    public MemberDto (
            String userEmail,
            String userId,
            String nickname,
            String profileImageUrl,
            String userLocation,
            String userIntroduce,
            String userBlog,
            List<TagDto> memberTags,
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
        this.userYoutube = userYoutube;
        this.userInsta = userInsta;
        this.userWebsite = userWebsite;
        this.interestTags = memberTags;
    }

}
