package com.example.motiday_api.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserSettingsRequest {
    private Boolean allowFollowRequest;  // 팔로우 신청 허용
    private Boolean allowFeedLike;       // 게시글 좋아요 허용
    private Boolean allowFeedComment;    // 게시글 댓글 허용
}