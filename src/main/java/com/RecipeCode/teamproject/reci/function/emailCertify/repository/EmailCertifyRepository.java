package com.RecipeCode.teamproject.reci.function.emailCertify.repository;

import com.RecipeCode.teamproject.reci.auth.entity.Member;
import com.RecipeCode.teamproject.reci.function.emailCertify.entity.EmailCertify;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailCertifyRepository extends JpaRepository<EmailCertify, Long> {
    Optional<EmailCertify> findTopByMemberOrderByTokenIdDesc(Member member);

    Optional<EmailCertify> findByMemberAndCode(Member member, String code);
}
