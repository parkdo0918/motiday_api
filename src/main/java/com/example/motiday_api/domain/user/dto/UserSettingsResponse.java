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
public class UserSettingsResponse {
    private Boolean allowFollowRequest;  // 팔로우 신청 허용
    private Boolean allowFeedLike;       // 게시글 좋아요 허용
    private Boolean allowFeedComment;    // 게시글 댓글 허용

    public static UserSettingsResponse from(User user) {
        return UserSettingsResponse.builder()
                .allowFollowRequest(user.getAllowFollowRequest())
                .allowFeedLike(user.getAllowFeedLike())
                .allowFeedComment(user.getAllowFeedComment())
                .build();
    }
}