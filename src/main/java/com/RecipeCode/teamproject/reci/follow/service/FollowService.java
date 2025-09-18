package com.RecipeCode.teamproject.reci.follow.service;

import com.RecipeCode.teamproject.reci.auth.dto.MemberDto;
import com.RecipeCode.teamproject.reci.auth.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FollowService {
    private final MemberRepository memberRepository;

    public List<MemberDto> searchUsers(String keyword) {
        return memberRepository.findByUserIdContainingIgnoreCase(keyword)
                .stream()
                .map(m-> new MemberDto(
                        m.getUserEmail(),
                        m.getUserId(),
                        m.getNickname(),
                        m.getProfileImageUrl(),
                        m.getUserLocation(),
                        m.getUserBlog(),
                        m.getUserInsta(),
                        m.getUserWebsite(),
                        m.getUserIntroduce(),
                        m.getUserBlog(),
                        m.getUserYoutube()
                ))
                .toList();
    }

//  Service
//  public List<MemberDto> searchUsers(String keyword) {
//    return memberRepository.findByUserIdContainingIgnoreCase(keyword)
//                           .stream()
//                           .map(memberMapper::toDto)
//                           .toList();
//}

}
