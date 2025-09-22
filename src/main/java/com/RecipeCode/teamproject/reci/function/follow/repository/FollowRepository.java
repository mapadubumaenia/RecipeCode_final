package com.RecipeCode.teamproject.reci.function.follow.repository;

import com.RecipeCode.teamproject.reci.auth.entity.Member;
import com.RecipeCode.teamproject.reci.function.follow.entity.Follow;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
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

}
