package com.example.motiday_api.domain.follow.service;

import com.example.motiday_api.domain.follow.entity.Follow;
import com.example.motiday_api.domain.follow.repository.FollowRepository;
import com.example.motiday_api.domain.user.entity.User;
import com.example.motiday_api.domain.user.repository.UserRepository;
import com.example.motiday_api.exception.DuplicateException;
import com.example.motiday_api.exception.ForbiddenException;
import com.example.motiday_api.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    // 팔로우
    @Transactional
    public Follow follow(Long followerId, Long followingId) {
        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        User following = userRepository.findById(followingId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        // 자기 자신 팔로우 방지
        if (followerId.equals(followingId)) {
            throw new ForbiddenException("자기 자신은 팔로우할 수 없습니다.");
        }

        // 이미 팔로우 중인지 확인
        if (followRepository.existsByFollowerAndFollowing(follower, following)) {
            throw new DuplicateException("이미 팔로우 중입니다.");
        }

        Follow follow = Follow.builder()
                .follower(follower)
                .following(following)
                .build();

        return followRepository.save(follow);
    }

    // 언팔로우
    @Transactional
    public void unfollow(Long followerId, Long followingId) {
        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        User following = userRepository.findById(followingId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        Follow follow = followRepository.findByFollowerAndFollowing(follower, following)
                .orElseThrow(() -> new ForbiddenException("팔로우 중이 아닙니다."));

        followRepository.delete(follow);
    }

    // 팔로워 목록 - 유저를 팔로잉 하는사람들이니 -> 팔로워
    public List<Follow> getFollowers(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        return followRepository.findByFollowing(user);
    }

    // 팔로잉 목록
    public List<Follow> getFollowings(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        return followRepository.findByFollower(user);
    }

    // 팔로우 여부 확인
    public boolean isFollowing(Long followerId, Long followingId) {
        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        User following = userRepository.findById(followingId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        return followRepository.existsByFollowerAndFollowing(follower, following);
    }
}