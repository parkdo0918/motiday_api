package com.example.motiday_api.controller;

import com.example.motiday_api.domain.user.dto.*;
import com.example.motiday_api.domain.user.entity.User;
import com.example.motiday_api.domain.user.service.UserService;
import com.example.motiday_api.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    // 소셜 로그인
    @PostMapping("/auth/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        User user = userService.loginOrRegister(
                request.getSocialType(),
                request.getSocialId()
        );

        // JWT 토큰 생성
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        // Refresh Token DB 저장
        user.updateRefreshToken(refreshToken, LocalDateTime.now().plusWeeks(2));

        LoginResponse response = LoginResponse.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .accessToken(accessToken)  // 토큰 전달
                .refreshToken(refreshToken)
                .build();

        return ResponseEntity.ok(response);
    }

    // 프로필 조회
    @GetMapping("/users/{userId}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long userId) {
        User user = userService.getUser(userId);
        return ResponseEntity.ok(UserResponse.from(user));
    }

    // 프로필 수정
    @PutMapping("/users/{userId}")
    public ResponseEntity<UserResponse> updateProfile(
            @PathVariable Long userId,
            @RequestBody UpdateProfileRequest request
    ) {
        User user = userService.updateProfile(
                userId,
                request.getNickname(),
                request.getProfileImageUrl(),
                request.getBio()
        );
        return ResponseEntity.ok(UserResponse.from(user));
    }

    // 닉네임 중복 체크
    @GetMapping("/users/check-nickname")
    public ResponseEntity<Boolean> checkNickname(@RequestParam String nickname) {
        boolean available = userService.isNicknameAvailable(nickname);
        return ResponseEntity.ok(available);
    }

    // Refresh Token으로 Access Token 재발급
    @PostMapping("/auth/refresh")
    public ResponseEntity<RefreshTokenResponse> refreshToken(@RequestBody RefreshTokenRequest request) {
        // 1. Refresh Token 검증
        if (!jwtTokenProvider.validateToken(request.getRefreshToken())) {
            return ResponseEntity.status(401).build();
        }

        // 2. 사용자 조회
        User user = userService.refreshAccessToken(request.getRefreshToken());

        // 3. 새 Access Token 생성
        String newAccessToken = jwtTokenProvider.generateAccessToken(user.getId());

        // 4. 새 Refresh Token 생성 (선택: 갱신할지 말지)
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user.getId());
        user.updateRefreshToken(newRefreshToken, LocalDateTime.now().plusWeeks(2));

        RefreshTokenResponse response = RefreshTokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();

        return ResponseEntity.ok(response);
    }
}