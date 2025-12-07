package com.example.motiday_api.domain.feed.dto;

import com.example.motiday_api.domain.feed.entity.Feed;
import com.example.motiday_api.domain.routine.entity.routine.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedResponse {
    private Long feedId;
    private Long userId;
    private String userNickname;
    private String userProfileImage;
    private Long routineId;
    private String routineTitle;
    private Category routineCategory;
    private String imageUrl;
    private String caption;
    private Integer likeCount;
    private Integer clapCount;
    private Integer commentCount;
    private Boolean isLikedByMe;
    private Boolean isClappedByMe;
    private LocalDateTime createdAt;

    public static FeedResponse from(Feed feed, boolean isLikedByMe, boolean isClappedByMe) {
        return FeedResponse.builder()
                .feedId(feed.getId())
                .userId(feed.getUser().getId())
                .userNickname(feed.getUser().getNickname())
                .userProfileImage(feed.getUser().getProfileImageUrl())
                .routineId(feed.getRoutine().getId())
                .routineTitle(feed.getRoutine().getTitle())
                .routineCategory(feed.getRoutine().getCategory())
                .imageUrl(feed.getImageUrl())
                .caption(feed.getCaption())
                .likeCount(feed.getLikeCount())
                .clapCount(feed.getClapCount())
                .commentCount(feed.getCommentCount())
                .isLikedByMe(isLikedByMe)
                .isClappedByMe(isClappedByMe)
                .createdAt(feed.getCreatedAt())
                .build();
    }
}