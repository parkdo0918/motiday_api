package com.example.motiday_api.domain.routine.service;

import com.example.motiday_api.domain.routine.entity.certification.WeeklyCertification;
import com.example.motiday_api.domain.routine.entity.participant.ParticipantStatus;
import com.example.motiday_api.domain.routine.entity.participant.RoutineParticipant;
import com.example.motiday_api.domain.routine.entity.routine.Routine;
import com.example.motiday_api.domain.routine.repository.RoutineParticipantRepository;
import com.example.motiday_api.domain.routine.repository.WeeklyCertificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PenaltyService {

    private final RoutineParticipantRepository participantRepository;
    private final WeeklyCertificationRepository weeklyCertRepository;

    // 매일 자정 실행 - 페널티/강퇴 처리
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void processPenalties() {
        log.info("페널티 처리 스케줄러 시작");

        // 정지 해제 처리
        releasePenalties();

        // 페널티/강퇴 체크
        checkAndApplyPenalties();

        log.info("페널티 처리 완료");
    }

    // 정지 해제 처리
    private void releasePenalties() {
        List<RoutineParticipant> penaltyParticipants = participantRepository.findAll().stream()
                .filter(RoutineParticipant::isPenalty)
                .filter(p -> p.getPenaltyEndDate() != null)
                .filter(p -> LocalDate.now().isAfter(p.getPenaltyEndDate()))
                .toList();

        for (RoutineParticipant participant : penaltyParticipants) {
            participant.releasePenalty();
            log.info("User {} - Routine {} : 정지 해제",
                    participant.getUser().getId(),
                    participant.getRoutine().getId());
        }
    }

    // 페널티/강퇴 체크
    private void checkAndApplyPenalties() {
        List<RoutineParticipant> activeParticipants = participantRepository.findAll().stream()
                .filter(RoutineParticipant::isActive)
                .toList();

        for (RoutineParticipant participant : activeParticipants) {
            checkParticipantPenalty(participant);
        }
    }

    // 개인별 페널티 체크
    private void checkParticipantPenalty(RoutineParticipant participant) {
        Routine routine = participant.getRoutine();
        int currentWeek = participant.getCurrentWeekNumber();

        // 최근 2주 연속 실패 체크
        boolean recentTwoWeeksFailed = checkRecentTwoWeeksFailure(
                participant,
                routine,
                currentWeek
        );

        if (!recentTwoWeeksFailed) {
            return;
        }

        // 페널티 이력 확인
        if (participant.getPenaltyCount() == 0) {
            // 첫 실패: 1주 정지
            participant.applyPenalty();
            log.info("User {} - Routine {} : 1주 정지 부여 (14일 연속 미달)",
                    participant.getUser().getId(),
                    routine.getId());
        } else {
            // 두 번째 실패: 강퇴
            participant.kick();
            routine.decreaseParticipants();
            log.info("User {} - Routine {} : 강퇴 (정지 후 재실패)",
                    participant.getUser().getId(),
                    routine.getId());
        }
    }

    // 최근 2주 연속 실패 체크
    private boolean checkRecentTwoWeeksFailure(
            RoutineParticipant participant,
            Routine routine,
            int currentWeek
    ) {
        if (currentWeek < 2) {
            return false;  // 2주차 미만은 체크 안 함
        }

        // 최근 2주 인증 기록
        WeeklyCertification lastWeek = weeklyCertRepository
                .findByUserAndRoutineAndWeekNumber(
                        participant.getUser(),
                        routine,
                        currentWeek - 1
                )
                .orElse(null);

        WeeklyCertification twoWeeksAgo = weeklyCertRepository
                .findByUserAndRoutineAndWeekNumber(
                        participant.getUser(),
                        routine,
                        currentWeek - 2
                )
                .orElse(null);

        // 둘 다 실패인 경우
        return (lastWeek == null || !lastWeek.getIsSuccess()) &&
                (twoWeeksAgo == null || !twoWeeksAgo.getIsSuccess());
    }
}