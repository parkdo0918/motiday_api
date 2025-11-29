package com.example.motiday_api.domain.user.service;

import com.example.motiday_api.domain.user.entity.SocialType;
import com.example.motiday_api.domain.user.entity.User;
import com.example.motiday_api.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    // 소셜 로그인 또는 회원가입
    @Transactional
    public User loginOrRegister(SocialType socialType, String socialId, String nickname) {
        return userRepository.findBySocialTypeAndSocialId(socialType, socialId)
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .socialType(socialType)
                            .socialId(socialId)
                            .nickname(nickname)
                            .build();
                    return userRepository.save(newUser);
                });
    }

    public User getUser(Long
                                userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }
    // 닉네임 중복 체크
    public boolean isNicknameAvailable(String nickname) {
        return !userRepository.existsByNickname(nickname);
    }

    // 프로필 수정
    @Transactional
    public User updateProfile(Long userId, String nickname, String profileImageUrl , String bio) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        user.updateProfile(nickname, profileImageUrl, bio);
        return user;
    }

    // 모티 적립
    @Transactional
    public void addMoti(Long userId, int amount) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        user.addMoti(amount);
    }

    // 모티 차감
    @Transactional
    public void deductMoti(Long userId, int amount) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        user.deductMoti(amount);
    }
}