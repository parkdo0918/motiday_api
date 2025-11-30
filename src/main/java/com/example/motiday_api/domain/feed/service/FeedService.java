package com.example.motiday_api.domain.feed.service;

import com.example.motiday_api.domain.feed.entity.Comment;
import com.example.motiday_api.domain.feed.entity.Feed;
import com.example.motiday_api.domain.feed.entity.Like;
import com.example.motiday_api.domain.feed.repository.CommentRepository;
import com.example.motiday_api.domain.feed.repository.FeedRepository;
import com.example.motiday_api.domain.feed.repository.LikeRepository;
import com.example.motiday_api.domain.routine.entity.certification.WeeklyCertification;
import com.example.motiday_api.domain.routine.entity.participant.RoutineParticipant;
import com.example.motiday_api.domain.routine.entity.routine.Routine;
import com.example.motiday_api.domain.routine.repository.RoutineParticipantRepository;
import com.example.motiday_api.domain.routine.repository.RoutineRepository;
import com.example.motiday_api.domain.routine.repository.WeeklyCertificationRepository;
import com.example.motiday_api.domain.stats.entity.RoutineStats;
import com.example.motiday_api.domain.stats.repository.RoutineStatsRepository;
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
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeedService {

    private final FeedRepository feedRepository;
    private final UserRepository userRepository;
    private final RoutineRepository routineRepository;
    private final RoutineParticipantRepository participantRepository;
    private final WeeklyCertificationRepository weeklyCertRepository;
    private final RoutineStatsRepository routineStatsRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;

    // 사용자별 피드 조회 (프로필용)
    public List<Feed> getUserFeeds(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        return feedRepository.findByUserOrderByCreatedAtDesc(user);
    }

    // 피드 조회 (피드 상세 조회용, 아직은 필요없음)
    public Feed getFeed(Long feedId) {
        return feedRepository.findById(feedId)
                .orElseThrow(() -> new IllegalArgumentException("피드를 찾을 수 없습니다."));
    }

    // 피드 생성 (인증 업로드)
    @Transactional
    public Feed createFeed(Long userId, Long routineId, String imageUrl,
                           String caption, boolean isSharedToRoutine) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        Routine routine = routineRepository.findById(routineId)
                .orElseThrow(() -> new RoutineNotFoundException("루틴을 찾을 수 없습니다."));

        // 참여 중인지 확인
        RoutineParticipant participant = participantRepository.findByUserAndRoutine(user, routine)
                .orElseThrow(() -> new ForbiddenException("참여 중인 루틴이 아닙니다."));

        // 주차 계산 (참여일 기준 rolling 7 days)
        long daysSinceJoined = ChronoUnit.DAYS.between(
                participant.getJoinedAt().toLocalDate(),
                LocalDate.now()
        );
        int currentWeek = (int) (daysSinceJoined / 7) + 1;

        // 주간 인증 기록 조회 또는 생성
        WeeklyCertification weeklyCert = weeklyCertRepository
                .findByUserAndRoutineAndWeekNumber(user, routine, currentWeek)
                .orElseGet(() -> {
                    LocalDate weekStart = participant.getJoinedAt().toLocalDate()
                            .plusWeeks(currentWeek - 1);
                    LocalDate weekEnd = weekStart.plusDays(6);

                    WeeklyCertification newCert = WeeklyCertification.builder()
                            .user(user)
                            .routine(routine)
                            .weekNumber(currentWeek)
                            .weekStartDate(weekStart)
                            .weekEndDate(weekEnd)
                            .build();
                    return weeklyCertRepository.save(newCert);
                });

        // 인증 횟수 증가
        weeklyCert.increaseCertification();
        participant.increaseCertificationCount();

        // 피드 생성
        Feed feed = Feed.builder()
                .user(user)
                .routine(routine)
                .imageUrl(imageUrl)
                .caption(caption)
                .isSharedToRoutine(isSharedToRoutine)
                .build();

        Feed savedFeed = feedRepository.save(feed);

        // 루틴 통계 업데이트
        updateRoutineStats(routine);

        return savedFeed;
    }

    // 루틴 통계 업데이트 (해당 루틴을 통계에 추가)
    private void updateRoutineStats(Routine routine) {
        LocalDate today = LocalDate.now();
        RoutineStats stats = routineStatsRepository
                .findByRoutineAndDate(routine, today)
                .orElseGet(() -> RoutineStats.createToday(routine));

        stats.increaseDailyCertification();
        routineStatsRepository.save(stats);
    }

    // 홈 피드 조회
    public List<Feed> getHomeFeed() {
        return feedRepository.findAllByOrderByCreatedAtDesc();
    }

    // 활동 게시물 조회 (루틴별)
    public List<Feed> getRoutineFeed(Long routineId) {
        Routine routine = routineRepository.findById(routineId)
                .orElseThrow(() -> new RoutineNotFoundException("루틴을 찾을 수 없습니다."));

        return feedRepository.findByRoutineAndIsSharedToRoutineTrueOrderByCreatedAtDesc(routine);
    }

    // 좋아요
    @Transactional
    public void likeFeed(Long userId, Long feedId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new IllegalArgumentException("피드를 찾을 수 없습니다."));

        // 이미 좋아요했는지 확인
        if (likeRepository.existsByFeedAndUser(feed, user)) {
            throw new DuplicateException("이미 좋아요한 피드입니다.");
        }

        // 좋아요 생성
        Like like = Like.builder()
                .feed(feed)
                .user(user)
                .build();
        likeRepository.save(like);

        // 좋아요 수 증가
        feed.increaseLikeCount();
    }

    // 좋아요 취소
    @Transactional
    public void unlikeFeed(Long userId, Long feedId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new IllegalArgumentException("피드를 찾을 수 없습니다."));

        // 좋아요 찾기
        Like like = likeRepository.findByFeedAndUser(feed, user)
                .orElseThrow(() -> new ForbiddenException("좋아요하지 않은 피드입니다."));

        // 좋아요 삭제
        likeRepository.delete(like);

        // 좋아요 수 감소
        feed.decreaseLikeCount();
    }

    // 댓글 작성
    @Transactional
    public Comment createComment(Long userId, Long feedId, String content) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new IllegalArgumentException("피드를 찾을 수 없습니다."));

        // 댓글 생성
        Comment comment = Comment.builder()
                .feed(feed)
                .user(user)
                .content(content)
                .build();

        Comment savedComment = commentRepository.save(comment);

        // 댓글 수 증가
        feed.increaseCommentCount();

        return savedComment;
    }

    // 댓글 삭제
    @Transactional
    public void deleteComment(Long userId, Long commentId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));

        // 작성자 확인
        if (!comment.isOwner(user)) {
            throw new ForbiddenException("댓글 작성자만 삭제할 수 있습니다.");
        }

        Feed feed = comment.getFeed();

        // 댓글 삭제
        commentRepository.delete(comment);

        // 댓글 수 감소
        feed.decreaseCommentCount();
    }
}