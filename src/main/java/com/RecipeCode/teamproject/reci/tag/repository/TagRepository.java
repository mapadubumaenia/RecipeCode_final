package com.RecipeCode.teamproject.reci.tag.repository;


import com.RecipeCode.teamproject.reci.tag.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

    //    대소문자 무시+삭제되지 않은 태그만 확인
    @Query(value = "select t from Tag t\n"+
            "where lower(t.tag) = lower(:tag)\n"+
            "and t.deleted = false ")
    Optional<Tag> findByTagIgnoreCaseAndDeletedFalse(@Param("tag") String tag);

    List<Tag> findByDeletedFalse();

    //    중복여부 boolean 체크용
    boolean existsByTag(String tag);

    // 삭제 여부 무시하고 한 방에 찾기 (대소문자 무시)
    Optional<Tag> findByTagIgnoreCase(String tag);


}
