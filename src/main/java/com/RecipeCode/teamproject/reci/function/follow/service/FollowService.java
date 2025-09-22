package com.RecipeCode.teamproject.reci.function.follow.service;

import com.RecipeCode.teamproject.common.ErrorMsg;
import com.RecipeCode.teamproject.common.MapStruct;
import com.RecipeCode.teamproject.reci.auth.dto.MemberDto;
import com.RecipeCode.teamproject.reci.auth.entity.Member;
import com.RecipeCode.teamproject.reci.auth.repository.MemberRepository;
import com.RecipeCode.teamproject.reci.function.follow.entity.Follow;
import com.RecipeCode.teamproject.reci.function.follow.repository.FollowRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FollowService {
    private final MemberRepository memberRepository;
    private final FollowRepository followRepository;
    private final MapStruct mapStruct;
    private final ErrorMsg errorMsg;

    // 팔로우
    public void follow(String followerEmail, String followingEmail) {
        Member follower = memberRepository.findById(followerEmail)
                .orElseThrow(() -> new RuntimeException("팔로워 계정을 찾을 수 없음"));
        Member following = memberRepository.findById(followingEmail)
                .orElseThrow(() -> new RuntimeException("팔로잉 계정을 찾을 수 없음"));

        if (follower.equals(following)) {
            throw new IllegalArgumentException("자기 자신은 팔로우할 수 없습니다.");
        }

        if (followRepository.existsByFollowerAndFollowing(follower, following)) {
            return; // 이미 팔로우 중
        }

        Follow follow = new Follow();
        follow.setFollower(follower);
        follow.setFollowing(following);

        followRepository.save(follow);
    }
    // 언팔로우
    @Transactional
    public void unfollow(String followerEmail, String followingEmail) {
        Member follower = memberRepository.findById(followerEmail)
                .orElseThrow(() -> new RuntimeException("팔로워 계정을 찾을 수 없음"));
        Member following = memberRepository.findById(followingEmail)
                .orElseThrow(() -> new RuntimeException("팔로잉 계정을 찾을 수 없음"));

        // 팔로우 관계 찾아오기
        followRepository.findByFollowerAndFollowing(follower, following)
                .ifPresent(followRepository::delete);
    }




    //  Service
    public List<MemberDto> searchUsers(String keyword) {
        return memberRepository.findByUserIdContainingIgnoreCase(keyword)
                .stream()
                .map(mapStruct::toDto)
                .toList();
    }

}


//    public List<MemberDto> searchUsers(String keyword) {
//        return memberRepository.findByUserIdContainingIgnoreCase(keyword)
//                .stream()
//                .map(m-> new MemberDto(
//                        m.getUserEmail(),
//                        m.getUserId(),
//                        m.getNickname(),
//                        m.getProfileImageUrl(),
//                        m.getUserLocation(),
//                        m.getUserBlog(),
//                        m.getUserInsta(),
//                        m.getUserWebsite(),
//                        m.getUserIntroduce(),
//                        m.getUserBlog(),
//                        m.getUserYoutube()
//                ))
//                .toList();
//    }