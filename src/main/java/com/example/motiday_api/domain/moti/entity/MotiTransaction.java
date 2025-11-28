package com.example.motiday_api.domain.moti.entity;

import com.example.motiday_api.domain.common.BaseTimeEntity;
import com.example.motiday_api.domain.user.entity.User;
import com.example.motiday_api.domain.routine.entity.routine.Routine;
import com.example.motiday_api.domain.routine.entity.certification.WeeklyCertification;
import jakarta.persistence.*;
import lombok.*;

/**
 * 모티 거래 내역 엔티티
 * 모티 적립 및 사용 이력 관리 (주간 보상 방식)
 * 모든 모티 변동 사항을 기록하여 감사 추적 가능
 */
@Entity
@Table(name = "moti_transactions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MotiTransaction extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private Long id;  // 거래 고유 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;  // 사용자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "routine_id")
    private Routine routine;  // 연관 루틴 (주간 보상 시)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "weekly_cert_id")
    private WeeklyCertification weeklyCertification;  // 연관 주간 인증 (보상 근거)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionType type;  // 거래 유형 (EARN: 적립, SPEND: 사용)

    @Column(nullable = false)
    private Integer amount;  // 모티 수량 (주간 보상: 2/3/4, 체험단: -15)

    @Column(length = 200)
    private String description;  // 거래 설명 ("운동 루틴 Week 1 달성", "체험단 신청" 등)

    // ========== 비즈니스 메서드 ==========

    /**
     * 적립 거래인지 확인
     * @return EARN이면 true
     */
    public boolean isEarn() {
        return this.type == TransactionType.EARN;
    }

    /**
     * 사용 거래인지 확인
     * @return SPEND면 true
     */
    public boolean isSpend() {
        return this.type == TransactionType.SPEND;
    }

    /**
     * 주간 보상 거래 생성 (정적 팩토리 메서드)
     * @param user 사용자
     * @param routine 루틴
     * @param weeklyCert 주간 인증 기록
     * @param amount 보상 모티
     * @return MotiTransaction
     */
    public static MotiTransaction createWeeklyReward(
            User user,
            Routine routine,
            WeeklyCertification weeklyCert,
            int amount
    ) {
        return MotiTransaction.builder()
                .user(user)
                .routine(routine)
                .weeklyCertification(weeklyCert)
                .type(TransactionType.EARN)
                .amount(amount)
                .description(String.format("%s Week %d 달성",
                        routine.getTitle(),
                        weeklyCert.getWeekNumber()))
                .build();
    }

    /**
     * 체험단 신청 거래 생성 (정적 팩토리 메서드)
     * @param user 사용자
     * @param trialName 체험단 이름
     * @return MotiTransaction
     */
    public static MotiTransaction createTrialApplication(User user, String trialName) {
        return MotiTransaction.builder()
                .user(user)
                .type(TransactionType.SPEND)
                .amount(15)
                .description("체험단 신청: " + trialName)
                .build();
    }
}