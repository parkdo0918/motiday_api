package com.example.motiday_api.domain.stats.service;

import com.example.motiday_api.domain.feed.repository.FeedRepository;
import com.example.motiday_api.domain.routine.entity.participant.ParticipantStatus;
import com.example.motiday_api.domain.routine.entity.routine.Routine;
import com.example.motiday_api.domain.routine.entity.routine.RoutineStatus;
import com.example.motiday_api.domain.routine.repository.RoutineParticipantRepository;
import com.example.motiday_api.domain.routine.repository.RoutineRepository;
import com.example.motiday_api.domain.stats.entity.RoutineStats;
import com.example.motiday_api.domain.stats.repository.RoutineStatsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoutineStatsService {

    private final RoutineStatsRepository routineStatsRepository;
    private final RoutineRepository routineRepository;
    private final RoutineParticipantRepository participantRepository;
    private final FeedRepository feedRepository;

    // 매일 자정 실행 - 통계 갱신 및 방 폭파 체크
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void updateDailyStats() {
        log.info("루틴 통계 갱신 스케줄러 시작");

        List<Routine> activeRoutines = routineRepository
                .findByStatusOrderByCreatedAtDesc(RoutineStatus.ACTIVE);

        for (Routine routine : activeRoutines) {
            updateRoutineStats(routine);
            checkRoutineClosure(routine);
        }

        log.info("루틴 통계 갱신 완료");
    }

    // 루틴 통계 갱신
    private void updateRoutineStats(Routine routine) {
        LocalDate today = LocalDate.now();

        // 최근 7일/14일 인증 수 계산
        LocalDateTime sevenDaysAgo = today.minusDays(7).atStartOfDay();
        LocalDateTime fourteenDaysAgo = today.minusDays(14).atStartOfDay();

        int last7DaysCert = feedRepository.findAll().stream()
                .filter(feed -> feed.getRoutine().getId().equals(routine.getId()))
                .filter(feed -> feed.getCreatedAt().isAfter(sevenDaysAgo))
                .toList()
                .size();

        int last14DaysCert = feedRepository.findAll().stream()
                .filter(feed -> feed.getRoutine().getId().equals(routine.getId()))
                .filter(feed -> feed.getCreatedAt().isAfter(fourteenDaysAgo))
                .toList()
                .size();

        // 활성 참여자 수
        int activeCount = participantRepository.countByRoutineAndStatus(
                routine,
                ParticipantStatus.ACTIVE
        );

        // 통계 업데이트
        RoutineStats stats = routineStatsRepository
                .findByRoutineAndDate(routine, today)
                .orElseGet(() -> RoutineStats.createToday(routine));

        stats.updateStats(last7DaysCert, last14DaysCert, activeCount);
        routineStatsRepository.save(stats);

        log.info("Routine {} 통계 갱신: 활성 {}명, 7일 {}건, 14일 {}건",
                routine.getId(), activeCount, last7DaysCert, last14DaysCert);
    }

    // 방 폭파 조건 체크
    private void checkRoutineClosure(Routine routine) {
        LocalDate today = LocalDate.now();

        RoutineStats stats = routineStatsRepository
                .findByRoutineAndDate(routine, today)
                .orElse(null);

        if (stats == null) {
            return;
        }

        // 방 폭파 조건: 활성 3명 이하 + 최근 14일 인증 0건
        if (stats.shouldCloseRoutine()) {
            routine.closeRoutine();
            log.warn("Routine {} 방 폭파: 활성 {}명, 14일 인증 {}건",
                    routine.getId(),
                    stats.getActiveParticipants(),
                    stats.getLast14DaysCertCount());
        }
    }
}