package com.RecipeCode.teamproject.reci.tag.repository;


import com.RecipeCode.teamproject.reci.tag.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

    //    태그명이 이미 존재하는지 확인
    Optional<Tag> findByTag(String tag);

    //    중복여부 boolean 체크용
    boolean existsByTag(String tag);
}
