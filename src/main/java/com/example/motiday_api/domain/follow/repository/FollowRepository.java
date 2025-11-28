package com.example.motiday_api.domain.follow.repository;

import com.example.motiday_api.domain.follow.entity.Follow;
import com.example.motiday_api.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {

    // 팔로우 관계 조회
    Optional<Follow> findByFollowerAndFollowing(User follower, User following);

    // 팔로우 여부 확인
    boolean existsByFollowerAndFollowing(User follower, User following);

    // 팔로워 목록 (나를 팔로우하는 사람들)
    List<Follow> findByFollowing(User following);

    // 팔로잉 목록 (내가 팔로우하는 사람들)
    List<Follow> findByFollower(User follower);

    // 팔로워 수
    int countByFollowing(User following);

    // 팔로잉 수
    int countByFollower(User follower);
}