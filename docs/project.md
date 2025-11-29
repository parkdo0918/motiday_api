# Motiday 프로젝트 문서

## 📌 프로젝트 개요

**Motiday(모티데이)**는 루틴 인증과 상호 동기부여를 결합한 웰니스 커뮤니티 플랫폼입니다.

### 핵심 기능
- 🏃 루틴 클럽: 운동/공부/독서 카테고리별 클럽 생성 및 참여
- 📸 주간 인증: 매주 일정 횟수 이상 인증 업로드
- 💰 보상 시스템: 주간 목표 달성 시 MOTI 지급
- 🏪 스토어: 제휴 영양제 정보 제공
- 👥 소셜 기능: 팔로우, 좋아요, 댓글

---

## 🛠 기술 스택

- **Backend**: Spring Boot 3.4.0, Java 21
- **Database**: MySQL 8.0
- **ORM**: JPA (Hibernate)
- **인증**: JWT (Access Token + Refresh Token)
- **스케줄러**: Spring Scheduler
- **빌드**: Gradle

---

## 📂 프로젝트 구조

```
src/main/java/com/example/motiday_api/
├── domain/
│   ├── common/
│   │   └── BaseTimeEntity.java           # 생성일/수정일 공통 엔티티
│   ├── user/
│   │   ├── entity/
│   │   │   ├── User.java                 # 사용자 엔티티
│   │   │   └── SocialType.java           # 소셜 로그인 타입 (KAKAO, GOOGLE, NAVER)
│   │   ├── repository/
│   │   │   └── UserRepository.java
│   │   ├── service/
│   │   │   └── UserService.java
│   │   └── dto/
│   │       ├── LoginRequest.java
│   │       ├── LoginResponse.java
│   │       ├── UpdateProfileRequest.java
│   │       └── UserResponse.java
│   ├── routine/
│   │   ├── entity/
│   │   │   ├── routine/
│   │   │   │   ├── Routine.java          # 루틴 (클럽)
│   │   │   │   ├── Category.java         # EXERCISE, STUDY, READING
│   │   │   │   ├── Difficulty.java       # EASY(2회/주), STANDARD(3회/주), HARD(4회/주)
│   │   │   │   └── RoutineStatus.java    # ACTIVE, CLOSED
│   │   │   ├── participant/
│   │   │   │   ├── RoutineParticipant.java  # 참여자
│   │   │   │   └── ParticipantStatus.java   # ACTIVE, PENALTY, KICKED, WITHDRAWN
│   │   │   └── certification/
│   │   │       └── WeeklyCertification.java # 주간 인증 기록
│   │   ├── repository/
│   │   │   ├── RoutineRepository.java
│   │   │   ├── RoutineParticipantRepository.java
│   │   │   └── WeeklyCertificationRepository.java
│   │   ├── service/
│   │   │   ├── RoutineService.java
│   │   │   ├── WeeklyCertificationService.java  # 주간 보상 스케줄러
│   │   │   └── PenaltyService.java              # 페널티/강퇴 스케줄러
│   │   └── dto/
│   │       ├── CreateRoutineRequest.java
│   │       ├── RoutineResponse.java
│   │       └── RoutineParticipantResponse.java
│   ├── stats/
│   │   ├── entity/
│   │   │   └── RoutineStats.java         # 루틴 통계 (방 폭파 체크용)
│   │   ├── repository/
│   │   │   └── RoutineStatsRepository.java
│   │   ├── service/
│   │   │   └── RoutineStatsService.java  # 통계 갱신 및 방 폭파 스케줄러
│   │   └── dto/
│   │       └── RoutineStatsResponse.java
│   ├── feed/
│   │   ├── entity/
│   │   │   ├── Feed.java                 # 인증 피드
│   │   │   ├── Like.java                 # 좋아요
│   │   │   └── Comment.java              # 댓글
│   │   ├── repository/
│   │   │   ├── FeedRepository.java
│   │   │   ├── LikeRepository.java
│   │   │   └── CommentRepository.java
│   │   ├── service/
│   │   │   └── FeedService.java
│   │   └── dto/
│   │       ├── CreateFeedRequest.java
│   │       ├── FeedResponse.java
│   │       ├── CreateCommentRequest.java
│   │       └── CommentResponse.java
│   ├── follow/
│   │   ├── entity/
│   │   │   └── Follow.java               # 팔로우 관계
│   │   ├── repository/
│   │   │   └── FollowRepository.java
│   │   ├── service/
│   │   │   └── FollowService.java
│   │   └── dto/
│   │       └── FollowResponse.java
│   ├── moti/
│   │   ├── entity/
│   │   │   ├── MotiTransaction.java      # 모티 거래 내역
│   │   │   └── TransactionType.java      # EARN, SPEND
│   │   ├── repository/
│   │   │   └── MotiTransactionRepository.java
│   │   ├── service/
│   │   │   └── MotiTransactionService.java
│   │   └── dto/
│   │       └── MotiTransactionResponse.java
│   └── product/
│       ├── entity/
│       │   └── Product.java              # 제휴 상품
│       ├── repository/
│       │   └── ProductRepository.java
│       ├── service/
│       │   └── ProductService.java
│       └── dto/
│           └── ProductResponse.java
└── controller/
    ├── UserController.java
    ├── RoutineController.java
    ├── FeedController.java
    ├── FollowController.java
    ├── ProductController.java
    └── MotiTransactionController.java
```

---

## 🎯 핵심 비즈니스 로직

### 1. 루틴 참여 제한
- **카테고리별 1개 제한**: 운동/공부/독서 각 카테고리당 1개 루틴만 참여 가능
- **정원 제한**: 모든 루틴 30명 고정
- **재참여 제한**: 강퇴 시 1개월간 해당 루틴 참여 불가

### 2. 주차 계산 (Rolling 7 Days)
- 개인별 참여일 기준으로 주차 계산
- Week 1: 참여일 + 0~6일
- Week 2: 참여일 + 7~13일
- Week 3: 참여일 + 14~20일
- ...

**예시**:
```
김모티 참여일: 6월 1일 (월)
- Week 1: 6/1(월) ~ 6/7(일)
- Week 2: 6/8(월) ~ 6/14(일)

박모티 참여일: 6월 3일 (수)
- Week 1: 6/3(수) ~ 6/9(화)
- Week 2: 6/10(수) ~ 6/16(화)
```

### 3. 주간 보상 시스템
**매일 자정 스케줄러 실행**:
1. 개인별 주차 종료일 확인
2. 주간 인증 횟수 확인
3. 목표 달성 시:
    - MOTI 지급 (EASY: 2, STANDARD: 3, HARD: 4)
    - 스트릭(연속 성공 주차) 증가
    - MotiTransaction 생성
4. 목표 미달 시:
    - 스트릭 초기화
    - MOTI 지급 없음

**Difficulty별 기준**:
- EASY: 주 2회 이상 → 2 MOTI
- STANDARD: 주 3회 이상 → 3 MOTI
- HARD: 주 4회 이상 → 4 MOTI

### 4. 페널티 시스템
**매일 자정 스케줄러 실행**:
1. 최근 2주 연속 실패 확인
2. 첫 실패:
    - 1주 정지 (PENALTY 상태)
    - 정지 해제 후 다시 참여 가능
3. 정지 후 재실패:
    - 강퇴 (KICKED 상태)
    - 1개월간 해당 루틴 재참여 불가

### 5. 방 폭파 조건
**매일 자정 스케줄러 실행**:
- 조건 1: 활성 참여자 3명 이하
- 조건 2: 최근 14일간 인증 업로드 0건
- 두 조건 모두 충족 시 → CLOSED 상태로 변경

### 6. 루틴 통계 갱신
**매일 자정 스케줄러 실행**:
- 활성 참여자 수 계산
- 최근 7일/14일 인증 수 집계
- 오늘/어제 인증 수 집계
- RoutineStats 테이블 업데이트

---

## 🔐 인증 시스템

### JWT 토큰 구조
- **Access Token**: 1시간 유효
- **Refresh Token**: 2주 유효, DB 저장

### 인증 흐름
1. 소셜 로그인 (카카오/구글/네이버)
2. Access Token + Refresh Token 발급
3. API 요청 시 Access Token 포함
4. Access Token 만료 시 Refresh Token으로 재발급

---

## 📊 데이터베이스 스키마

### 주요 테이블

#### users
```sql
CREATE TABLE users (
    user_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    social_type VARCHAR(20) NOT NULL,
    social_id VARCHAR(100) NOT NULL UNIQUE,
    nickname VARCHAR(50) NOT NULL UNIQUE,
    profile_image_url VARCHAR(500),
    bio VARCHAR(200),
    moti_balance INT DEFAULT 0,
    refresh_token VARCHAR(500),
    refresh_token_expires_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

#### routines
```sql
CREATE TABLE routines (
    routine_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(100) NOT NULL,
    description TEXT,
    category VARCHAR(20) NOT NULL,
    difficulty VARCHAR(20) NOT NULL,
    current_participants INT DEFAULT 0,
    max_participants INT DEFAULT 30,
    start_date DATE NOT NULL,
    region VARCHAR(50),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

#### routine_participants
```sql
CREATE TABLE routine_participants (
    participant_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    routine_id BIGINT NOT NULL,
    joined_at TIMESTAMP NOT NULL,
    total_certification_count INT DEFAULT 0,
    current_week_number INT DEFAULT 1,
    consecutive_success_weeks INT DEFAULT 0,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    penalty_count INT DEFAULT 0,
    penalty_start_date DATE,
    penalty_end_date DATE,
    kicked_at TIMESTAMP,
    ban_until DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY (user_id, routine_id)
);
```

#### weekly_certifications
```sql
CREATE TABLE weekly_certifications (
    cert_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    routine_id BIGINT NOT NULL,
    week_number INT NOT NULL,
    week_start_date DATE NOT NULL,
    week_end_date DATE NOT NULL,
    certification_count INT DEFAULT 0,
    is_success BOOLEAN DEFAULT FALSE,
    moti_earned INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY (user_id, routine_id, week_number)
);
```

#### routine_stats
```sql
CREATE TABLE routine_stats (
    stat_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    routine_id BIGINT NOT NULL,
    date DATE NOT NULL,
    daily_certification_count INT DEFAULT 0,
    yesterday_certification_count INT DEFAULT 0,
    active_participants INT DEFAULT 0,
    last_7days_cert_count INT DEFAULT 0,
    last_14days_cert_count INT DEFAULT 0,
    updated_at TIMESTAMP,
    UNIQUE KEY (routine_id, date)
);
```

#### feeds
```sql
CREATE TABLE feeds (
    feed_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    routine_id BIGINT NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    caption TEXT,
    is_shared_to_routine BOOLEAN DEFAULT TRUE,
    like_count INT DEFAULT 0,
    comment_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

---

## ⚙️ 스케줄러

### 1. WeeklyCertificationService
**실행 시간**: 매일 00:00:00  
**역할**: 주간 보상 지급
```java
@Scheduled(cron = "0 0 0 * * *")
```
- 개인별 주차 종료일 확인
- 주간 목표 달성 여부 판단
- MOTI 지급 및 거래 내역 생성

### 2. PenaltyService
**실행 시간**: 매일 00:00:00  
**역할**: 페널티 및 강퇴 처리
```java
@Scheduled(cron = "0 0 0 * * *")
```
- 정지 해제 처리
- 2주 연속 실패 시 정지 부여
- 정지 후 재실패 시 강퇴

### 3. RoutineStatsService
**실행 시간**: 매일 00:00:00  
**역할**: 통계 갱신 및 방 폭파 체크
```java
@Scheduled(cron = "0 0 0 * * *")
```
- 루틴별 통계 계산
- 방 폭파 조건 체크
- RoutineStats 테이블 업데이트

---

## 🚀 주요 API 엔드포인트

### User
- `POST /api/auth/login` - 소셜 로그인
- `GET /api/users/{userId}` - 프로필 조회
- `PUT /api/users/{userId}` - 프로필 수정

### Routine
- `POST /api/routines` - 루틴 생성
- `GET /api/routines/recruiting` - 모집 중인 루틴
- `POST /api/routines/{routineId}/join` - 루틴 참여
- `DELETE /api/routines/{routineId}/withdraw` - 루틴 탈퇴

### Feed
- `POST /api/feeds` - 인증 업로드
- `GET /api/feeds` - 홈 피드
- `POST /api/feeds/{feedId}/like` - 좋아요
- `POST /api/feeds/{feedId}/comments` - 댓글 작성

### Follow
- `POST /api/users/{userId}/follow` - 팔로우
- `GET /api/users/{userId}/followers` - 팔로워 목록

### Product
- `GET /api/products` - 상품 목록

### Moti
- `GET /api/users/{userId}/moti/transactions` - 거래 내역

자세한 API 명세는 `API_SPECIFICATION.md` 참고

---

## 🎨 주요 기획 사항

### 카테고리별 루틴 제한
- 운동/공부/독서 각 카테고리당 **1개 루틴만 참여 가능**
- 최대 3개 루틴 동시 참여 (각 카테고리 1개씩)
- 예: 운동 1개 + 공부 1개 + 독서 1개 = 총 3개

### 모티 지급 방식
- 카테고리별로 **각각 지급**
- 예: 운동 2 MOTI + 공부 4 MOTI + 독서 3 MOTI = 총 9 MOTI/주

### 정원 및 마감
- 모든 루틴 **30명 고정**
- 30/30 달성 시 **마감** 카테고리로 표시
- 마감된 루틴은 참여 불가

### 활동 게시물
- Feed 테이블 재사용
- `isSharedToRoutine` 필드로 공유 여부 제어
- 홈 피드: 전체 피드
- 활동 게시물: `isSharedToRoutine = true`인 피드만

### 이미지 저장
- DB에는 URL만 저장
- 실제 파일은 클라우드 스토리지 (Cloudflare R2 또는 AWS S3)

---

## 🔍 중요 체크 포인트

### 1. Service Layer에서 비즈니스 로직 처리
- 카테고리별 1개 제한 체크
- 정원 확인
- 재참여 가능 여부 확인
- 권한 확인 (본인 확인 등)

### 2. Entity에 비즈니스 메서드 구현
- `canJoin()`: 참여 가능 여부
- `increaseCertification()`: 인증 횟수 증가
- `applyPenalty()`: 페널티 부여
- 등 도메인 로직은 엔티티에 최대한 위임

### 3. DTO에서 엔티티 변환
- `from()` 정적 메서드 활용
- 엔티티를 직접 반환하지 않음
- 필요한 정보만 선택적으로 노출

### 4. 스케줄러 트랜잭션 관리
- `@Transactional` 필수
- 대량 데이터 처리 시 배치 고려
- 로그 남기기 (`@Slf4j`)

---

## 🐛 알려진 이슈 및 고려사항

### 1. 스케줄러 중복 실행 방지
- 분산 환경에서 스케줄러 중복 실행 가능
- Redis Lock 또는 ShedLock 도입 고려

### 2. Feed 조회 성능
- `findAll()` 후 Stream 필터링은 비효율적
- Repository에 쿼리 메서드 추가 필요
- 페이징 처리 필요

### 3. isLikedByMe 계산
- N+1 문제 발생 가능
- Batch Size 설정 또는 Join Fetch 고려

### 4. 파일 업로드
- 현재 imageUrl만 받음
- 실제 파일 업로드 API 구현 필요

---

## 📚 참고 문서
- API 명세서: `API_SPECIFICATION.md`
- ERD: `ERD.md`