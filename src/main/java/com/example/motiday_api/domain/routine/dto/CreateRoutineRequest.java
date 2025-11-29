package com.example.motiday_api.domain.routine.dto;

import com.example.motiday_api.domain.routine.entity.routine.Category;
import com.example.motiday_api.domain.routine.entity.routine.Difficulty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateRoutineRequest {
    private String title;
    private String description;
    private Category category;
    private Difficulty difficulty;
    private LocalDate startDate;
    private String region;
}