package com.RecipeCode.teamproject.reci.auth.repository;


import com.RecipeCode.teamproject.reci.auth.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member,String> {
    Optional<Member> findByUserEmail(String userEmail);
}
