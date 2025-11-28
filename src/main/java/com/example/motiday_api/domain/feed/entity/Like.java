package com.example.motiday_api.domain.feed.entity;

import com.example.motiday_api.domain.common.BaseTimeEntity;
import com.example.motiday_api.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

/**
 * 좋아요 엔티티
 * 피드에 대한 사용자의 좋아요 기록
 * 한 사람이 같은 피드에 중복 좋아요 불가
 */
@Entity
@Table(name = "likes",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"feed_id", "user_id"})  // 중복 좋아요 방지
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Like extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "like_id")
    private Long id;  // 좋아요 고유 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feed_id", nullable = false)
    private Feed feed;  // 좋아요한 피드

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;  // 좋아요한 사용자

}