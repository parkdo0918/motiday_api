package com.example.motiday_api.domain.routine.dto;

import com.example.motiday_api.domain.routine.entity.routine.Category;
import com.example.motiday_api.domain.routine.entity.routine.Difficulty;
import com.example.motiday_api.domain.routine.entity.routine.Routine;
import com.example.motiday_api.domain.routine.entity.routine.RoutineStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoutineResponse {
    private Long routineId;
    private String title;
    private String description;
    private Category category;
    private Difficulty difficulty;
    private Integer currentParticipants;
    private Integer maxParticipants;
    private LocalDate startDate;
    private String region;
    private RoutineStatus status;
    private LocalDateTime createdAt;

    public static RoutineResponse from(Routine routine) {
        return RoutineResponse.builder()
                .routineId(routine.getId())
                .title(routine.getTitle())
                .description(routine.getDescription())
                .category(routine.getCategory())
                .difficulty(routine.getDifficulty())
                .currentParticipants(routine.getCurrentParticipants())
                .maxParticipants(routine.getMaxParticipants())
                .startDate(routine.getStartDate())
                .region(routine.getRegion())
                .status(routine.getStatus())
                .createdAt(routine.getCreatedAt())
                .build();
    }
}