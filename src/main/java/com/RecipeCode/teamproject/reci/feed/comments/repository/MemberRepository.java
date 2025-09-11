package com.RecipeCode.teamproject.reci.feed.comments.repository;

import com.RecipeCode.teamproject.reci.auth.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, String> {
}
