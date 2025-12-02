package com.example.motiday_api.domain.feed.repository;

import com.example.motiday_api.domain.feed.entity.Feed;
import com.example.motiday_api.domain.routine.entity.routine.Routine;
import com.example.motiday_api.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedRepository extends JpaRepository<Feed, Long> {

    // 홈 피드 (전체)
    @Query("SELECT f FROM Feed f JOIN FETCH f.user JOIN FETCH f.routine ORDER BY f.createdAt DESC")
    List<Feed> findAllByOrderByCreatedAtDesc();

    // 활동 게시물 (루틴별 + 공유 허용)
    @Query("SELECT f FROM Feed f JOIN FETCH f.user JOIN FETCH f.routine WHERE f.routine = :routine AND f.isSharedToRoutine = true ORDER BY f.createdAt DESC")
    List<Feed> findByRoutineAndIsSharedToRoutineTrueOrderByCreatedAtDesc(Routine routine);

    // 사용자별 피드 (프로필)
    @Query("SELECT f FROM Feed f JOIN FETCH f.user JOIN FETCH f.routine WHERE f.user = :user ORDER BY f.createdAt DESC")
    List<Feed> findByUserOrderByCreatedAtDesc(User user);
}