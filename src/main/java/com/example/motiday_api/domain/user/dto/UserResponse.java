package com.example.motiday_api.domain.user.dto;

import com.example.motiday_api.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private Long userId;
    private String nickname;
    private String profileImageUrl;
    private String bio;
    private Integer motiBalance;
    private Integer feedCount;
    private Integer followerCount;

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .profileImageUrl(user.getProfileImageUrl())
                .bio(user.getBio())
                .motiBalance(user.getMotiBalance())
                .build();
    }

    public static UserResponse from(User user, int feedCount, int followerCount) {
        return UserResponse.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .profileImageUrl(user.getProfileImageUrl())
                .bio(user.getBio())
                .motiBalance(user.getMotiBalance())
                .feedCount(feedCount)
                .followerCount(followerCount)
                .build();
    }
}