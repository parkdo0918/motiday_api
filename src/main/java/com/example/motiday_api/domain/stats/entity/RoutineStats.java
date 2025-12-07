package com.example.motiday_api.domain.stats.entity;

import com.example.motiday_api.domain.routine.entity.routine.Routine;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 루틴 통계 엔티티
 * 클럽별 날짜 기반 통계 관리 (방 폭파 조건 확인용)
 * 매일 자정 스케줄러가 집계하여 업데이트
 */
@Entity
@Table(name = "routine_stats",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"routine_id", "date"})  // 루틴당 날짜별 1개만
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class RoutineStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stat_id")
    private Long id;  // 통계 고유 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "routine_id", nullable = false)
    private Routine routine;  // 루틴

    @Column(nullable = false)
    private LocalDate date;  // 통계 날짜

    @Column(name = "daily_certification_count", nullable = false)
    @Builder.Default
    private Integer dailyCertificationCount = 0;  // 해당 날짜 인증 수

    @Column(name = "yesterday_certification_count", nullable = false)
    @Builder.Default
    private Integer yesterdayCertificationCount = 0;  // 어제 인증 수 (추가)

    @Column(name = "active_participants", nullable = false)
    @Builder.Default
    private Integer activeParticipants = 0;  // 활성 참여자 수 (ACTIVE 상태)

    @Column(name = "last_7days_cert_count", nullable = false)
    @Builder.Default
    private Integer last7DaysCertCount = 0;  // 최근 7일 인증 수 (화면 표시용)

    @Column(name = "last_14days_cert_count", nullable = false)
    @Builder.Default
    private Integer last14DaysCertCount = 0;  // 최근 14일 인증 수 (방 폭파 조건용)

    @Column(name = "consecutive_days_below_3", nullable = false)
    @Builder.Default
    private Integer consecutiveDaysBelow3 = 0;  // 연속으로 활성 참여자 3명 미만이었던 일수

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;  // 수정 일시

    // ========== 비즈니스 메서드 ==========

    /**
     * 당일 인증 수 증가
     * Feed 생성 시마다 호출
     */
    public void increaseDailyCertification() {
        this.dailyCertificationCount++;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 통계 갱신 (스케줄러 사용)
     * 매일 자정에 최근 7일/14일 인증 수 재계산
     * @param last7Days 최근 7일 인증 수
     * @param last14Days 최근 14일 인증 수
     * @param activeCount 활성 참여자 수
     * @param yesterdayCount 어제 인증 수 (추가)
     */
    public void updateStats(int last7Days, int last14Days, int activeCount, int yesterdayCount) {
        this.last7DaysCertCount = last7Days;
        this.last14DaysCertCount = last14Days;
        this.activeParticipants = activeCount;
        this.yesterdayCertificationCount = yesterdayCount;

        // 연속 일수 업데이트: 3명 미만이면 증가, 아니면 리셋
        if (activeCount < 3) {
            this.consecutiveDaysBelow3++;
        } else {
            this.consecutiveDaysBelow3 = 0;
        }

        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 방 폭파 조건 확인
     * 1) 연속 14일 동안 활성 참여자 3명 미만 AND
     * 2) 최근 14일 인증 0건
     * @return 방 폭파 조건 충족 시 true
     */
    public boolean shouldCloseRoutine() {
        return this.consecutiveDaysBelow3 >= 14 && this.last14DaysCertCount == 0;
    }

    /**
     * 오늘 통계 생성 (정적 팩토리 메서드)
     * @param routine 루틴
     * @return RoutineStats
     */
    public static RoutineStats createToday(Routine routine) {
        return RoutineStats.builder()
                .routine(routine)
                .date(LocalDate.now())
                .dailyCertificationCount(0)
                .activeParticipants(routine.getCurrentParticipants())
                .last7DaysCertCount(0)
                .last14DaysCertCount(0)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * BaseTimeEntity 대신 수동 업데이트
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}