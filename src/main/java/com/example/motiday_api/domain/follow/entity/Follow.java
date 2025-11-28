package com.example.motiday_api.domain.follow.entity;

import com.example.motiday_api.domain.common.BaseTimeEntity;
import com.example.motiday_api.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

/**
 * 팔로우 엔티티
 * 사용자 간의 팔로우 관계 관리 (단방향)
 * A가 B를 팔로우: follower=A, following=B
 */
@Entity
@Table(name = "follows",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"follower_user_id", "following_user_id"})  // 중복 팔로우 방지
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Follow extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "follow_id")
    private Long id;  // 팔로우 관계 고유 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follower_user_id", nullable = false)
    private User follower;  // 팔로우하는 사람 (A)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "following_user_id", nullable = false)
    private User following;  // 팔로우 당하는 사람 (B)

    // ========== 비즈니스 메서드 ==========

    /**
     * 자기 자신을 팔로우하는지 확인
     * 팔로우 생성 전 검증용
     * @return 자기 자신이면 true
     */
    public boolean isSelfFollow() {
        return this.follower.getId().equals(this.following.getId());
    }
}