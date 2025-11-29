package com.example.motiday_api.domain.feed.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateFeedRequest {
    private Long routineId;
    private String imageUrl;
    private String caption;
    private Boolean isSharedToRoutine;
}