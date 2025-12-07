package com.example.motiday_api.domain.routine.service;

import com.example.motiday_api.domain.moti.entity.MotiTransaction;
import com.example.motiday_api.domain.moti.repository.MotiTransactionRepository;
import com.example.motiday_api.domain.routine.entity.certification.WeeklyCertification;
import com.example.motiday_api.domain.routine.entity.participant.RoutineParticipant;
import com.example.motiday_api.domain.routine.entity.routine.Category;
import com.example.motiday_api.domain.routine.entity.routine.Difficulty;
import com.example.motiday_api.domain.routine.entity.routine.Routine;
import com.example.motiday_api.domain.routine.repository.RoutineParticipantRepository;
import com.example.motiday_api.domain.routine.repository.RoutineRepository;
import com.example.motiday_api.domain.routine.repository.WeeklyCertificationRepository;
import com.example.motiday_api.domain.user.entity.SocialType;
import com.example.motiday_api.domain.user.entity.User;
import com.example.motiday_api.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class WeeklyCertificationServiceTest {

    @Autowired
    private WeeklyCertificationService weeklyCertificationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoutineRepository routineRepository;

    @Autowired
    private RoutineParticipantRepository participantRepository;

    @Autowired
    private WeeklyCertificationRepository weeklyCertRepository;

    @Autowired
    private MotiTransactionRepository motiTransactionRepository;

    @Test
    @DisplayName("주간 인증 완료 시 모티 지급 및 스트릭 증가")
    void testWeeklyRewardSuccess() {
        // Given: 사용자와 루틴 생성
        User user = User.builder()
                .socialType(SocialType.KAKAO)
                .socialId("test_weekly_reward_user"+ UUID.randomUUID())
                .nickname("주간보상테스트유저")
                .build();
        userRepository.save(user);

        Routine routine = Routine.builder()
                .creator(user)
                .title("주간 보상 테스트 루틴")
                .category(Category.EXERCISE)
                .difficulty(Difficulty.EASY)  // 주 2회, 2 모티
                .startDate(LocalDate.now())
                .build();
        routineRepository.save(routine);

        // 참여자 생성 (6일 전 참여 = 1주차 진행 중, 오늘이 종료일)
        RoutineParticipant participant = RoutineParticipant.builder()
                .user(user)
                .routine(routine)
                .joinedAt(LocalDateTime.now().minusDays(6))
                .build();
        participantRepository.save(participant);

        // 주간 인증 기록 생성 (2회 인증 완료)
        WeeklyCertification weeklyCert = WeeklyCertification.builder()
                .user(user)
                .routine(routine)
                .weekNumber(1)
                .weekStartDate(LocalDate.now().minusDays(6))
                .weekEndDate(LocalDate.now())  // 오늘이 주차 종료일
                .build();
        weeklyCert.increaseCertification();
        weeklyCert.increaseCertification();
        weeklyCertRepository.save(weeklyCert);

        int beforeMoti = user.getMotiBalance();

        // When: 주간 보상 처리
        weeklyCertificationService.processWeeklyRewards();

        // Then: 모티 지급 확인 (2 MOTI)
        User updatedUser = userRepository.findById(user.getId()).get();
        assertThat(updatedUser.getMotiBalance()).isEqualTo(beforeMoti + 2);

        // 스트릭 증가 확인
        RoutineParticipant updatedParticipant = participantRepository.findById(participant.getId()).get();
        assertThat(updatedParticipant.getConsecutiveSuccessWeeks()).isEqualTo(1);

        // 거래 내역 생성 확인
        List<MotiTransaction> transactions = motiTransactionRepository.findByUserOrderByCreatedAtDesc(updatedUser);
        assertThat(transactions).isNotEmpty();

        System.out.println("✅ 주간 보상 테스트 성공 - 모티 지급: 2 MOTI");
    }

    @Test
    @DisplayName("주간 목표 미달성 시 스트릭 초기화")
    void testWeeklyRewardFail() {
        // Given: 사용자와 루틴 생성
        User user = User.builder()
                .socialType(SocialType.KAKAO)
                .socialId("test_weekly_fail_user"+ UUID.randomUUID())
                .nickname("주간실패테스트유저")
                .build();
        userRepository.save(user);

        Routine routine = Routine.builder()
                .creator(user)
                .title("주간 실패 테스트 루틴")
                .category(Category.STUDY)
                .difficulty(Difficulty.STANDARD)  // 주 3회 필요
                .startDate(LocalDate.now())
                .build();
        routineRepository.save(routine);

        RoutineParticipant participant = RoutineParticipant.builder()
                .user(user)
                .routine(routine)
                .joinedAt(LocalDateTime.now().minusDays(6))
                .build();
        participant.increaseStreak();  // 스트릭 1로 설정
        participantRepository.save(participant);

        // 주간 인증 기록 생성 (1회만 인증 - 목표 미달)
        WeeklyCertification weeklyCert = WeeklyCertification.builder()
                .user(user)
                .routine(routine)
                .weekNumber(1)
                .weekStartDate(LocalDate.now().minusDays(6))
                .weekEndDate(LocalDate.now())  // 오늘이 주차 종료일
                .build();
        weeklyCert.increaseCertification();
        weeklyCertRepository.save(weeklyCert);

        int beforeMoti = user.getMotiBalance();

        // When: 주간 보상 처리
        weeklyCertificationService.processWeeklyRewards();

        // Then: 모티 지급 안됨
        User updatedUser = userRepository.findById(user.getId()).get();
        assertThat(updatedUser.getMotiBalance()).isEqualTo(beforeMoti);

        // 스트릭 초기화 확인
        RoutineParticipant updatedParticipant = participantRepository.findById(participant.getId()).get();
        assertThat(updatedParticipant.getConsecutiveSuccessWeeks()).isEqualTo(0);

        System.out.println("✅ 주간 실패 테스트 성공 - 스트릭 초기화됨");
    }

    @Test
    @DisplayName("HARD 난이도 - 주 4회 달성 시 4 MOTI 지급")
    void testWeeklyRewardHardDifficulty() {
        // Given: 사용자와 HARD 루틴 생성
        User user = User.builder()
                .socialType(SocialType.KAKAO)
                .socialId("test_hard_difficulty_user"+ UUID.randomUUID())
                .nickname("고난이도테스트유저")
                .build();
        userRepository.save(user);

        Routine routine = Routine.builder()
                .creator(user)
                .title("고난이도 테스트 루틴")
                .category(Category.READING)
                .difficulty(Difficulty.HARD)  // 주 4회, 4 모티
                .startDate(LocalDate.now())
                .build();
        routineRepository.save(routine);

        RoutineParticipant participant = RoutineParticipant.builder()
                .user(user)
                .routine(routine)
                .joinedAt(LocalDateTime.now().minusDays(6))
                .build();
        participantRepository.save(participant);

        // 주간 인증 기록 생성 (4회 인증 완료)
        WeeklyCertification weeklyCert = WeeklyCertification.builder()
                .user(user)
                .routine(routine)
                .weekNumber(1)
                .weekStartDate(LocalDate.now().minusDays(6))
                .weekEndDate(LocalDate.now())  // 오늘이 주차 종료일
                .build();
        weeklyCert.increaseCertification();
        weeklyCert.increaseCertification();
        weeklyCert.increaseCertification();
        weeklyCert.increaseCertification();
        weeklyCertRepository.save(weeklyCert);

        int beforeMoti = user.getMotiBalance();

        // When: 주간 보상 처리
        weeklyCertificationService.processWeeklyRewards();

        // Then: 4 MOTI 지급 확인
        User updatedUser = userRepository.findById(user.getId()).get();
        assertThat(updatedUser.getMotiBalance()).isEqualTo(beforeMoti + 4);

        System.out.println("✅ HARD 난이도 테스트 성공 - 모티 지급: 4 MOTI");
    }
}