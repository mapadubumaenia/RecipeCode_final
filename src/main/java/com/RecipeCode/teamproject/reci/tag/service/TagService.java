package com.RecipeCode.teamproject.reci.tag.service;

import com.RecipeCode.teamproject.common.ErrorMsg;
import com.RecipeCode.teamproject.common.RecipeMapStruct;
import com.RecipeCode.teamproject.reci.auth.membertag.repository.MemberTagRepository;
import com.RecipeCode.teamproject.reci.feed.recipeTag.repository.RecipeTagRepository;
import com.RecipeCode.teamproject.reci.tag.dto.TagDto;
import com.RecipeCode.teamproject.reci.tag.entity.Tag;
import com.RecipeCode.teamproject.reci.tag.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TagService {
    private final TagRepository tagRepository;
    private final RecipeTagRepository recipeTagRepository;
    private final MemberTagRepository memberTagRepository;
    private final RecipeMapStruct recipeMapStruct;
    private final ErrorMsg errorMsg;

    //    태그 저장(신규 + 재사용)
    public Tag saveOrGetTag(String rawtag){

        String key = normalize(rawtag);

        // 삭제여부 상관없이 먼저 찾기 (대소문자 무시)
        Optional<Tag> opt = tagRepository.findByTagIgnoreCase(key);

        if (opt.isPresent()) {
            Tag tag = opt.get();
            if (tag.isDeleted()) {
                tag.setDeleted(false);       // ✅ 재활성화
                return tagRepository.save(tag);
            }
            return tag;                      // ✅ 기존 거 재사용
        }

        // 없으면 새로 생성
        Tag newTag = new Tag();
        newTag.setTag(key);
        newTag.setDeleted(false);
        return tagRepository.save(newTag);
    }

    private String normalize(String s) {
        if (s == null) return "";
        // 공백 정리 + 대소문자 통일(영문 대비)
        return s.trim().replaceAll("\\s+", " ").toLowerCase();
    }



    //    태그 전체 조회
    public List<TagDto> getAllTags(){
        List<Tag> tags = tagRepository.findByDeletedFalse();
        if(tags.isEmpty()){
            throw new RuntimeException(errorMsg.getMessage("errors.not.found"));
        }
        return recipeMapStruct.toTagDtoList(tags);
    }

    //    태그 논리 삭제
    public void tagLogicDelete(Long tagId){
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(()->new RuntimeException(errorMsg.getMessage("errors.not.found")));
        tag.setDeleted(true);
    }

    @Transactional
    public void cleanupUnusedTags() {
        List<Tag> allTags = tagRepository.findAll();
        for (Tag tag : allTags) {
            boolean usedByMember = memberTagRepository.existsByTag(tag);
            boolean usedByRecipe = recipeTagRepository.existsByTag(tag);

            if (!usedByMember && !usedByRecipe && !tag.isDeleted()) {
                tag.setDeleted(true);
                tagRepository.save(tag);
            }
        }
    }


}
