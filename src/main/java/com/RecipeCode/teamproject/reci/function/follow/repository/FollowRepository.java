package com.RecipeCode.teamproject.reci.function.follow.repository;

import com.RecipeCode.teamproject.reci.auth.entity.Member;
import com.RecipeCode.teamproject.reci.function.follow.entity.Follow;
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
public interface FollowRepository extends JpaRepository<Follow, Long> {
    // 내가 팔로우한 사람들
    Slice<Follow> findByFollower(Member follower, Pageable pageable);

    // 나를 팔로우한 사람들
    Slice<Follow> findByFollowing(Member following, Pageable pageable);

    // 중복 팔로우 방지
    boolean existsByFollowerAndFollowing(Member follower, Member following);

    // 단건 조회
    Optional<Follow> findByFollowerAndFollowing(Member follower, Member following);

    // 팔로워 수 (나를 팔로우하는 사람들 수)
    long countByFollowing(Member following);

    // 팔로잉 수 (내가 팔로우하는 사람들 수)
    long countByFollower(Member follower);

    // 내가 팔로우하는 사람들의 userId 목록
    @Query(value = "select f.following.userId from Follow f\n" +
                   "where f.follower.userEmail = :viewerEmail")
    List<String> findFollowingUserIds(@Param("viewerEmail") String viewerEmail);

    // 나를 팔로우하는 사람들의 userId 목록
    @Query(value = "select f.follower.userId from Follow f\n" +
            "where f.following.userEmail = :viewerEmail")
    List<String> findFollowerUserIds(@Param("viewerEmail") String viewerEmail);


}
