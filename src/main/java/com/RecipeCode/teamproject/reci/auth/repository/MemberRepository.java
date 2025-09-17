package com.RecipeCode.teamproject.reci.auth.repository;


import com.RecipeCode.teamproject.reci.auth.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member,String> {
    Optional<Member> findByUserEmail(String userEmail);

    // TODO: Profile(Mypage) 안에서 userId 검색
    List<Member> findByUserIdContainingIgnoreCase(String keyword);
}
