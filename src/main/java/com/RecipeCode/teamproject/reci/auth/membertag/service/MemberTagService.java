package com.RecipeCode.teamproject.reci.auth.membertag.service;

import com.RecipeCode.teamproject.common.ErrorMsg;
import com.RecipeCode.teamproject.common.RecipeMapStruct;
import com.RecipeCode.teamproject.reci.auth.entity.Member;
import com.RecipeCode.teamproject.reci.auth.membertag.entity.MemberTag;
import com.RecipeCode.teamproject.reci.auth.membertag.repository.MemberTagRepository;
import com.RecipeCode.teamproject.reci.auth.repository.MemberRepository;
import com.RecipeCode.teamproject.reci.auth.service.MemberService;
import com.RecipeCode.teamproject.reci.feed.recipeTag.entity.RecipeTag;
import com.RecipeCode.teamproject.reci.feed.recipeTag.repository.RecipeTagRepository;
import com.RecipeCode.teamproject.reci.feed.recipes.entity.Recipes;
import com.RecipeCode.teamproject.reci.tag.dto.TagDto;
import com.RecipeCode.teamproject.reci.tag.entity.Tag;
import com.RecipeCode.teamproject.reci.tag.repository.TagRepository;
import com.RecipeCode.teamproject.reci.tag.service.TagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Log4j2
@Service
@RequiredArgsConstructor
public class MemberTagService {
    private final MemberTagRepository memberTagRepository;
    private final TagRepository tagRepository;
    private final RecipeMapStruct recipeMapStruct;
    private final TagService tagService;
    private final ErrorMsg errorMsg;
    private final MemberRepository memberRepository;



    //    멤버에 태그 저장
    public void saveTagsForMember(List<TagDto> tagDtos,
                                  Member member) {
        for (TagDto dto : tagDtos) {
//            1.태그가 이미 DB에 있는지 확인(없으면 생성)
            Tag tag = tagService.saveOrGetTag(dto.getTag()); // 태그 존재 체크 -> 없으면 생성

//            2. 이미 연결된 태그인지 체크
            boolean exists = memberTagRepository.existsByMemberAndTag(member,tag);
            if (exists) continue; // 중복 방지

//            3. 새로 연결
            MemberTag memberTag = new MemberTag();
            memberTag.setMember(member);
            memberTag.setTag(tag);

            // 양방향 동기화
            member.getMemberTags().add(memberTag);
            memberTagRepository.save(memberTag);
        }
    }
    //  RecipeTagService 쪽에 연결 끊긴 태그 처리 로직 추가
    public void cleanupUnusedTags() {
        List<Long> inUseIds = memberTagRepository.findAllTagIdsInUse();
        List<Tag> allTags = tagRepository.findAll();
        for (Tag tag : allTags) {
            if (!inUseIds.contains(tag.getTagId()) && !tag.isDeleted()) {
                tag.setDeleted(true);
                tagRepository.save(tag);
            }
        }
    }


    @Transactional
    public void syncTagsForMember(Member member, List<TagDto> incomingDtos) {
        List<TagDto> safeDtos = (incomingDtos == null) ? List.of() : incomingDtos;

        // 1) 현재 연결된 MemberTag들(Tag까지 fetch)
        List<MemberTag> currentLinks = memberTagRepository.findByMemberWithTag(member.getUserEmail());

        // 2) 현재 연결 맵 (정규화된 문자열 -> 링크)
        java.util.function.Function<String, String> norm = s ->
                (s == null) ? "" : s.trim().replaceAll("\\s+", " ").toLowerCase();
        java.util.Map<String, MemberTag> currentMap = new java.util.HashMap<>();
        for (MemberTag mt : currentLinks) {
            currentMap.put(norm.apply(mt.getTag().getTag()), mt);
        }

        // 3) DTO 태그 normalize + 중복 제거
        java.util.LinkedHashSet<String> want = new java.util.LinkedHashSet<>();
        for (TagDto dto : safeDtos) {
            String key = norm.apply(dto.getTag());
            if (!key.isBlank()) want.add(key);
        }

        // 4) 추가 또는 유지 판단
        for (String key : want) {
            MemberTag link = currentMap.remove(key);
            if (link == null) {
                // 새 태그 연결
                Tag tag = tagService.saveOrGetTag(key); // 삭제된 상태면 revive
                MemberTag newLink = new MemberTag();
                newLink.setMember(member);
                newLink.setTag(tag);
                member.getMemberTags().add(newLink);
                memberTagRepository.save(newLink);
            }
        }

        // 5) 남은 것 = 원치 않는 연결 -> 삭제
        if (!currentMap.isEmpty()) {
            java.util.Collection<MemberTag> toDelete = currentMap.values();
            member.getMemberTags().removeAll(toDelete);
            memberTagRepository.deleteAll(toDelete);
        }

        // 6) 아무도 안 쓰는 태그는 soft delete
        cleanupUnusedTags();
    }

}
