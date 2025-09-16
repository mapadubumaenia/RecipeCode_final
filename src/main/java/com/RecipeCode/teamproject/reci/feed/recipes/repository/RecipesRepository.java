package com.RecipeCode.teamproject.reci.feed.recipes.repository;

import com.RecipeCode.teamproject.reci.feed.recipes.entity.Recipes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
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

// 마이페이지 내 피드조회
//    Slice<> : Spring Data JPA 같은 프레임워크에서 Slice는 페이징 처리 시 사용
//    전체 데이터의 총 개수나 총 페이지 수를 제공하지 않고, 다음 페이지 존재 여부만을 알려주어 무한 스크롤과 같은 구현
    @Query(value = "select r from Recipes r\n"+
                   "where r.member.userEmail = :useremail\n"+
                   "and r.deleted = false "+
                   "order by r.insertTime desc")
    Slice<Recipes> findByUserEmail(@Param("userEmail") String userEmail,
                                   Pageable pageable);


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

    // 삭제 포함 단건 조회 (native로 @Where 무시)
    @Query(value = "SELECT * FROM RECIPES WHERE UUID = :uuid", nativeQuery = true)
    Optional<Recipes> findIncludingDeleted(@Param("uuid") String uuid);

    // 목록도 필요하면 비슷하게…
    @Query(value = "SELECT * FROM RECIPES WHERE DELETED IN ('Y','N') ORDER BY CREATED_AT DESC",
            nativeQuery = true)
    List<Recipes> findAllIncludingDeleted();




}
