package com.RecipeCode.teamproject.reci.faq.repository;

import com.RecipeCode.teamproject.reci.faq.entity.Faq;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FaqRepository extends JpaRepository<Faq, Long> {
//    검색어 조회
    @Query(value = "select f from Faq f\n" +
            "where f.faqQuestion like %:searchKeyword%")
    Page<Faq> selectFaqList(
            @Param("searchKeyword") String searchKeyword,
            Pageable pageable
    );
    // 카테고리(tag) 조회
    @Query("select f from Faq f " +
            "where f.faqTag = :tag")
    Page<Faq> selectFaqListByTag(
            @Param("tag") String tag,
            Pageable pageable
    );
}
