package com.RecipeCode.teamproject.reci.auth.repository;

import com.RecipeCode.teamproject.reci.auth.entity.Member;

import java.util.Optional;

public interface MemberRepositoryCustom {
    Optional<Member> findByUserEmail(String email);
    Optional<Member> findByUserId(String userId);
}
