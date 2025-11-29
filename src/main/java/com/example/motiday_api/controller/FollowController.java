package com.example.motiday_api.controller;

import com.example.motiday_api.domain.follow.dto.FollowResponse;
import com.example.motiday_api.domain.follow.entity.Follow;
import com.example.motiday_api.domain.follow.repository.FollowRepository;
import com.example.motiday_api.domain.follow.service.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;
    private final FollowRepository followRepository;

    // 팔로우
    @PostMapping("/users/{userId}/follow")
    public ResponseEntity<Void> follow(
            @PathVariable Long userId,
            @AuthenticationPrincipal Long currentUserId
    ) {
        followService.follow(currentUserId, userId);
        return ResponseEntity.ok().build();
    }

    // 언팔로우
    @DeleteMapping("/users/{userId}/follow")
    public ResponseEntity<Void> unfollow(
            @PathVariable Long userId,
            @AuthenticationPrincipal Long currentUserId
    ) {
        followService.unfollow(currentUserId, userId);
        return ResponseEntity.ok().build();
    }

    // 팔로워 목록 (나를 팔로우하는 사람들)
    @GetMapping("/users/{userId}/followers")
    public ResponseEntity<List<FollowResponse>> getFollowers(@PathVariable Long userId) {
        List<Follow> follows = followService.getFollowers(userId);
        List<FollowResponse> response = follows.stream()
                .map(FollowResponse::fromFollower)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    // 팔로잉 목록 (내가 팔로우하는 사람들)
    @GetMapping("/users/{userId}/followings")
    public ResponseEntity<List<FollowResponse>> getFollowings(@PathVariable Long userId) {
        List<Follow> follows = followService.getFollowings(userId);
        List<FollowResponse> response = follows.stream()
                .map(FollowResponse::fromFollowing)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    // 팔로우 여부 확인
    @GetMapping("/users/{userId}/follow/status")
    public ResponseEntity<Boolean> getFollowStatus(
            @PathVariable Long userId,
            @AuthenticationPrincipal Long currentUserId
    ) {
        boolean isFollowing = followService.isFollowing(currentUserId, userId);
        return ResponseEntity.ok(isFollowing);
    }
}