package com.example.motiday_api.domain.user.entity;

import com.example.motiday_api.domain.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"social_type", "social_id"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SocialType socialType;

    @Column(nullable = false, length = 100)
    private String socialId;

    @Column(nullable = false, length = 50)
    private String nickname;

    @Column(length = 500)
    private String profileImageUrl;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(nullable = false)
    @Builder.Default
    private Integer motiBalance = 0;

    // 환경설정 필드
    @Column(name = "allow_follow_request", nullable = false)
    @Builder.Default
    private Boolean allowFollowRequest = true;  // 팔로우 신청 허용

    @Column(name = "allow_feed_like", nullable = false)
    @Builder.Default
    private Boolean allowFeedLike = true;  // 게시글 좋아요 허용

    @Column(name = "allow_feed_comment", nullable = false)
    @Builder.Default
    private Boolean allowFeedComment = true;  // 게시글 댓글 허용

    // Refresh Token 관련 필드 추가
    @Column(length = 500)
    private String refreshToken;

    //만료시간
    @Column(name = "refresh_token_expires_at")
    private LocalDateTime refreshTokenExpiresAt;

    // 비즈니스 메서드
    //프로필 업데이트
    public void updateProfile(String nickname, String profileImageUrl, String bio) {
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.bio = bio;
    }

    //모티 추가
    public void addMoti(int amount) {
        this.motiBalance += amount;
    }

    //모티 삭제
    public void deductMoti(int amount) {
        if (this.motiBalance < amount) {
            throw new IllegalStateException("보유 모티가 부족합니다.");
        }
        this.motiBalance -= amount;
    }

    // Refresh Token 관련 메서드

    // 로그인 시 저장
    public void updateRefreshToken(String token, LocalDateTime expiresAt) {
        this.refreshToken = token;
        this.refreshTokenExpiresAt = expiresAt;
    }

    // 로그아웃 시 삭제
    public void clearRefreshToken() {
        this.refreshToken = null;
        this.refreshTokenExpiresAt = null;
    }

    // 유효성 체크
    public boolean isRefreshTokenValid() {
        if (this.refreshToken == null || this.refreshTokenExpiresAt == null) {
            return false;
        }
        return LocalDateTime.now().isBefore(this.refreshTokenExpiresAt);
    }

    // 환경설정 변경
    public void updateSettings(Boolean allowFollowRequest, Boolean allowFeedLike, Boolean allowFeedComment) {
        if (allowFollowRequest != null) {
            this.allowFollowRequest = allowFollowRequest;
        }
        if (allowFeedLike != null) {
            this.allowFeedLike = allowFeedLike;
        }
        if (allowFeedComment != null) {
            this.allowFeedComment = allowFeedComment;
        }
    }
}