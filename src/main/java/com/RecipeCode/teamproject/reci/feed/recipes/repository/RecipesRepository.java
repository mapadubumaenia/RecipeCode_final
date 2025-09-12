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

//  최신순(email 조회-관리자에 필요할까 싶어서 만들어둠)
    Page<Recipes> findByMember_UserEmailInAndPostStatusOrderByInsertTimeDesc(
            List<String> userEmails,
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

//    uuid로 레시피 찾기 상세조회
//    @Query("SELECT r FROM Recipes r \n" +
//            "LEFT JOIN FETCH r.ingredients \n" +
//            "LEFT JOIN FETCH r.contents \n" +
//            "WHERE r.uuid = :uuid")
//    Optional<Recipes> findByUuid(String uuid);

    @Query("SELECT r FROM Recipes r " +
            "LEFT JOIN FETCH r.contents " +
            "WHERE r.uuid = :uuid")
    Optional<Recipes> findByUuid(String uuid);

//    섬네일만 바로 가져옴
    @Query(value = "select r.thumbnail from Recipes r where r.uuid = :uuid")
    byte[] findThumbnailByUuid(@Param("uuid") String uuid);

}
