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
    @Query(value = "select f from Faq f\n" +
            "where f.faq_question like %:searchKeyword%")
    Page<Faq> selectFaqList(
            @Param("searchKeyword") String searchKeyword,
            Pageable pageable
    );
}
