package com.example.motiday_api.domain.routine.entity.routine;

import com.example.motiday_api.domain.common.BaseTimeEntity;
import com.example.motiday_api.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "routines")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Routine extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "routine_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_user_id", nullable = false)
    private User creator;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Category category;

    @Column(name = "current_participants", nullable = false)
    @Builder.Default
    private Integer currentParticipants = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Difficulty difficulty;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(length = 50)
    private String region;

    @Column(name = "max_participants", nullable = false)
    @Builder.Default
    private Integer maxParticipants = 30;  // 모든 루틴 30명 제한


    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private RoutineStatus status = RoutineStatus.ACTIVE;

    // 비즈니스 메서드
    public void increaseParticipants() {
        this.currentParticipants++;
    }

    public void decreaseParticipants() {
        if (this.currentParticipants > 0) {
            this.currentParticipants--;
        }
    }

    public void closeRoutine() {
        this.status = RoutineStatus.CLOSED;
    }

    public boolean isActive() {
        return this.status == RoutineStatus.ACTIVE;
    }

    public boolean canJoin() {
        if (!isActive()) return false;
        return currentParticipants < maxParticipants;  // 30명 미만
    }

    // Difficulty에서 값 가져오기
    public int getRequiredFrequency() {
        return this.difficulty.getRequiredFrequency();
    }

    public int getWeeklyMoti() {
        return this.difficulty.getWeeklyMoti();
    }
}