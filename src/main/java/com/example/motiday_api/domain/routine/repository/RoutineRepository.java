package com.example.motiday_api.domain.routine.repository;

import com.example.motiday_api.domain.routine.entity.routine.Category;
import com.example.motiday_api.domain.routine.entity.routine.Routine;
import com.example.motiday_api.domain.routine.entity.routine.RoutineStatus;
import com.example.motiday_api.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoutineRepository extends JpaRepository<Routine, Long> {

    // 카테고리별 조회
    List<Routine> findByCategory(Category category);

    // 전체 활성 루틴 (모집중)
    List<Routine> findByStatusOrderByCreatedAtDesc(RoutineStatus status);

    // 카테고리 + 활성 루틴
    List<Routine> findByCategoryAndStatusOrderByCreatedAtDesc(Category category, RoutineStatus status);

    // 참여중 (내가 참여한 루틴)
    @Query("SELECT rp.routine FROM RoutineParticipant rp " +
            "WHERE rp.user = :user AND rp.status = 'ACTIVE'")
    List<Routine> findParticipatingRoutines(@Param("user") User user);
}