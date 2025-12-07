package com.example.motiday_api.domain.feed.entity;

import com.example.motiday_api.domain.common.BaseTimeEntity;
import com.example.motiday_api.domain.user.entity.User;
import com.example.motiday_api.domain.routine.entity.routine.Routine;
import jakarta.persistence.*;
import lombok.*;

/**
 * 피드(인증 게시물) 엔티티
 * 루틴 인증 사진/영상 및 캡션 관리
 * 홈 피드 및 활동 게시물에 표시
 */
@Entity
@Table(name = "feeds")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Feed extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feed_id")
    private Long id;  // 피드 고유 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;  // 작성자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "routine_id", nullable = false)
    private Routine routine;  // 연관된 루틴

    @Column(nullable = false, length = 500)
    private String imageUrl;  // 인증 이미지/영상 URL

    @Column(columnDefinition = "TEXT")
    private String caption;  // 캡션/메모

    @Column(name = "is_shared_to_routine", nullable = false)
    @Builder.Default
    private Boolean isSharedToRoutine = false;  // 활동 게시물 공유 여부 (토글)

    @Column(name = "like_count", nullable = false)
    @Builder.Default
    private Integer likeCount = 0;  // 좋아요 수

    @Column(name = "clap_count", nullable = false)
    @Builder.Default
    private Integer clapCount = 0;  // 박수 수

    @Column(name = "comment_count", nullable = false)
    @Builder.Default
    private Integer commentCount = 0;  // 댓글 수

    // ========== 비즈니스 메서드 ==========

    /**
     * 좋아요 수 증가
     * Like 엔티티 생성 시 호출
     */
    public void increaseLikeCount() {
        this.likeCount++;
    }

    /**
     * 좋아요 수 감소
     * Like 엔티티 삭제 시 호출 (좋아요 취소)
     */
    public void decreaseLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }

    /**
     * 박수 수 증가
     * Clap 엔티티 생성 시 호출
     */
    public void increaseClapCount() {
        this.clapCount++;
    }

    /**
     * 박수 수 감소
     * Clap 엔티티 삭제 시 호출 (박수 취소)
     */
    public void decreaseClapCount() {
        if (this.clapCount > 0) {
            this.clapCount--;
        }
    }

    /**
     * 댓글 수 증가
     * Comment 엔티티 생성 시 호출
     */
    public void increaseCommentCount() {
        this.commentCount++;
    }

    /**
     * 댓글 수 감소
     * Comment 엔티티 삭제 시 호출
     */
    public void decreaseCommentCount() {
        if (this.commentCount > 0) {
            this.commentCount--;
        }
    }

    /**
     * 활동 게시물 공유 설정 변경
     * 업로드 시 또는 수정 시 호출
     * @param isShared 공유 여부
     */
    public void updateSharedStatus(boolean isShared) {
        this.isSharedToRoutine = isShared;
    }

    /**
     * 캡션 수정
     * 피드 수정 시 호출
     * @param newCaption 새 캡션
     */
    public void updateCaption(String newCaption) {
        this.caption = newCaption;
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