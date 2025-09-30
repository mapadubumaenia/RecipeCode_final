package com.RecipeCode.teamproject.reci.auth.membertag.repository;

import com.RecipeCode.teamproject.reci.auth.entity.Member;
import com.RecipeCode.teamproject.reci.auth.membertag.entity.MemberTag;
import com.RecipeCode.teamproject.reci.tag.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;

public interface MemberTagRepository extends JpaRepository<MemberTag, Long> {
    //    userEmail로 찾기
    List<MemberTag> findByMember(Member member);

    //    존재하는 태그 확인
    boolean existsByMemberAndTag(Member member, Tag tag);
    void deleteByMember_UserEmail(String userEmail);

    @Query("select distinct mt.tag.tagId from MemberTag mt")
    List<Long> findAllTagIdsInUse();

    @Query("select mt from MemberTag mt join fetch mt.tag where mt.member.userEmail = :email")
    List<MemberTag> findByMemberWithTag(@Param("email") String email);

    boolean existsByTag(Tag tag);
}
