package com.example.motiday_api.domain.routine.entity.participant;

import com.example.motiday_api.domain.common.BaseTimeEntity;
import com.example.motiday_api.domain.routine.entity.routine.Routine;
import com.example.motiday_api.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 루틴 참여자 엔티티
 * User와 Routine 간의 N:M 관계를 나타내는 중간 테이블
 * 참여 상태, 페널티, 스트릭 등을 관리
 */
@Entity
@Table(name = "routine_participants",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"routine_id", "user_id"})  // 한 사람이 같은 루틴에 중복 참여 방지
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class RoutineParticipant extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "participant_id")
    private Long id;  // 참여 기록 고유 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "routine_id", nullable = false)
    private Routine routine;  // 참여한 루틴

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;  // 참여한 사용자

    @Column(nullable = false)
    private LocalDateTime joinedAt;  // 참여 시작 일시 (개인 Week1 기준점)

    @Column(name = "total_certification_count", nullable = false)
    @Builder.Default
    private Integer totalCertificationCount = 0;  // 총 인증 횟수 (누적) - 프로필 누적 확인용

    @Column(name = "current_week_number", nullable = false)
    @Builder.Default
    private Integer currentWeekNumber = 1;  // 현재 주차 (개인 기준, 1주차부터 시작)

    @Column(name = "consecutive_success_weeks", nullable = false)
    @Builder.Default
    private Integer consecutiveSuccessWeeks = 0;  // 연속 성공 주차 (스트릭)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ParticipantStatus status = ParticipantStatus.ACTIVE;  // 참여 상태 (ACTIVE/PENALTY/KICKED/WITHDRAWN)

    @Column(name = "penalty_count", nullable = false)
    @Builder.Default
    private Integer penaltyCount = 0;  // 경고 누적 횟수

    @Column(name = "penalty_start_date")
    private LocalDate penaltyStartDate;  // 정지 시작 날짜

    @Column(name = "penalty_end_date")
    private LocalDate penaltyEndDate;  // 정지 종료 날짜 (시작일 + 7일)

    @Column(name = "kicked_at")
    private LocalDateTime kickedAt;  // 강퇴된 일시

    @Column(name = "ban_until")
    private LocalDate banUntil;  // 재참여 제한 종료일 (강퇴 후 1개월)

    // ========== 비즈니스 메서드 ==========

    /**
     * 인증 횟수 증가
     * Feed 생성 시 호출
     */
    public void increaseCertificationCount() {
        this.totalCertificationCount++;
    }

    /**
     * 주차 증가
     * 매주 개인 기준 주차 종료 시 호출
     */
    public void increaseWeekNumber() {
        this.currentWeekNumber++;
    }

    /**
     * 스트릭(연속 성공 주차) 증가
     * 주간 목표 달성 시 호출
     */
    public void increaseStreak() {
        this.consecutiveSuccessWeeks++;
    }

    /**
     * 스트릭(연속 성공 주차) 초기화
     * 주간 목표 실패 시 호출
     */
    public void resetStreak() {
        this.consecutiveSuccessWeeks = 0;
    }

    /**
     * 정지(페널티) 부여
     * 14일 연속 인증 기준 미달 시 호출
     * 1주간 MOTI 적립 정지
     */
    public void applyPenalty() {
        this.status = ParticipantStatus.PENALTY;
        this.penaltyCount++;
        this.penaltyStartDate = LocalDate.now();
        this.penaltyEndDate = LocalDate.now().plusWeeks(1);  // 1주 후
    }

    /**
     * 정지 해제
     * penalty_end_date 지나면 자동 호출
     */
    public void releasePenalty() {
        this.status = ParticipantStatus.ACTIVE;
        this.penaltyStartDate = null;
        this.penaltyEndDate = null;
    }

    /**
     * 강퇴 처리
     * 정지 상태에서 다시 실패 시 호출
     * 1개월간 재참여 금지
     */
    public void kick() {
        this.status = ParticipantStatus.KICKED;
        this.kickedAt = LocalDateTime.now();
        this.banUntil = LocalDate.now().plusMonths(1);  // 1개월 후
    }

    /**
     * 자진 탈퇴 처리
     * 사용자가 직접 루틴 나가기 시 호출
     */
    public void withdraw() {
        this.status = ParticipantStatus.WITHDRAWN;
    }

    /**
     * 정상 활동 중인지 확인
     * @return ACTIVE 상태이면 true
     */
    public boolean isActive() {
        return this.status == ParticipantStatus.ACTIVE;
    }

    /**
     * 정지 상태인지 확인
     * @return PENALTY 상태이면 true
     */
    public boolean isPenalty() {
        return this.status == ParticipantStatus.PENALTY;
    }

    /**
     * 재참여 가능 여부 확인
     * 강퇴된 경우 banUntil 지났는지 체크
     * @return 재참여 가능하면 true
     */
    public boolean canRejoin() {
        // 강퇴 상태 아니면 재참여 가능
        if (this.status != ParticipantStatus.KICKED) return true;
        // 강퇴 상태면 ban 기간 확인 (kick() 메서드에서 항상 1개월 설정됨)
        return LocalDate.now().isAfter(this.banUntil);
    }
}