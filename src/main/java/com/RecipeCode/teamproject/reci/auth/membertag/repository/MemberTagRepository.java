package com.RecipeCode.teamproject.reci.auth.membertag.repository;

import com.RecipeCode.teamproject.reci.auth.entity.Member;
import com.RecipeCode.teamproject.reci.auth.membertag.entity.MemberTag;
import com.RecipeCode.teamproject.reci.feed.recipeTag.entity.RecipeTag;
import com.RecipeCode.teamproject.reci.feed.recipes.entity.Recipes;
import com.RecipeCode.teamproject.reci.tag.entity.Tag;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MemberTagRepository {
    //    userEmail로 찾기
    List<MemberTag> findByMember(Member member);

    //    존재하는 태그 확인
    boolean existsByMemberAndTag(Member member, Tag tag);



}
