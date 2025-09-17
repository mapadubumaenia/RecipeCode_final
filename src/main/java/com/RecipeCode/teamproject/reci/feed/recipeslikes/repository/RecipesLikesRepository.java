package com.RecipeCode.teamproject.reci.feed.recipeslikes.repository;

import com.RecipeCode.teamproject.reci.auth.entity.Member;
import com.RecipeCode.teamproject.reci.feed.ingredient.entity.Ingredient;
import com.RecipeCode.teamproject.reci.feed.recipes.entity.Recipes;
import com.RecipeCode.teamproject.reci.feed.recipeslikes.entity.RecipesLikes;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecipesLikesRepository extends JpaRepository<RecipesLikes, Long> {

//    눌렀는지 확인하기
    boolean existsByMemberAndRecipes(Member member, Recipes recipes);

//    지우기
    void deleteByMemberAndRecipes(Member member, Recipes recipes);

//    갯수세기
    long countByRecipes(Recipes recipes);

//    소프트 삭제제외하고 조회
    @Query(value = "select count(l) from RecipesLikes l\n"+
                    "where l.recipes.uuid = :uuid\n" +
                    "and l.recipes.deleted = false")
    long countVisibleLikes(@Param("uuid") String uuid);


//    마이페이지 내가 좋아요 한 피드조회
    @Query(value = "select r from Recipes r\n" +
            "join RecipesLikes l on r.uuid = l.recipes.uuid\n" +
            "where l.member.userEmail = :userEmail\n"+
            "and r.deleted = false\n"+
            "order by r.insertTime desc")
    Slice<Recipes> findByLikedRecipes(@Param("userEmail")String userEmail,
                                      Pageable pageable);

}
