package com.example.motiday_api.domain.routine.repository;

import com.example.motiday_api.domain.routine.entity.participant.ParticipantStatus;
import com.example.motiday_api.domain.routine.entity.participant.RoutineParticipant;
import com.example.motiday_api.domain.routine.entity.routine.Category;
import com.example.motiday_api.domain.routine.entity.routine.Routine;
import com.example.motiday_api.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoutineParticipantRepository extends JpaRepository<RoutineParticipant, Long> {

    // 참여 여부 확인
    Optional<RoutineParticipant> findByUserAndRoutine(User user, Routine routine);

    // 내가 참여 중인 루틴 (루틴 카테고리 중)
    List<RoutineParticipant> findByUserAndStatus(User user, ParticipantStatus status);

    // 루틴의 활성 참여자 수
    int countByRoutineAndStatus(Routine routine, ParticipantStatus status);

    // 카테고리별 1개 제한 체크
    boolean existsByUserAndRoutine_CategoryAndStatus(
            User user,
            Category category,
            ParticipantStatus status
    );

    // 사용자별 참여 기록 삭제 (회원탈퇴 시)
    void deleteByUser(User user);
}