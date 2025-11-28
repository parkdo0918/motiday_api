package com.example.motiday_api.domain.stats.repository;

import com.example.motiday_api.domain.routine.entity.routine.Routine;
import com.example.motiday_api.domain.stats.entity.RoutineStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface RoutineStatsRepository extends JpaRepository<RoutineStats, Long> {

    // 날짜별 통계
    Optional<RoutineStats> findByRoutineAndDate(Routine routine, LocalDate date);
}