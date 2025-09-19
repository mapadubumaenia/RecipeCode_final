package com.RecipeCode.teamproject.reci.auth.membertag.service;

import com.RecipeCode.teamproject.common.ErrorMsg;
import com.RecipeCode.teamproject.common.RecipeMapStruct;
import com.RecipeCode.teamproject.reci.auth.entity.Member;
import com.RecipeCode.teamproject.reci.auth.membertag.entity.MemberTag;
import com.RecipeCode.teamproject.reci.auth.membertag.repository.MemberTagRepository;
import com.RecipeCode.teamproject.reci.auth.repository.MemberRepository;
import com.RecipeCode.teamproject.reci.feed.recipeTag.entity.RecipeTag;
import com.RecipeCode.teamproject.reci.feed.recipeTag.repository.RecipeTagRepository;
import com.RecipeCode.teamproject.reci.feed.recipes.entity.Recipes;
import com.RecipeCode.teamproject.reci.tag.dto.TagDto;
import com.RecipeCode.teamproject.reci.tag.entity.Tag;
import com.RecipeCode.teamproject.reci.tag.repository.TagRepository;
import com.RecipeCode.teamproject.reci.tag.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberTagService {
    private final MemberTagRepository memberTagRepository;
    private final TagRepository tagRepository;
    private final RecipeMapStruct recipeMapStruct;
    private final TagService tagService;
    private final ErrorMsg errorMsg;

    //    레시피에 태그 저장
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
            member.getMemberTag().add(memberTag);
            memberTagRepository.save(memberTag);
        }
    }

}
