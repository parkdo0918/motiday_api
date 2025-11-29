package com.example.motiday_api.domain.routine.dto;

import com.example.motiday_api.domain.routine.entity.participant.RoutineParticipant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoutineParticipantResponse {
    private Long participantId;
    private Long userId;
    private Long routineId;
    private Integer totalCertificationCount;
    private Integer currentWeekNumber;
    private Integer consecutiveSuccessWeeks;
    private LocalDateTime joinedAt;

    public static RoutineParticipantResponse from(RoutineParticipant participant) {
        return RoutineParticipantResponse.builder()
                .participantId(participant.getId())
                .userId(participant.getUser().getId())
                .routineId(participant.getRoutine().getId())
                .totalCertificationCount(participant.getTotalCertificationCount())
                .currentWeekNumber(participant.getCurrentWeekNumber())
                .consecutiveSuccessWeeks(participant.getConsecutiveSuccessWeeks())
                .joinedAt(participant.getJoinedAt())
                .build();
    }
}