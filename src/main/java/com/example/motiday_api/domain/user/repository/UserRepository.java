package com.example.motiday_api.domain.user.repository;

import com.example.motiday_api.domain.user.entity.SocialType;
import com.example.motiday_api.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    //로그인
    Optional<User> findBySocialTypeAndSocialId(SocialType socialType, String socialId);

    // 닉네임 중복 체크
    boolean existsByNickname(String nickname);

    //refreshToken으로 사용자 조회
    Optional<User> findByRefreshToken(String refreshToken);
}