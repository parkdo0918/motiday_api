package com.example.motiday_api.domain.feed.repository;

import com.example.motiday_api.domain.feed.entity.Feed;
import com.example.motiday_api.domain.routine.entity.routine.Routine;
import com.example.motiday_api.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedRepository extends JpaRepository<Feed, Long> {

    // 홈 피드 (전체)
    List<Feed> findAllByOrderByCreatedAtDesc();

    // 활동 게시물 (루틴별 + 공유 허용)
    List<Feed> findByRoutineAndIsSharedToRoutineTrueOrderByCreatedAtDesc(Routine routine);

    // 사용자별 피드 (프로필)
    List<Feed> findByUserOrderByCreatedAtDesc(User user);
}