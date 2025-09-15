package com.RecipeCode.teamproject.reci.auth.entity;

import com.RecipeCode.teamproject.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

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

    @Lob
    private byte[] profileImage;
    private String profileImageUrl;

}
