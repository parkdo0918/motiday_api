package com.example.motiday_api.domain.follow.dto;

import com.example.motiday_api.domain.follow.entity.Follow;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FollowResponse {
    private Long userId;
    private String nickname;
    private String profileImageUrl;

    public static FollowResponse fromFollower(Follow follow) {
        return FollowResponse.builder()
                .userId(follow.getFollower().getId())
                .nickname(follow.getFollower().getNickname())
                .profileImageUrl(follow.getFollower().getProfileImageUrl())
                .build();
    }

    public static FollowResponse fromFollowing(Follow follow) {
        return FollowResponse.builder()
                .userId(follow.getFollowing().getId())
                .nickname(follow.getFollowing().getNickname())
                .profileImageUrl(follow.getFollowing().getProfileImageUrl())
                .build();
    }
}