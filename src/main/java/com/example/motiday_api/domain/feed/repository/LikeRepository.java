package com.example.motiday_api.domain.feed.repository;

import com.example.motiday_api.domain.feed.entity.Feed;
import com.example.motiday_api.domain.feed.entity.Like;
import com.example.motiday_api.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {

    // 좋아요 여부 확인
    Optional<Like> findByFeedAndUser(Feed feed, User user);

    // 좋아요 존재 여부
    boolean existsByFeedAndUser(Feed feed, User user);

    // 사용자별 좋아요 삭제 (회원탈퇴 시)
    void deleteByUser(User user);

}