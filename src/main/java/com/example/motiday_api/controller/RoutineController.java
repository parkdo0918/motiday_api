package com.example.motiday_api.controller;

import com.example.motiday_api.domain.routine.dto.CreateRoutineRequest;
import com.example.motiday_api.domain.routine.dto.RoutineParticipantResponse;
import com.example.motiday_api.domain.routine.dto.RoutineResponse;
import com.example.motiday_api.domain.routine.entity.participant.RoutineParticipant;
import com.example.motiday_api.domain.routine.entity.routine.Category;
import com.example.motiday_api.domain.routine.entity.routine.Routine;
import com.example.motiday_api.domain.routine.service.RoutineService;
import com.example.motiday_api.domain.stats.dto.RoutineStatsResponse;
import com.example.motiday_api.domain.stats.entity.RoutineStats;
import com.example.motiday_api.domain.stats.repository.RoutineStatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RoutineController {

    private final RoutineService routineService;
    private final RoutineStatsRepository routineStatsRepository;

    // 루틴 생성
    @PostMapping("/routines")
    public ResponseEntity<RoutineResponse> createRoutine(
            @AuthenticationPrincipal Long userId,
            @RequestBody CreateRoutineRequest request

    ) {
        if (userId == null) {
            throw new IllegalArgumentException("인증이 필요합니다. JWT 토큰을 확인하세요.");
        }

        Routine created = routineService.createRoutine(
                userId,
                request.getTitle(),
                request.getDescription(),
                request.getCategory(),
                request.getDifficulty(),
                request.getStartDate(),
                request.getRegion()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(RoutineResponse.from(created));
    }

    // 전체 활성 루틴 조회 (모집중 + 마감)
    @GetMapping("/routines")
    public ResponseEntity<List<RoutineResponse>> getAllRoutines(
            @RequestParam Category category  // 필수
    ) {
        List<Routine> routines = routineService.getActiveRoutines();

        // 카테고리 필터링
        routines = routines.stream()
                .filter(r -> r.getCategory() == category)
                .collect(Collectors.toList());

        List<RoutineResponse> response = routines.stream()
                .map(RoutineResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    // 모집 중인 루틴 조회
    @GetMapping("/routines/recruiting")
    public ResponseEntity<List<RoutineResponse>> getRecruitingRoutines(
            @RequestParam Category category  // 필수
    ) {
        List<Routine> routines = routineService.getRecruitingRoutines(category);
        List<RoutineResponse> response = routines.stream()
                .map(RoutineResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    // 마감된 루틴 조회
    @GetMapping("/routines/closed")
    public ResponseEntity<List<RoutineResponse>> getClosedRoutines(
            @RequestParam Category category  // 필수
    ) {
        List<Routine> routines = routineService.getClosedRoutines(category);
        List<RoutineResponse> response = routines.stream()
                .map(RoutineResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    // 내가 참여 중인 루틴
    @GetMapping("/users/{userId}/routines")
    public ResponseEntity<List<RoutineResponse>> getMyRoutines(
            @PathVariable Long userId,
            @RequestParam(required = false) Category category
    ) {
        List<Routine> routines = routineService.getMyRoutines(userId, category);
        List<RoutineResponse> response = routines.stream()
                .map(RoutineResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    // 루틴 참여
    @PostMapping("/routines/{routineId}/join")
    public ResponseEntity<RoutineParticipantResponse> joinRoutine(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long routineId
    ) {
        RoutineParticipant participant = routineService.joinRoutine(userId, routineId);
        return ResponseEntity.ok(RoutineParticipantResponse.from(participant));
    }

    // 루틴 탈퇴
    @DeleteMapping("/routines/{routineId}/withdraw")
    public ResponseEntity<Void> withdrawRoutine(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long routineId
    ) {
        routineService.withdrawRoutine(userId, routineId);
        return ResponseEntity.ok().build();
    }

    // 루틴 통계 조회
    @GetMapping("/routines/{routineId}/stats")
    public ResponseEntity<RoutineStatsResponse> getRoutineStats(@PathVariable Long routineId) {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        Routine routine = routineService.getRoutine(routineId);
        RoutineStats todayStats = routineStatsRepository
                .findByRoutineAndDate(routine, today)
                .orElse(null);

        // 통계 데이터가 없으면 기본값 반환
        RoutineStatsResponse response;
        if (todayStats == null) {
            response = RoutineStatsResponse.builder()
                    .activeParticipants(routine.getCurrentParticipants())
                    .last7DaysCertCount(0)
                    .dailyCertificationCount(0)
                    .yesterdayCertificationCount(0)
                    .build();
        } else {
            response = RoutineStatsResponse.builder()
                    .activeParticipants(todayStats.getActiveParticipants())
                    .last7DaysCertCount(todayStats.getLast7DaysCertCount())
                    .dailyCertificationCount(todayStats.getDailyCertificationCount())
                    .yesterdayCertificationCount(todayStats.getYesterdayCertificationCount())
                    .build();
        }

        return ResponseEntity.ok(response);
    }
}