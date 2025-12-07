package com.example.motiday_api.domain.stats;

import com.example.motiday_api.domain.feed.entity.Feed;
import com.example.motiday_api.domain.feed.repository.FeedRepository;
import com.example.motiday_api.domain.routine.entity.participant.RoutineParticipant;
import com.example.motiday_api.domain.routine.entity.routine.Category;
import com.example.motiday_api.domain.routine.entity.routine.Difficulty;
import com.example.motiday_api.domain.routine.entity.routine.Routine;
import com.example.motiday_api.domain.routine.entity.routine.RoutineStatus;
import com.example.motiday_api.domain.routine.repository.RoutineParticipantRepository;
import com.example.motiday_api.domain.routine.repository.RoutineRepository;
import com.example.motiday_api.domain.stats.entity.RoutineStats;
import com.example.motiday_api.domain.stats.repository.RoutineStatsRepository;
import com.example.motiday_api.domain.stats.service.RoutineStatsService;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class RoutineStatsTest {

    @Autowired
    private RoutineStatsRepository routineStatsRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoutineRepository routineRepository;

    @Autowired
    private RoutineParticipantRepository participantRepository;

    @Autowired
    private FeedRepository feedRepository;

    @Autowired
    private RoutineStatsService routineStatsService;

    @Test
    @DisplayName("루틴 통계 조회 - 오늘 인증 횟수 확인")
    void testRoutineStatsQuery() {
        // Given: 사용자와 루틴 생성
        User user = User.builder()
                .socialType(SocialType.KAKAO)
                .socialId("test_stats_user"+ UUID.randomUUID())
                .nickname("통계테스트유저")
                .build();
        userRepository.save(user);

        Routine routine = Routine.builder()
                .creator(user)
                .title("통계 테스트 루틴")
                .category(Category.STUDY)
                .difficulty(Difficulty.EASY)
                .startDate(LocalDate.now())
                .build();
        routineRepository.save(routine);

              RoutineParticipant participant = RoutineParticipant.builder()
                .user(user)
                .routine(routine)
                      .joinedAt(LocalDateTime.now())
                .build();

        participantRepository.save(participant);

        // 오늘 통계 생성 및 인증 3회 추가
        RoutineStats todayStats = RoutineStats.createToday(routine);
        todayStats.increaseDailyCertification();
        todayStats.increaseDailyCertification();
        todayStats.increaseDailyCertification();
        routineStatsRepository.save(todayStats);

        // When: 통계 조회
        RoutineStats result = routineStatsRepository
                .findByRoutineAndDate(routine, LocalDate.now())
                .orElse(null);

        // Then: 인증 횟수 확인
        assertThat(result).isNotNull();
        assertThat(result.getDailyCertificationCount()).isEqualTo(3);

        System.out.println("✅ 루틴 통계 조회 테스트 성공 - 오늘 인증 횟수: " + result.getDailyCertificationCount());
    }

    @Test
    @DisplayName("루틴 통계 - 여러 날짜별 인증 횟수 누적")
    void testMultipleDaysStats() {
        // Given: 사용자와 루틴 생성
        User user = User.builder()
                .socialType(SocialType.KAKAO)
                .socialId("test_multi_stats_user"+ UUID.randomUUID())
                .nickname("다중통계테스트유저")
                .build();
        userRepository.save(user);

        Routine routine = Routine.builder()
                .creator(user)
                .title("다중 통계 테스트 루틴")
                .category(Category.EXERCISE)
                .difficulty(Difficulty.STANDARD)
                .startDate(LocalDate.now())
                .build();
        routineRepository.save(routine);

        // 오늘 통계 (5회)
        RoutineStats todayStats = RoutineStats.createToday(routine);
        for (int i = 0; i < 5; i++) {
            todayStats.increaseDailyCertification();
        }
        routineStatsRepository.save(todayStats);

        // 어제 통계 (3회)
        RoutineStats yesterdayStats = RoutineStats.builder()
                .routine(routine)
                .date(LocalDate.now().minusDays(1))
                .dailyCertificationCount(0)
                .build();
        for (int i = 0; i < 3; i++) {
            yesterdayStats.increaseDailyCertification();
        }
        routineStatsRepository.save(yesterdayStats);

        // When: 오늘 통계만 조회
        RoutineStats todayResult = routineStatsRepository
                .findByRoutineAndDate(routine, LocalDate.now())
                .orElse(null);

        // Then: 오늘 인증 횟수만 확인 (5회)
        assertThat(todayResult).isNotNull();
        assertThat(todayResult.getDailyCertificationCount()).isEqualTo(5);

        System.out.println("✅ 다중 날짜 통계 테스트 성공 - 오늘: 5회, 어제: 3회");
    }

    @Test
    @DisplayName("루틴 통계 - 통계 없을 때 기본값 0 반환")
    void testNoStatsReturnsZero() {
        // Given: 루틴만 생성 (통계 없음)
        User user = User.builder()
                .socialType(SocialType.KAKAO)
                .socialId("test_no_stats_user"+ UUID.randomUUID())
                .nickname("통계없음테스트유저")
                .build();
        userRepository.save(user);

        Routine routine = Routine.builder()
                .creator(user)
                .title("통계 없음 테스트 루틴")
                .category(Category.READING)
                .difficulty(Difficulty.HARD)
                .startDate(LocalDate.now())
                .build();
        routineRepository.save(routine);

        // When: 통계 조회
        RoutineStats result = routineStatsRepository
                .findByRoutineAndDate(routine, LocalDate.now())
                .orElse(null);

        // Then: null 반환 (컨트롤러에서 0으로 처리)
        assertThat(result).isNull();

        System.out.println("✅ 통계 없음 테스트 성공 - null 반환 (컨트롤러에서 기본값 0 처리)");
    }

    @Test
    @DisplayName("루틴 통계 - 7일/14일 인증 수 집계")
    void test7And14DaysCertificationCount() throws Exception {
        // Given: 사용자와 루틴 생성
        User user = User.builder()
                .socialType(SocialType.KAKAO)
                .socialId("test_7_14days_user" + UUID.randomUUID())
                .nickname("7일14일테스트유저")
                .build();
        userRepository.save(user);

        Routine routine = Routine.builder()
                .creator(user)
                .title("7일/14일 집계 테스트 루틴")
                .category(Category.STUDY)
                .difficulty(Difficulty.EASY)
                .startDate(LocalDate.now().minusDays(20))
                .build();
        routineRepository.save(routine);

        RoutineParticipant participant = RoutineParticipant.builder()
                .user(user)
                .routine(routine)
                .joinedAt(LocalDateTime.now().minusDays(20))
                .build();
        participantRepository.save(participant);

        // Feed 생성 (과거 날짜로 createdAt 설정)
        // 5일 전: 2개
        createFeedWithCustomDate(user, routine, LocalDateTime.now().minusDays(5));
        createFeedWithCustomDate(user, routine, LocalDateTime.now().minusDays(5));

        // 10일 전: 3개 (14일 범위에만 포함)
        createFeedWithCustomDate(user, routine, LocalDateTime.now().minusDays(10));
        createFeedWithCustomDate(user, routine, LocalDateTime.now().minusDays(10));
        createFeedWithCustomDate(user, routine, LocalDateTime.now().minusDays(10));

        // When: 통계 업데이트 (스케줄러 호출)
        routineStatsService.updateDailyStats();

        // Then: 7일/14일 인증 수 확인
        RoutineStats stats = routineStatsRepository
                .findByRoutineAndDate(routine, LocalDate.now())
                .orElse(null);

        assertThat(stats).isNotNull();
        assertThat(stats.getLast7DaysCertCount()).isEqualTo(2);  // 5일 전 2개
        assertThat(stats.getLast14DaysCertCount()).isEqualTo(5);  // 5일 전 2개 + 10일 전 3개

        System.out.println("✅ 7일/14일 인증 수 테스트 성공 - 7일: " + stats.getLast7DaysCertCount()
                + ", 14일: " + stats.getLast14DaysCertCount());
    }

    @Test
    @DisplayName("루틴 통계 - 어제 인증 수 집계")
    void testYesterdayCertificationCount() throws Exception {
        // Given: 사용자와 루틴 생성
        User user = User.builder()
                .socialType(SocialType.KAKAO)
                .socialId("test_yesterday_user" + UUID.randomUUID())
                .nickname("어제인증테스트유저")
                .build();
        userRepository.save(user);

        Routine routine = Routine.builder()
                .creator(user)
                .title("어제 인증 수 테스트 루틴")
                .category(Category.EXERCISE)
                .difficulty(Difficulty.STANDARD)
                .startDate(LocalDate.now().minusDays(5))
                .build();
        routineRepository.save(routine);

        RoutineParticipant participant = RoutineParticipant.builder()
                .user(user)
                .routine(routine)
                .joinedAt(LocalDateTime.now().minusDays(5))
                .build();
        participantRepository.save(participant);

        // 어제 Feed 4개 생성
        LocalDateTime yesterday = LocalDate.now().minusDays(1).atTime(10, 0);
        for (int i = 0; i < 4; i++) {
            createFeedWithCustomDate(user, routine, yesterday.plusHours(i));
        }

        // 오늘 Feed 2개 생성 (어제 카운트에 포함되지 않아야 함)
        createFeedWithCustomDate(user, routine, LocalDateTime.now().minusHours(2));
        createFeedWithCustomDate(user, routine, LocalDateTime.now().minusHours(1));

        // When: 통계 업데이트
        routineStatsService.updateDailyStats();

        // Then: 어제 인증 수 확인
        RoutineStats stats = routineStatsRepository
                .findByRoutineAndDate(routine, LocalDate.now())
                .orElse(null);

        assertThat(stats).isNotNull();
        assertThat(stats.getYesterdayCertificationCount()).isEqualTo(4);

        System.out.println("✅ 어제 인증 수 테스트 성공 - 어제: " + stats.getYesterdayCertificationCount() + "건");
    }

    @Test
    @DisplayName("루틴 통계 - 활성 참여자 수 집계")
    void testActiveParticipantsCount() {
        // Given: 사용자와 루틴 생성
        User creator = User.builder()
                .socialType(SocialType.KAKAO)
                .socialId("test_active_creator" + UUID.randomUUID())
                .nickname("활성참여자생성자")
                .build();
        userRepository.save(creator);

        Routine routine = Routine.builder()
                .creator(creator)
                .title("활성 참여자 수 테스트 루틴")
                .category(Category.READING)
                .difficulty(Difficulty.HARD)
                .startDate(LocalDate.now())
                .build();
        routineRepository.save(routine);

        // 활성 참여자 5명 생성
        for (int i = 0; i < 5; i++) {
            User user = User.builder()
                    .socialType(SocialType.KAKAO)
                    .socialId("active_user_" + i + "_" + UUID.randomUUID())
                    .nickname("활성유저" + i)
                    .build();
            userRepository.save(user);

            RoutineParticipant participant = RoutineParticipant.builder()
                    .user(user)
                    .routine(routine)
                    .joinedAt(LocalDateTime.now())
                    .build();
            participantRepository.save(participant);
        }

        // 비활성 참여자 2명 생성
        for (int i = 0; i < 2; i++) {
            User user = User.builder()
                    .socialType(SocialType.KAKAO)
                    .socialId("inactive_user_" + i + "_" + UUID.randomUUID())
                    .nickname("비활성유저" + i)
                    .build();
            userRepository.save(user);

            RoutineParticipant participant = RoutineParticipant.builder()
                    .user(user)
                    .routine(routine)
                    .joinedAt(LocalDateTime.now())
                    .build();
            participant.withdraw();  // 비활성화
            participantRepository.save(participant);
        }

        // When: 통계 업데이트
        routineStatsService.updateDailyStats();

        // Then: 활성 참여자 수 확인 (5명만 카운트)
        RoutineStats stats = routineStatsRepository
                .findByRoutineAndDate(routine, LocalDate.now())
                .orElse(null);

        assertThat(stats).isNotNull();
        assertThat(stats.getActiveParticipants()).isEqualTo(5);

        System.out.println("✅ 활성 참여자 수 테스트 성공 - 활성: " + stats.getActiveParticipants() + "명");
    }

    @Test
    @DisplayName("방 폭파 조건 충족 - 14일 연속 3명 미만 && 14일 인증 0건")
    void testRoutineClosureConditionMet() throws Exception {
        // Given: 사용자와 루틴 생성
        User creator = User.builder()
                .socialType(SocialType.KAKAO)
                .socialId("test_closure_creator" + UUID.randomUUID())
                .nickname("폭파테스트생성자")
                .build();
        userRepository.save(creator);

        Routine routine = Routine.builder()
                .creator(creator)
                .title("방 폭파 테스트 루틴")
                .category(Category.EXERCISE)
                .difficulty(Difficulty.EASY)
                .startDate(LocalDate.now().minusDays(20))
                .build();
        routineRepository.save(routine);

        // 활성 참여자 2명 생성 (3명 미만)
        for (int i = 0; i < 2; i++) {
            User user = User.builder()
                    .socialType(SocialType.KAKAO)
                    .socialId("closure_user_" + i + "_" + UUID.randomUUID())
                    .nickname("폭파유저" + i)
                    .build();
            userRepository.save(user);

            RoutineParticipant participant = RoutineParticipant.builder()
                    .user(user)
                    .routine(routine)
                    .joinedAt(LocalDateTime.now())
                    .build();
            participantRepository.save(participant);
        }

        // 14일 인증 0건 (Feed 생성 안 함)

        // 14일 연속 통계 시뮬레이션: consecutiveDaysBelow3 = 14로 설정
        RoutineStats stats = RoutineStats.createToday(routine);
        stats.updateStats(0, 0, 2, 0);  // 초기화: 활성 2명

        // 14일 연속 3명 미만 시뮬레이션 (13번 더 호출)
        for (int i = 0; i < 13; i++) {
            stats.updateStats(0, 0, 2, 0);
        }
        routineStatsRepository.save(stats);

        // When: 방 폭파 체크
        routineStatsService.updateDailyStats();

        // Then: 루틴이 CLOSED 상태로 변경
        Routine updatedRoutine = routineRepository.findById(routine.getId()).orElse(null);
        assertThat(updatedRoutine).isNotNull();
        assertThat(updatedRoutine.getStatus()).isEqualTo(RoutineStatus.CLOSED);

        System.out.println("✅ 방 폭파 조건 충족 테스트 성공 - 루틴 상태: " + updatedRoutine.getStatus());
    }

    @Test
    @DisplayName("방 폭파 조건 미충족 - 활성 4명 (조건 불충족)")
    void testRoutineClosureConditionNotMet_ActiveUsers() {
        // Given: 사용자와 루틴 생성
        User creator = User.builder()
                .socialType(SocialType.KAKAO)
                .socialId("test_no_closure_creator" + UUID.randomUUID())
                .nickname("폭파안됨생성자")
                .build();
        userRepository.save(creator);

        Routine routine = Routine.builder()
                .creator(creator)
                .title("방 폭파 안됨 테스트 루틴")
                .category(Category.STUDY)
                .difficulty(Difficulty.STANDARD)
                .startDate(LocalDate.now().minusDays(20))
                .build();
        routineRepository.save(routine);

        // 활성 참여자 4명 생성 (3명 초과 -> 폭파 조건 미충족)
        for (int i = 0; i < 4; i++) {
            User user = User.builder()
                    .socialType(SocialType.KAKAO)
                    .socialId("no_closure_user_" + i + "_" + UUID.randomUUID())
                    .nickname("폭파안됨유저" + i)
                    .build();
            userRepository.save(user);

            RoutineParticipant participant = RoutineParticipant.builder()
                    .user(user)
                    .routine(routine)
                    .joinedAt(LocalDateTime.now())
                    .build();
            participantRepository.save(participant);
        }

        // 14일 인증 0건

        // When: 통계 업데이트
        routineStatsService.updateDailyStats();

        // Then: 루틴이 ACTIVE 상태 유지
        Routine updatedRoutine = routineRepository.findById(routine.getId()).orElse(null);
        assertThat(updatedRoutine).isNotNull();
        assertThat(updatedRoutine.getStatus()).isEqualTo(RoutineStatus.ACTIVE);

        System.out.println("✅ 방 폭파 조건 미충족 테스트 성공 (활성 4명) - 루틴 상태: " + updatedRoutine.getStatus());
    }

    @Test
    @DisplayName("방 폭파 조건 미충족 - 14일 연속 3명 미만이지만 14일 인증 1건 이상")
    void testRoutineClosureConditionNotMet_HasCertification() throws Exception {
        // Given: 사용자와 루틴 생성
        User creator = User.builder()
                .socialType(SocialType.KAKAO)
                .socialId("test_has_cert_creator" + UUID.randomUUID())
                .nickname("인증있음생성자")
                .build();
        userRepository.save(creator);

        Routine routine = Routine.builder()
                .creator(creator)
                .title("인증 있음 테스트 루틴")
                .category(Category.READING)
                .difficulty(Difficulty.HARD)
                .startDate(LocalDate.now().minusDays(20))
                .build();
        routineRepository.save(routine);

        // 활성 참여자 1명 (3명 미만)
        User user = User.builder()
                .socialType(SocialType.KAKAO)
                .socialId("has_cert_user" + UUID.randomUUID())
                .nickname("인증있음유저")
                .build();
        userRepository.save(user);

        RoutineParticipant participant = RoutineParticipant.builder()
                .user(user)
                .routine(routine)
                .joinedAt(LocalDateTime.now().minusDays(20))
                .build();
        participantRepository.save(participant);

        // 10일 전에 인증 1건 (14일 범위 내)
        createFeedWithCustomDate(user, routine, LocalDateTime.now().minusDays(10));

        // 14일 연속 3명 미만 시뮬레이션 (하지만 인증은 1건 있음)
        RoutineStats stats = RoutineStats.createToday(routine);
        for (int i = 0; i < 14; i++) {
            stats.updateStats(1, 1, 1, 0);  // 14일 인증 1건, 활성 1명
        }
        routineStatsRepository.save(stats);

        // When: 방 폭파 체크
        routineStatsService.updateDailyStats();

        // Then: 루틴이 ACTIVE 상태 유지 (인증 1건 있으므로 폭파 조건 미충족)
        Routine updatedRoutine = routineRepository.findById(routine.getId()).orElse(null);
        assertThat(updatedRoutine).isNotNull();
        assertThat(updatedRoutine.getStatus()).isEqualTo(RoutineStatus.ACTIVE);

        System.out.println("✅ 방 폭파 조건 미충족 테스트 성공 (14일 인증 1건) - 루틴 상태: " + updatedRoutine.getStatus());
    }

    // Helper: 과거 날짜로 Feed 생성
    private void createFeedWithCustomDate(User user, Routine routine, LocalDateTime createdAt) throws Exception {
        Feed feed = Feed.builder()
                .user(user)
                .routine(routine)
                .imageUrl("test_image_" + UUID.randomUUID() + ".jpg")
                .caption("테스트 인증")
                .build();
        feedRepository.save(feed);

        // Reflection으로 createdAt 설정
        var createdAtField = feed.getClass().getSuperclass().getDeclaredField("createdAt");
        createdAtField.setAccessible(true);
        createdAtField.set(feed, createdAt);
        feedRepository.save(feed);
    }
}