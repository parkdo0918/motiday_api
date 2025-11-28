package com.example.motiday_api.domain.routine.service;

import com.example.motiday_api.domain.moti.entity.MotiTransaction;
import com.example.motiday_api.domain.moti.repository.MotiTransactionRepository;
import com.example.motiday_api.domain.routine.entity.certification.WeeklyCertification;
import com.example.motiday_api.domain.routine.entity.participant.RoutineParticipant;
import com.example.motiday_api.domain.routine.entity.routine.Routine;
import com.example.motiday_api.domain.routine.repository.RoutineParticipantRepository;
import com.example.motiday_api.domain.routine.repository.RoutineRepository;
import com.example.motiday_api.domain.routine.repository.WeeklyCertificationRepository;
import com.example.motiday_api.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

// 주간 스케줄러
@Slf4j
@Service
@RequiredArgsConstructor
public class WeeklyCertificationService {

    private final WeeklyCertificationRepository weeklyCertRepository;
    private final RoutineParticipantRepository participantRepository;
    private final RoutineRepository routineRepository;
    private final MotiTransactionRepository transactionRepository;

    // 매일 자정 실행 - 주간 보상 지급
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void processWeeklyRewards() {
        log.info("주간 보상 지급 스케줄러 시작");

        List<RoutineParticipant> activeParticipants =
                participantRepository.findAll().stream()
                        .filter(RoutineParticipant::isActive)
                        .toList();

        for (RoutineParticipant participant : activeParticipants) {
            processParticipantWeeklyReward(participant);
        }

        log.info("주간 보상 지급 완료");
    }

    // 개인별 주간 보상 처리
    private void processParticipantWeeklyReward(RoutineParticipant participant) {
        User user = participant.getUser();
        Routine routine = participant.getRoutine();

        // 참여 후 경과 일수
        long daysSinceJoined = ChronoUnit.DAYS.between(
                participant.getJoinedAt().toLocalDate(),
                LocalDate.now()
        );

        // 현재 주차
        int currentWeek = (int) (daysSinceJoined / 7) + 1;

        // 주차 종료일 확인
        LocalDate weekStartDate = participant.getJoinedAt().toLocalDate()
                .plusWeeks(currentWeek - 1);
        LocalDate weekEndDate = weekStartDate.plusDays(6);

        // 오늘이 주차 종료일이 아니면 스킵
        if (!LocalDate.now().equals(weekEndDate)) {
            return;
        }

        // 해당 주차 인증 기록 조회
        WeeklyCertification weeklyCert = weeklyCertRepository
                .findByUserAndRoutineAndWeekNumber(user, routine, currentWeek)
                .orElse(null);

        if (weeklyCert == null) {
            log.info("User {} - Routine {} Week {} : 인증 기록 없음",
                    user.getId(), routine.getId(), currentWeek);

            // 실패 처리 (스트릭 초기화)
            participant.resetStreak();
            return;
        }

        // 주간 목표 달성 여부 확인 및 보상 지급
        int requiredFrequency = routine.getDifficulty().getRequiredFrequency();
        int weeklyMoti = routine.getDifficulty().getWeeklyMoti();

        weeklyCert.completeWeek(requiredFrequency, weeklyMoti);

        if (weeklyCert.getIsSuccess()) {
            // 성공: 모티 지급
            user.addMoti(weeklyMoti);
            participant.increaseStreak();

            // 거래 내역 생성
            MotiTransaction transaction = MotiTransaction.createWeeklyReward(
                    user, routine, weeklyCert, weeklyMoti
            );
            transactionRepository.save(transaction);

            log.info("User {} - Routine {} Week {} : 보상 지급 {}MOTI",
                    user.getId(), routine.getId(), currentWeek, weeklyMoti);
        } else {
            // 실패: 스트릭 초기화
            participant.resetStreak();

            log.info("User {} - Routine {} Week {} : 목표 미달성 ({}회 / {}회)",
                    user.getId(), routine.getId(), currentWeek,
                    weeklyCert.getCertificationCount(), requiredFrequency);
        }

        // 주차 증가
        participant.increaseWeekNumber();
    }
}