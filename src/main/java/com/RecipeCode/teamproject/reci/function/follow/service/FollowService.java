package com.RecipeCode.teamproject.reci.function.follow.service;

import com.RecipeCode.teamproject.common.ErrorMsg;
import com.RecipeCode.teamproject.common.MapStruct;
import com.RecipeCode.teamproject.reci.auth.dto.MemberDto;
import com.RecipeCode.teamproject.reci.auth.entity.Member;
import com.RecipeCode.teamproject.reci.auth.repository.MemberRepository;
import com.RecipeCode.teamproject.reci.auth.service.MemberService;
import com.RecipeCode.teamproject.reci.function.follow.dto.FollowDto;
import com.RecipeCode.teamproject.reci.function.follow.entity.Follow;
import com.RecipeCode.teamproject.reci.function.follow.repository.FollowRepository;
import com.RecipeCode.teamproject.reci.function.notification.enums.NotificationEvent;
import com.RecipeCode.teamproject.reci.function.notification.service.NotificationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;


import java.util.List;

@Service
@RequiredArgsConstructor
public class FollowService {

    private final MemberRepository memberRepository;
    private final MemberService memberService;
    private final FollowRepository followRepository;
    private final MapStruct mapStruct;
    private final ErrorMsg errorMsg;
    private final NotificationService notificationService;

    // 공통 Member 조회 (userId → userEmail 변환)
    private Member findByUserIdOrThrow(String userId, String errorCode) {
        return memberRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException(errorMsg.getMessage(errorCode)));
    }

    // 팔로우
    @Transactional
    public void follow(String followerEmail, String followingEmail) {
        Member follower = memberRepository.findById(followerEmail)
                .orElseThrow(() -> new RuntimeException(errorMsg.getMessage("errors.follower.notfound")));
        Member following = memberRepository.findById(followingEmail)
                .orElseThrow(() -> new RuntimeException(errorMsg.getMessage("errors.following.notfound")));
//        Member following = findByUserIdOrThrow(followingUserId, "errors.following.notfound");

        if (follower.equals(following)) {
            throw new IllegalArgumentException(errorMsg.getMessage("errors.follow.self"));
        }

        if (followRepository.existsByFollowerAndFollowing(follower, following)) {
            return; // 이미 팔로우 중
        }

        Follow follow = new Follow();
        follow.setFollower(follower);
        follow.setFollowing(following);

        followRepository.save(follow);

        // 알림 생성
//        notificationService.createNotification(
//                follower.getUserEmail(),                  // 알림을 발생시킨 사람
//                following.getUserEmail(),                 // 알림을 받는 사람
//                NotificationEvent.FOLLOW,                 // enum 넘김
//                "FOLLOW",                                 // 소스 타입
//                String.valueOf(follow.getFollowId())      // 소스 ID
//        );
    }

    // 언팔로우
    @Transactional
    public void unfollow(String followerEmail, String followingEmail) {
        Member follower = memberRepository.findById(followerEmail)
                .orElseThrow(() -> new RuntimeException(errorMsg.getMessage("errors.follower.notfound")));
        Member following = memberRepository.findById(followingEmail)
                .orElseThrow(()->new RuntimeException(errorMsg.getMessage("errors.following.notfound")));

        followRepository.findByFollowerAndFollowing(follower, following)
                .ifPresent(followRepository::delete);
    }

    // 사용자 검색
    public List<MemberDto> searchUsers(String keyword) {
        return memberRepository.findByUserIdContainingIgnoreCase(keyword)
                .stream()
                .map(mapStruct::toDto)
                .toList();
    }

    // 특정 유저의 팔로잉 목록
    public Slice<FollowDto> getUserFollowingList(String userId, Pageable pageable) {
        Member user = findByUserIdOrThrow(userId, "errors.not.found");
        return followRepository.findByFollower(user, pageable)
                .map(mapStruct::toFollowingDto);
    }

    // 특정 유저의 팔로워 목록
    public Slice<FollowDto> getUserFollowerList(String userId, Pageable pageable) {
        Member user = findByUserIdOrThrow(userId, "errors.not.found");
        return followRepository.findByFollowing(user, pageable)
                .map(mapStruct::toFollowerDto);
    }

    // 특정 유저의 팔로워 수
    public long getUserFollowerCount(String userId) {
        Member user = findByUserIdOrThrow(userId, "errors.not.found");
        return followRepository.countByFollowing(user);
    }

    // 특정 유저의 팔로잉 수
    public long getUserFollowingCount(String userId) {
        Member user = findByUserIdOrThrow(userId, "errors.not.found");
        return followRepository.countByFollower(user);
    }

    // owner(Network Profile)가 팔로우 중인 사람 목록에 대한 viewer(로그인 사용자)의 팔로우 여부
    public Slice<FollowDto> getUserFollowingListWithStatus(String ownerEmail,
                                                           String viewerEmail, Pageable pageable) {

        // 로그인 사용자(viewer)
        Member owner = memberService.getByUserEmail(ownerEmail);
        Member viewer = memberService.getByUserEmail(viewerEmail);

        // owner가 팔로우하는 관계들
        Slice<Follow> slice = followRepository.findByFollower(owner, pageable);

        // '대상 사용자'는 following
        return slice.map(f-> {
            Member target = f.getFollowing();

            boolean iFollowHim = followRepository
                    .existsByFollowerAndFollowing(viewer, target);
            boolean heFollowsMe = followRepository
                    .existsByFollowerAndFollowing(target, viewer);

            FollowDto dto = new FollowDto();
            dto.setMember(mapStruct.toDto(target));
            dto.setFollowingStatus(iFollowHim);
            dto.setFollowerStatus(heFollowsMe);
            return dto;
        });
    }

    // owner(Network Profile)를 팔로우 중인 사람 목록에 대한 viewer(로그인 사용자)의 팔로우 여부
    public Slice<FollowDto> getUserFollowerListWithStatus(String ownerEmail,
                                                          String viewerEmail, Pageable pageable) {

        Member owner = memberService.getByUserEmail(ownerEmail);
        Member viewer = memberService.getByUserEmail(viewerEmail);

        // owner를 팔로우하는 관계들
        Slice<Follow> slice = followRepository.findByFollowing(owner, pageable);

        // '대상 사용자'는 follower
        return slice.map(f-> {
            Member target = f.getFollower();

            boolean iFollowHim = followRepository
                    .existsByFollowerAndFollowing(viewer, target);
            boolean heFollowsMe = followRepository
                    .existsByFollowerAndFollowing(target, viewer);

            FollowDto dto = new FollowDto();
            dto.setMember(mapStruct.toDto(target));
            dto.setFollowingStatus(iFollowHim);
            dto.setFollowerStatus(heFollowsMe);
            return dto;
        });
    }

}
