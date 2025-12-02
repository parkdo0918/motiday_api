package com.example.motiday_api.controller;

import com.example.motiday_api.domain.feed.dto.CommentResponse;
import com.example.motiday_api.domain.feed.dto.CreateCommentRequest;
import com.example.motiday_api.domain.feed.dto.CreateFeedRequest;
import com.example.motiday_api.domain.feed.dto.FeedResponse;
import com.example.motiday_api.domain.feed.entity.Comment;
import com.example.motiday_api.domain.feed.entity.Feed;
import com.example.motiday_api.domain.feed.repository.CommentRepository;
import com.example.motiday_api.domain.feed.repository.LikeRepository;
import com.example.motiday_api.domain.feed.service.FeedService;
import com.example.motiday_api.domain.user.entity.User;
import com.example.motiday_api.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FeedController {

    private final FeedService feedService;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;

    // 피드 생성 (인증 업로드)
    @PostMapping("/feeds")
    public ResponseEntity<FeedResponse> createFeed(
            @AuthenticationPrincipal Long userId,
            @RequestBody CreateFeedRequest request
    ) {
        User user = userRepository.findById(userId).orElseThrow();

        Feed feed = feedService.createFeed(
                userId,
                request.getRoutineId(),
                request.getImageUrl(),
                request.getCaption(),
                request.getIsSharedToRoutine()
        );

        boolean isLiked = likeRepository.existsByFeedAndUser(feed, user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(FeedResponse.from(feed, isLiked));
    }

    // 홈 피드 조회
    @GetMapping("/feeds")
    public ResponseEntity<List<FeedResponse>> getHomeFeed(
            @AuthenticationPrincipal Long userId
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        List<Feed> feeds = feedService.getHomeFeed();
        List<FeedResponse> response = feeds.stream()
                .map(feed -> {
                    boolean isLiked = likeRepository.existsByFeedAndUser(feed, user);
                    return FeedResponse.from(feed, isLiked);
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    // 활동 게시물 조회 (루틴별)
    @GetMapping("/routines/{routineId}/feeds")
    public ResponseEntity<List<FeedResponse>> getRoutineFeed(
            @PathVariable Long routineId,
            @AuthenticationPrincipal Long userId
    ) {
        User user = userRepository.findById(userId).orElseThrow();

        List<Feed> feeds = feedService.getRoutineFeed(routineId);
        List<FeedResponse> response = feeds.stream()
                .map(feed -> {
                    boolean isLiked = likeRepository.existsByFeedAndUser(feed, user);
                    return FeedResponse.from(feed, isLiked);
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    // 사용자별 피드 조회, 내 피드 조회 포함 (프로필)
    @GetMapping("/users/{userId}/feeds")
    public ResponseEntity<List<FeedResponse>> getUserFeeds(
            @PathVariable Long userId,
            @AuthenticationPrincipal Long currentUserId
    ) {
        User currentUser = userRepository.findById(currentUserId).orElseThrow();

        List<Feed> feeds = feedService.getUserFeeds(userId);
        List<FeedResponse> response = feeds.stream()
                .map(feed -> {
                    boolean isLiked = likeRepository.existsByFeedAndUser(feed, currentUser);
                    return FeedResponse.from(feed, isLiked);
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    // 좋아요
    @PostMapping("/feeds/{feedId}/like")
    public ResponseEntity<Integer> likeFeed(
            @PathVariable Long feedId,
            @AuthenticationPrincipal Long userId
    ) {
        feedService.likeFeed(userId, feedId);

        // 업데이트된 좋아요 수 반환
        Feed feed = feedService.getFeed(feedId);
        return ResponseEntity.ok(feed.getLikeCount());
    }

    // 좋아요 취소
    @DeleteMapping("/feeds/{feedId}/like")
    public ResponseEntity<Integer> unlikeFeed(
            @PathVariable Long feedId,
            @AuthenticationPrincipal Long userId
    ) {
        feedService.unlikeFeed(userId, feedId);

        // 업데이트된 좋아요 수 반환
        Feed feed = feedService.getFeed(feedId);
        return ResponseEntity.ok(feed.getLikeCount());
    }

    // 댓글 작성
    @PostMapping("/feeds/{feedId}/comments")
    public ResponseEntity<CommentResponse> createComment(
            @PathVariable Long feedId,
            @RequestBody CreateCommentRequest request,
            @AuthenticationPrincipal Long userId
    ) {
        Comment comment = feedService.createComment(userId, feedId, request.getContent());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CommentResponse.from(comment));
    }

    // 댓글 목록 조회
    @GetMapping("/feeds/{feedId}/comments")
    public ResponseEntity<List<CommentResponse>> getComments(@PathVariable Long feedId) {
        List<Comment> comments = commentRepository.findByFeedOrderByCreatedAtAsc(
                feedService.getFeed(feedId)
        );
        List<CommentResponse> response = comments.stream()
                .map(CommentResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    // 댓글 삭제
    @DeleteMapping("/feeds/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal Long userId
    ) {
        feedService.deleteComment(userId, commentId);
        return ResponseEntity.ok().build();
    }
}