package com.RecipeCode.teamproject.reci.auth.repository;

import com.RecipeCode.teamproject.reci.admin.entity.Admin;
import com.RecipeCode.teamproject.reci.auth.entity.Member;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
    public class MemberRepositoryImpl implements MemberRepositoryCustom {

    @PersistenceContext
    private final EntityManager em;

    @Override
    public Optional<Member> findByUserEmail(String email) {
        // 1️⃣ 일반 회원(users 테이블)
        List<Member> users = em.createQuery(
                        "SELECT m FROM Member m WHERE m.userEmail = :email", Member.class)
                .setParameter("email", email)
                .getResultList();

        if (!users.isEmpty()) {
            return Optional.of(users.get(0));
        }

        // 2️⃣ 관리자(admins 테이블)
        List<Admin> admins = em.createQuery(
                        "SELECT a FROM Admin a WHERE a.adminEmail = :email", Admin.class)
                .setParameter("email", email)
                .getResultList();

        if (!admins.isEmpty()) {
            Admin a = admins.get(0);
            Member m = new Member();
            m.setUserEmail(a.getAdminEmail());
            m.setUserId(a.getAdminId());
            m.setNickname(a.getNickname());
            m.setPassword(a.getPassword());
            m.setProfileImage(a.getProfileImage());
            m.setProfileImageUrl(a.getProfileImageUrl());
            m.setRole(a.getRole());
            m.setUserBlog(a.getAdminBlog());
            m.setUserInsta(a.getAdminInsta());
            m.setUserIntroduce(a.getAdminIntroduce());
            m.setUserLocation(a.getAdminLocation());
            m.setUserWebsite(a.getAdminWebsite());
            m.setUserYoutube(a.getAdminYoutube());
            m.setProfileStatus(a.getProfileStatus());
            m.setDeleted(a.getDeleted());
            m.setDeletedAt(a.getDeletedAt());
            m.setProvider(a.getProvider());
            m.setProviderId(a.getProviderId());
            return Optional.of(m);
        }

        return Optional.empty();
    }

    @Override
    public Optional<Member> findByUserId(String userId) {
        List<Member> members = em.createQuery(
                        "SELECT m FROM Member m WHERE m.userId = :userId", Member.class)
                .setParameter("userId", userId)
                .getResultList();

        if (!members.isEmpty()) return Optional.of(members.get(0));

        // 2️⃣ 관리자(admins 테이블)
        List<Admin> admins = em.createQuery(
                        "SELECT a FROM Admin a WHERE a.adminId = :userId", Admin.class)
                .setParameter("userId", userId)
                .getResultList();

        if (!admins.isEmpty()) {
            Admin a = admins.get(0);
            Member m = new Member();
            m.setUserEmail(a.getAdminEmail());
            m.setUserId(a.getAdminId());
            m.setNickname(a.getNickname());
            m.setPassword(a.getPassword());
            m.setProfileImage(a.getProfileImage());
            m.setProfileImageUrl(a.getProfileImageUrl());
            m.setRole(a.getRole());
            m.setUserBlog(a.getAdminBlog());
            m.setUserInsta(a.getAdminInsta());
            m.setUserIntroduce(a.getAdminIntroduce());
            m.setUserLocation(a.getAdminLocation());
            m.setUserWebsite(a.getAdminWebsite());
            m.setUserYoutube(a.getAdminYoutube());
            m.setProfileStatus(a.getProfileStatus());
            m.setDeleted(a.getDeleted());
            m.setDeletedAt(a.getDeletedAt());
            m.setProvider(a.getProvider());
            m.setProviderId(a.getProviderId());
            return Optional.of(m);
        }

        return Optional.empty();
    }
}
