package com.example.motiday_api.domain.feed.entity;

import com.example.motiday_api.domain.common.BaseTimeEntity;
import com.example.motiday_api.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

/**
 * 댓글 엔티티
 * 피드에 대한 댓글 관리
 */
@Entity
@Table(name = "comments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Comment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long id;  // 댓글 고유 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feed_id", nullable = false)
    private Feed feed;  // 댓글이 달린 피드

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;  // 댓글 작성자

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;  // 댓글 내용

    // ========== 비즈니스 메서드 ==========

    /**
     * 댓글 내용 수정
     * 댓글 수정 API에서 호출
     * @param newContent 새 댓글 내용
     */
    public void updateContent(String newContent) {
        this.content = newContent;
    }

    /**
     * 작성자 본인인지 확인
     * 수정/삭제 권한 체크용
     * @param user 확인할 사용자
     * @return 작성자 본인이면 true
     */
    public boolean isOwner(User user) {
        return this.user.getId().equals(user.getId());
    }
}