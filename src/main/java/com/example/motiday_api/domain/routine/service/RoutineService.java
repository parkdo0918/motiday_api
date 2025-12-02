package com.example.motiday_api.domain.routine.service;

import com.example.motiday_api.domain.routine.entity.participant.ParticipantStatus;
import com.example.motiday_api.domain.routine.entity.participant.RoutineParticipant;
import com.example.motiday_api.domain.routine.entity.routine.Category;
import com.example.motiday_api.domain.routine.entity.routine.Difficulty;
import com.example.motiday_api.domain.routine.entity.routine.Routine;
import com.example.motiday_api.domain.routine.entity.routine.RoutineStatus;
import com.example.motiday_api.domain.routine.repository.RoutineParticipantRepository;
import com.example.motiday_api.domain.routine.repository.RoutineRepository;
import com.example.motiday_api.domain.user.entity.User;
import com.example.motiday_api.domain.user.repository.UserRepository;
import com.example.motiday_api.exception.DuplicateException;
import com.example.motiday_api.exception.ForbiddenException;
import com.example.motiday_api.exception.RoutineNotFoundException;
import com.example.motiday_api.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoutineService {

    private final RoutineRepository routineRepository;
    private final RoutineParticipantRepository participantRepository;
    private final UserRepository userRepository;

    // 루틴 조회 - 루틴 상세 페이지
    public Routine getRoutine(Long routineId) {
        return routineRepository.findById(routineId)
                .orElseThrow(() -> new RoutineNotFoundException("루틴을 찾을 수 없습니다."));
    }

    // 루틴 생성
    @Transactional
    public Routine createRoutine(Long userId, String title, String description,
                                  Category category, Difficulty difficulty,
                                  LocalDate startDate, String region) {
        User creator = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        // 같은 카테고리 루틴 참여 중인지 확인 (카테고리별 1개 제한)
        if (participantRepository.existsByUserAndRoutine_CategoryAndStatus(
                creator,
                category,
                ParticipantStatus.ACTIVE
        )) {
            throw new DuplicateException(
                    category + " 카테고리 루틴은 이미 참여 중입니다. " +
                            "하나의 카테고리에는 1개의 루틴만 참여할 수 있습니다."
            );
        }

        Routine routine = Routine.builder()
                .creator(creator)
                .title(title)
                .description(description)
                .category(category)
                .difficulty(difficulty)
                .startDate(startDate)
                .region(region)
                .build();

        Routine savedRoutine = routineRepository.save(routine);

        // 생성자를 자동으로 참여자로 추가
        RoutineParticipant participant = RoutineParticipant.builder()
                .user(creator)
                .routine(savedRoutine)
                .joinedAt(LocalDateTime.now())
                .build();
        participantRepository.save(participant);

        // 참여자 수 증가
        savedRoutine.increaseParticipants();

        return savedRoutine;
    }

    // 전체 활성 루틴 조회
    public List<Routine> getActiveRoutines() {
        return routineRepository.findByStatusOrderByCreatedAtDesc(RoutineStatus.ACTIVE);
    }

    // 모집 중인 루틴 조회 (전체 or 카테고리별)
    public List<Routine> getRecruitingRoutines(Category category) {
        return routineRepository.findRecruitingRoutines(category);
    }

    // 마감된 루틴 조회 (전체 or 카테고리별)
    public List<Routine> getClosedRoutines(Category category) {
        return routineRepository.findClosedRoutines(category);
    }

    // 내가 참여 중인 루틴 조회
    public List<Routine> getMyRoutines(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        return routineRepository.findParticipatingRoutines(user);
    }

    // 루틴 참여
    @Transactional
    public RoutineParticipant joinRoutine(Long userId, Long routineId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        Routine routine = routineRepository.findById(routineId)
                .orElseThrow(() -> new RoutineNotFoundException("루틴을 찾을 수 없습니다."));

        // 정원 확인
        if (!routine.canJoin()) {
            throw new ForbiddenException("정원이 마감되었습니다. (30/30명)");
        }

        // 기존 참여 기록 조회
        Optional<RoutineParticipant> existingParticipant =
                participantRepository.findByUserAndRoutine(user, routine);

        // 이미 활성 참여 중인지 확인
        if (existingParticipant.isPresent() && existingParticipant.get().isActive()) {
            throw new DuplicateException("이미 참여 중인 루틴입니다.");
        }

        // 재참여 가능 여부 확인 (강퇴 이력)
        if (existingParticipant.isPresent() && !existingParticipant.get().canRejoin()) {
            throw new ForbiddenException(
                    "재참여 제한 기간입니다. " +
                            existingParticipant.get().getBanUntil() + "까지 참여 불가"
            );
        }

        // 같은 카테고리 루틴 참여 중인지 확인 (카테고리별 1개 제한)
        if (participantRepository.existsByUserAndRoutine_CategoryAndStatus(
                user,
                routine.getCategory(),
                ParticipantStatus.ACTIVE
        )) {
            throw new DuplicateException(
                    routine.getCategory() + " 카테고리 루틴은 이미 참여 중입니다. " +
                            "하나의 카테고리에는 1개의 루틴만 참여할 수 있습니다."
            );
        }

        // 참여자 생성
        RoutineParticipant participant = RoutineParticipant.builder()
                .user(user)
                .routine(routine)
                .joinedAt(LocalDateTime.now())
                .build();

        // 참여자 수 증가
        routine.increaseParticipants();

        return participantRepository.save(participant);
    }

    // 루틴 탈퇴
    @Transactional
    public void withdrawRoutine(Long userId, Long routineId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        Routine routine = routineRepository.findById(routineId)
                .orElseThrow(() -> new RoutineNotFoundException("루틴을 찾을 수 없습니다."));

        RoutineParticipant participant = participantRepository.findByUserAndRoutine(user, routine)
                .orElseThrow(() -> new ForbiddenException("참여 중인 루틴이 아닙니다."));

        // 탈퇴 처리
        participant.withdraw();

        // 참여자 수 감소
        routine.decreaseParticipants();
    }
}