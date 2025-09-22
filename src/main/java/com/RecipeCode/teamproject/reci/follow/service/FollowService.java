package com.RecipeCode.teamproject.reci.follow.service;

import com.RecipeCode.teamproject.common.MapStruct;
import com.RecipeCode.teamproject.reci.auth.dto.MemberDto;
import com.RecipeCode.teamproject.reci.auth.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FollowService {
    private final MemberRepository memberRepository;
    private final MapStruct mapStruct;


//  Service
  public List<MemberDto> searchUsers(String keyword) {
    return memberRepository.findByUserIdContainingIgnoreCase(keyword)
                           .stream()
                           .map(mapStruct::toDto)
                           .toList();
}

}
