package com.RecipeCode.teamproject.reci.function.follow.service;

import com.RecipeCode.teamproject.reci.auth.entity.Member;
import com.RecipeCode.teamproject.reci.auth.repository.MemberRepository;
import com.RecipeCode.teamproject.reci.function.follow.dto.FollowDto;
import com.RecipeCode.teamproject.reci.function.follow.entity.Follow;
import com.RecipeCode.teamproject.reci.function.follow.repository.FollowRepository;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
@Log4j2
@SpringBootTest
class FollowServiceTest {
    @Autowired
    private FollowService followService;
    @Autowired
    private FollowRepository followRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Test
    void follow() {
        //given
        Member follower = new Member();
        follower.setUserEmail("kikiroro1506@gmail.com");
        follower.setUserId("@kikiroro");
        follower.setNickname("kikiroro");
        follower.setProfileStatus("PUBLIC");
        memberRepository.save(follower);

        Member following = new Member();
        following.setUserEmail("zxcv@gmail.com");
        following.setUserId("zxcv");
        following.setNickname("손주부");
        following.setProfileStatus("PUBLIC");
        memberRepository.save(following);

        // when
        followService.follow(follower.getUserEmail(), following.getUserEmail());

        // then
        boolean exists = followRepository.existsByFollowerAndFollowing(follower, following);
        log.info("팔로우 저장 여부 = {}", exists);
    }
    @Test
    void 자기자신_팔로우_예외() {
        // given
        Member member = new Member();
        member.setUserEmail("zxcv@gmail.com");
        member.setUserId("zxcv");
        member.setNickname("손주부");
        member.setProfileStatus("PUBLIC");
        memberRepository.save(member);

        // when
        try {
            followService.follow(member.getUserEmail(), member.getUserEmail());
        } catch (Exception error) {
            // then
            log.info("예외 발생 확인  : {}", error.getMessage());
        }
    }
    @Test
    void 중복팔로우_무시() {
        // given
        Member follower = new Member();
        follower.setUserEmail("jitzel@anyany.com");
        follower.setUserId("@jitzel");
        follower.setNickname("jitzel");
        follower.setProfileStatus("PUBLIC");
        memberRepository.save(follower);

        Member following = new Member();
        following.setUserEmail("kikiroro1506@gmail.com");
        following.setUserId("@kikiroro");
        following.setNickname("kikiroro");
        following.setProfileStatus("PUBLIC");
        memberRepository.save(following);

        // when
        followService.follow(follower.getUserEmail(), following.getUserEmail());
        followService.follow(follower.getUserEmail(), following.getUserEmail()); // 중복 호출

        // then
        Slice<Follow> slice = followRepository.findByFollower(follower, Pageable.unpaged());
        List<Follow> follows = slice.getContent(); // 실제 데이터 꺼내오기
        log.info("팔로우 개수 확인  : {}", follows.size()); // 기대값 = 1
    }

    @Test
    void unfollow() {
        // given
        Member follower = new Member();
        follower.setUserEmail("zxcv@gmail.com");
        follower.setUserId("zxcv");
        follower.setNickname("손주부");
        follower.setProfileStatus("PUBLIC");
        memberRepository.save(follower);

        Member following = new Member();
        following.setUserEmail("sj12@naver.com");
        following.setUserId("@sssjjj");
        following.setNickname("봉추");
        following.setProfileStatus("PUBLIC");
        memberRepository.save(following);

        // 먼저 팔로우 관계 생성
        followService.follow(follower.getUserEmail(), following.getUserEmail());
        log.info("팔로우 개수 (팔로우 직후) : {}", followRepository.findByFollower(follower, Pageable.unpaged()).getContent().size());

        // when - 언팔로우 실행
        followService.unfollow(follower.getUserEmail(), following.getUserEmail());

        // then
        List<Follow> follows = followRepository.findByFollower(follower, Pageable.unpaged()).getContent();
        log.info("팔로우 개수 (언팔로우 후) : {}", follows.size()); // 기대값 = 0
    }

    @Test
    void getFollowingList() {
        // 팔로우 관계 생성
        followService.follow("zxcv@gmail.com", "sj12@naver.com");

        // when (팔로잉 목록 조회)
        Slice<FollowDto> followingList =
                followService.getUserFollowingList("zxcv@gmail.com", Pageable.unpaged());
        // then
        log.info("팔로잉 목록 개수  : {}", followingList.getContent().size());
        log.info("팔로잉 대상 닉네임  : {}", followingList.getContent().get(0).getMember().getNickname());

    }

    @Test
    void getFollowerList() {
        // given (팔로우 관계 생성)
        followService.follow("zxcv@gmail.com", "sj12@naver.com");


        // when (팔로워 목록 조회: sj12 기준)
        Slice<FollowDto> followerList =
                followService.getUserFollowerList("sj12@naver.com", Pageable.unpaged());

        // then (로그로 확인)
        log.info("팔로워 목록 개수  : {}", followerList.getContent().size());
        if (!followerList.isEmpty()) {
            log.info("팔로워 닉네임  : {}", followerList.getContent().get(0).getMember().getNickname());
        }
    }
}


