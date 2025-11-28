package com.example.motiday_api.domain.routine.entity.certification;

import com.example.motiday_api.domain.common.BaseTimeEntity;
import com.example.motiday_api.domain.user.entity.User;
import com.example.motiday_api.domain.routine.entity.routine.Routine;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * 주간 인증 기록 엔티티
 * 참여일 기준 개인 주차별 인증 횟수 및 모티 보상 관리 (Rolling 7 days)
 */
@Entity
@Table(name = "weekly_certifications",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "routine_id", "week_number"})  // 한 사람의 특정 루틴 특정 주차는 하나만
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class WeeklyCertification extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "weekly_cert_id")
    private Long id;  // 주간 인증 기록 고유 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;  // 사용자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "routine_id", nullable = false)
    private Routine routine;  // 루틴

    @Column(name = "week_number", nullable = false)
    private Integer weekNumber;  // 참여 후 주차 (1, 2, 3...) - 개인 기준

    @Column(name = "week_start_date", nullable = false)
    private LocalDate weekStartDate;  // 해당 주차 시작일 (참여일 기준 계산)

    @Column(name = "week_end_date", nullable = false)
    private LocalDate weekEndDate;  // 해당 주차 종료일 (시작일 + 6일)

    @Column(name = "certification_count", nullable = false)
    @Builder.Default
    private Integer certificationCount = 0;  // 해당 주 인증 횟수

    @Column(name = "is_success", nullable = false)
    @Builder.Default
    private Boolean isSuccess = false;  // 주간 목표 달성 여부 (certificationCount >= requiredFrequency)

    @Column(name = "moti_earned", nullable = false)
    @Builder.Default
    private Integer motiEarned = 0;  // 해당 주에 획득한 모티 (0 or 2/3/4)

    // ========== 비즈니스 메서드 ==========

    /**
     * 인증 횟수 증가
     * Feed 생성 시 호출 (하루 1회 제한은 Feed 생성 전에 체크)
     */
    public void increaseCertification() {
        this.certificationCount++;
    }

    /**
     * 주간 목표 달성 여부 확인 및 보상 지급
     * 주차 종료 시점(week_end_date 23:59:59)에 스케줄러가 호출
     * @param requiredFrequency 난이도별 필요 인증 횟수 (2/3/4)
     * @param weeklyMoti 난이도별 주간 보상 (2/3/4)
     */
    public void completeWeek(int requiredFrequency, int weeklyMoti) {
        if (this.certificationCount >= requiredFrequency) {
            this.isSuccess = true;
            this.motiEarned = weeklyMoti;
        } else {
            this.isSuccess = false;
            this.motiEarned = 0;
        }
    }

    /**
     * 달성률 계산
     * @param requiredFrequency 난이도별 필요 인증 횟수
     * @return 달성률 (0.0 ~ 100.0)
     */
//    public double getAchievementRate(int requiredFrequency) {
//        return ((double) this.certificationCount / requiredFrequency) * 100.0;
//    }

    /**
     * 목표 달성했는지 확인
     * @param requiredFrequency 난이도별 필요 인증 횟수
     * @return 달성 여부
     */
//    public boolean isAchieved(int requiredFrequency) {
//        return this.certificationCount >= requiredFrequency;
//    }

}