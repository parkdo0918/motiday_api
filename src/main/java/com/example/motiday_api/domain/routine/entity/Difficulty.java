package com.example.motiday_api.domain.routine.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Difficulty {
    EASY(2, 2),         // 주 2회, 2 MOTI
    STANDARD(3, 3),     // 주 3회, 3 MOTI
    HARD(4, 4);         // 주 4회, 4 MOTI

    private final int requiredFrequency;  // 주당 필요 인증 횟수
    private final int weeklyMoti;         // 주간 보상 모티
}