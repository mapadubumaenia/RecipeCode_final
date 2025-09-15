package com.RecipeCode.teamproject.reci.feed.recipes.repository;

import com.RecipeCode.teamproject.reci.feed.recipes.entity.Recipes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecipesRepository extends JpaRepository<Recipes, String> {

//  특정 유저(ID검색) 레시피 최신순(피드)
    Page<Recipes> findByMember_UserIdInAndPostStatusOrderByInsertTimeDesc(
            List<String> userIds,
            String postStatus,
            Pageable pageable
    );

//  인기순(Id기준)
    Page<Recipes> findByMember_UserIdInAndPostStatusOrderByLikeCountDesc(
            List<String> userEmails,
            String postStatus,
            Pageable pageable
    );

//  마이페이지 서치용(id 뿐 아니라 다른 검색을 넣을거라면 사용)
    @Query(value = "select r from Recipes r\n"+
                    "where r.member.userId in :userIds\n"+
                    "and r.postStatus = :status")
    Page<Recipes> findFeedRecipes(
            @Param("userIds") List<String> userEmails,
            @Param("status") String status,
            Pageable pageable
    );

    @Query("SELECT r FROM Recipes r\n " +
            "LEFT JOIN FETCH RecipeContent\n " +
            "WHERE r.uuid = :uuid")
    Optional<Recipes> findByUuid(String uuid);


//    섬네일만 바로 가져옴
    @Query(value = "select r.thumbnail from Recipes r where r.uuid = :uuid")
    byte[] findThumbnailByUuid(@Param("uuid") String uuid);


//    레시피 태그조회 : 태그 테이블과 조인패치
@Query(value = "select DISTINCT r from Recipes r\n" +
        "left join fetch r.recipeTag rt\n" +
        "left join fetch rt.tag\n" +
        "where r.uuid = :uuid")
Optional<Recipes> findByIdWithTags(@Param("uuid") String uuid);
}
