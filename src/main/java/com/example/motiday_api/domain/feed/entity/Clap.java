package com.example.motiday_api.domain.feed.entity;

import com.example.motiday_api.domain.common.BaseTimeEntity;
import com.example.motiday_api.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

/**
 * 박수(Clap) 엔티티
 * 루틴별 피드(활동 게시물)에 대한 사용자의 박수 기록
 * 한 사람이 같은 피드에 중복 박수 불가
 */
@Entity
@Table(name = "claps",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"feed_id", "user_id"})  // 중복 박수 방지
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Clap extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "clap_id")
    private Long id;  // 박수 고유 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feed_id", nullable = false)
    private Feed feed;  // 박수한 피드

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;  // 박수한 사용자

}