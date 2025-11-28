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

    // 전체 활성 루틴
    List<Routine> findByStatusOrderByCreatedAtDesc(RoutineStatus status);

    // 모집 중인 루틴 (정원 미달)
    @Query("SELECT r FROM Routine r WHERE r.status = 'ACTIVE' " +
            "AND r.currentParticipants < r.maxParticipants " +
            "AND (:category IS NULL OR r.category = :category) " +
            "ORDER BY r.createdAt DESC")
    List<Routine> findRecruitingRoutines(@Param("category") Category category);

    // 마감된 루틴 (정원 초과)
    @Query("SELECT r FROM Routine r WHERE r.status = 'ACTIVE' " +
            "AND r.currentParticipants >= r.maxParticipants " +
            "AND (:category IS NULL OR r.category = :category) " +
            "ORDER BY r.createdAt DESC")
    List<Routine> findClosedRoutines(@Param("category") Category category);

    // 참여중 (내가 참여한 루틴)
    @Query("SELECT rp.routine FROM RoutineParticipant rp " +
            "WHERE rp.user = :user AND rp.status = 'ACTIVE'")
    List<Routine> findParticipatingRoutines(@Param("user") User user);
}