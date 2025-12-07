package com.example.motiday_api.domain.routine.repository;

import com.example.motiday_api.domain.routine.entity.certification.WeeklyCertification;
import com.example.motiday_api.domain.routine.entity.routine.Routine;
import com.example.motiday_api.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WeeklyCertificationRepository extends JpaRepository<WeeklyCertification, Long> {

    // 인증 시 해당 주차 찾기
    Optional<WeeklyCertification> findByUserAndRoutineAndWeekNumber(
            User user,
            Routine routine,
            Integer weekNumber
    );

    // 사용자별 인증 기록 삭제 (회원탈퇴 시)
    void deleteByUser(User user);
}