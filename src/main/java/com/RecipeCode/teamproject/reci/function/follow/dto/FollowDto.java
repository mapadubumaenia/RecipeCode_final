package com.RecipeCode.teamproject.reci.function.follow.dto;

import com.RecipeCode.teamproject.reci.auth.dto.MemberDto;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class FollowDto {
    private Long followId;            // 팔로우 PK
    private LocalDateTime followedAt; // 팔로우한 시각
    private MemberDto member;         // 상대방 유저 정보

    // 팔로우용 dto 추가
    private boolean followingStatus;        // 로그인한 내가 이 사용자를 팔로우 중?
    private boolean followerStatus;         // 이 사용자가 나를 팔로우 중?



}
