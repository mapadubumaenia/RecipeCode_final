package com.RecipeCode.teamproject.reci.auth.repository;


import com.RecipeCode.teamproject.reci.auth.entity.Member;
import com.RecipeCode.teamproject.reci.auth.membertag.entity.MemberTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member,String> {
    Optional<Member> findByUserEmail(String userEmail);
    //프로필이미지용
    Optional<Member> findByUserId(String userId);
    //태그를 포함한 멤버정보 불러오기
    @Query("select distinct m from Member m " +
            "left join fetch m.memberTags mt " +
            "left join fetch mt.tag t " +
            "where m.userEmail = :email and (t is null or t.deleted = false)")
    Optional<Member> findByUserEmailWithTags(@Param("email") String email);


    // TODO: Profile(Mypage) 안에서 userId 검색
    List<Member> findByUserIdContainingIgnoreCase(String keyword);




}
