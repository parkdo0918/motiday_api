package com.example.motiday_api.domain.user.entity;

import com.example.motiday_api.domain.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

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

    // 비즈니스 메서드
    public void updateProfile(String nickname, String bio) {
        this.nickname = nickname;
        this.bio = bio;
    }

    public void addMoti(int amount) {
        this.motiBalance += amount;
    }

    public void deductMoti(int amount) {
        if (this.motiBalance < amount) {
            throw new IllegalStateException("보유 모티가 부족합니다.");
        }
        this.motiBalance -= amount;
    }
}