package com.example.motiday_api.domain.routine.entity.participant;

public enum ParticipantStatus {
    ACTIVE,      // 정상 활동 중
    PENALTY,     // 정지 상태 (1주간)
    KICKED,      // 강퇴됨
    WITHDRAWN    // 자진 탈퇴
}