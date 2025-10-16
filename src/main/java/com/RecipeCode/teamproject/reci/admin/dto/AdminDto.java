package com.RecipeCode.teamproject.reci.admin.dto;

import com.RecipeCode.teamproject.reci.tag.dto.TagDto;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

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
    private String adminLocation;
    private String adminIntroduce;
    private String adminWebsite;
    private String adminInsta;
    private String adminYoutube;
    private String adminBlog;
    private String role;
    private String profileStatus;
    private String provider;
    private String providerId;
    private List<TagDto> interestTags;

    private byte[] profileImage;
    private String profileImageUrl;

    // MemberDto
    private Boolean noti_FOLLOW;
    private Boolean noti_COMMENT;

    private String deleted;
    private LocalDateTime deletedAt;
}