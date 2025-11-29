package com.example.motiday_api.domain.user.dto;

import com.example.motiday_api.domain.user.entity.SocialType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequest {
    private SocialType socialType;
    private String socialId;
}