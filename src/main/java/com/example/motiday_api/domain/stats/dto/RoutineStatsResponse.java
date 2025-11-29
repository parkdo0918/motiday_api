package com.example.motiday_api.domain.stats.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoutineStatsResponse {
    private Integer activeParticipants;
    private Integer last7DaysCertCount;
    private Integer dailyCertificationCount;
    private Integer yesterdayCertificationCount;
}