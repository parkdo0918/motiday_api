package com.example.motiday_api.domain.user.service;

import com.example.motiday_api.domain.feed.repository.FeedRepository;
import com.example.motiday_api.domain.follow.repository.FollowRepository;
import com.example.motiday_api.domain.user.dto.UserResponse;
import com.example.motiday_api.domain.user.entity.SocialType;
import com.example.motiday_api.domain.user.entity.User;
import com.example.motiday_api.domain.user.repository.UserRepository;
import com.example.motiday_api.exception.UnauthorizedException;
import com.example.motiday_api.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final FeedRepository feedRepository;
    private final FollowRepository followRepository;

    private String generateRandomNickname() {
        String[] names = {"열일하는모티", "꾸준한모티", "갓생사는모티", "집중하는모티"};

        Random random = new Random();
        String name = names[random.nextInt(names.length)];
        String randomNum = String.format("%04d", random.nextInt(10000));  // 0000~9999

        String nickname = name + randomNum;  // 예: 갓생사는모티1234

        // 중복 체크
        while (userRepository.existsByNickname(nickname)) {
            randomNum = String.format("%04d", random.nextInt(10000));
            nickname = name + randomNum;
        }

        return nickname;
    }

    // 소셜 로그인 또는 회원가입
    @Transactional
    public User loginOrRegister(SocialType socialType, String socialId) {
        return userRepository.findBySocialTypeAndSocialId(socialType, socialId) // 로그인
                .orElseGet(() -> { // 정보 없으면 회원가입

                    String randomnickname = generateRandomNickname();

                    User newUser = User.builder()
                            .socialType(socialType)
                            .socialId(socialId)
                            .nickname(randomnickname)
                            .build();
                    return userRepository.save(newUser);
                });
    }

    public User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));
    }

    // 프로필 조회 (feedCount, followerCount 포함)
    public UserResponse getUserProfile(Long userId) {
        User user = getUser(userId);
        int feedCount = feedRepository.countByUser(user);
        int followerCount = followRepository.countByFollowing(user);
        return UserResponse.from(user, feedCount, followerCount);
    }

    // Refresh Token DB 저장
    @Transactional
    public void updateRefreshToken(Long userId, String refreshToken) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        user.updateRefreshToken(refreshToken, LocalDateTime.now().plusWeeks(2));
    }

    // Refresh Token으로 Access Token 재발급
    @Transactional
    public User refreshAccessToken(String refreshToken) {
        // 1. Refresh Token으로 사용자 조회
        User user = userRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new UnauthorizedException("유효하지 않은 Refresh Token입니다."));

        // 2. Refresh Token 만료 확인
        if (user.getRefreshTokenExpiresAt().isBefore(LocalDateTime.now())) {
            throw new UnauthorizedException("Refresh Token이 만료되었습니다. 다시 로그인해주세요.");
        }

        return user;
    }

    // 닉네임 중복 체크
    public boolean isNicknameAvailable(String nickname) {
        return !userRepository.existsByNickname(nickname);
    }

    // 프로필 수정
    @Transactional
    public User updateProfile(Long userId, String nickname, String profileImageUrl , String bio) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        user.updateProfile(nickname, profileImageUrl, bio);
        return user;
    }

    // 모티 적립
    @Transactional
    public void addMoti(Long userId, int amount) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        user.addMoti(amount);
    }

    // 모티 차감
    @Transactional
    public void deductMoti(Long userId, int amount) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        user.deductMoti(amount);
    }


}