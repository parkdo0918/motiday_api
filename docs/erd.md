# Motiday ERD 설계

## 1. User (사용자)

사용자 계정 및 프로필 정보

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `user_id` | BIGINT | PK, AUTO_INCREMENT | 사용자 고유 ID |
| `social_type` | ENUM('KAKAO', 'GOOGLE', 'NAVER') | NOT NULL | 소셜 로그인 제공자 |
| `social_id` | VARCHAR(100) | NOT NULL | 소셜 로그인 고유 ID |
| `nickname` | VARCHAR(50) | NOT NULL | 사용자 닉네임 |
| `profile_image_url` | VARCHAR(500) | NULLABLE | 프로필 이미지 URL |
| `bio` | TEXT | NULLABLE | 한줄 소개 |
| `moti_balance` | INT | DEFAULT 0 | 현재 보유 모티 |
| `created_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 생성 일시 |
| `updated_at` | TIMESTAMP | ON UPDATE CURRENT_TIMESTAMP | 수정 일시 |

**Indexes**
- UNIQUE: (`social_type`, `social_id`)
- INDEX: `social_type`, `social_id`

---

## 2. Routine (루틴/챌린지)

루틴 기본 정보 및 설정 (지속형 클럽 구조)

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `routine_id` | BIGINT | PK, AUTO_INCREMENT | 루틴 고유 ID |
| `creator_user_id` | BIGINT | FK → User, NOT NULL | 루틴 생성자 ID |
| `title` | VARCHAR(100) | NOT NULL | 루틴 제목 |
| `description` | TEXT | NULLABLE | 루틴 설명 (목적, 효과) |
| `category` | ENUM('EXERCISE', 'STUDY', 'READING') | NOT NULL | 루틴 카테고리 |
| `max_participants` | INT | NULLABLE | 최대 참여 인원 (제한 없으면 NULL) |
| `current_participants` | INT | DEFAULT 0 | 현재 활성 참여자 수 |
| `difficulty` | ENUM('EASY', 'STANDARD', 'HARD') | NOT NULL | 난이도 (모드) |
| `certification_frequency_per_week` | INT | NOT NULL | 주당 최소 인증 횟수 (2/3/4) |
| `moti_per_week` | INT | NOT NULL | 주당 지급 모티 (2/3/4) |
| `start_date` | DATE | NOT NULL | 루틴 시작 날짜 |
| `region` | VARCHAR(50) | NULLABLE | 지역 (서울, 대구 등) |
| `is_public` | BOOLEAN | DEFAULT TRUE | 공개 여부 |
| `status` | ENUM('ACTIVE', 'CLOSED') | DEFAULT 'ACTIVE' | 루틴 상태 (폭파 시 CLOSED) |
| `created_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 생성 일시 |
| `updated_at` | TIMESTAMP | ON UPDATE CURRENT_TIMESTAMP | 수정 일시 |

**Indexes**
- FK: `creator_user_id` → User(`user_id`)
- INDEX: `category`, `difficulty`, `status`, `start_date`

---

## 3. RoutineParticipant (루틴 참여자)

사용자와 루틴 간의 참여 관계 및 페널티 관리

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `participant_id` | BIGINT | PK, AUTO_INCREMENT | 참여 기록 고유 ID |
| `routine_id` | BIGINT | FK → Routine, NOT NULL | 루틴 ID |
| `user_id` | BIGINT | FK → User, NOT NULL | 사용자 ID |
| `joined_at` | TIMESTAMP | NOT NULL | 참여 시작 일시 |
| `total_certification_count` | INT | DEFAULT 0 | 총 인증 횟수 |
| `current_week_number` | INT | DEFAULT 1 | 현재 주차 (개인 기준) |
| `consecutive_success_weeks` | INT | DEFAULT 0 | 연속 성공 주차 (스트릭) |
| `status` | ENUM('ACTIVE', 'PENALTY', 'KICKED', 'WITHDRAWN') | DEFAULT 'ACTIVE' | 참여 상태 |
| `penalty_count` | INT | DEFAULT 0 | 경고 누적 횟수 |
| `penalty_start_date` | DATE | NULLABLE | 정지 시작 날짜 |
| `penalty_end_date` | DATE | NULLABLE | 정지 종료 날짜 (1주 후) |
| `kicked_at` | TIMESTAMP | NULLABLE | 강퇴 일시 |
| `ban_until` | DATE | NULLABLE | 재참여 제한 종료일 (강퇴 후 1개월) |
| `created_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 생성 일시 |
| `updated_at` | TIMESTAMP | ON UPDATE CURRENT_TIMESTAMP | 수정 일시 |

**Indexes**
- UNIQUE: (`routine_id`, `user_id`)
- FK: `routine_id` → Routine(`routine_id`)
- FK: `user_id` → User(`user_id`)
- INDEX: `user_id`, `routine_id`, `status`, `ban_until`

---

## 4. Feed (인증 피드)

루틴 인증 게시물

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `feed_id` | BIGINT | PK, AUTO_INCREMENT | 피드 고유 ID |
| `user_id` | BIGINT | FK → User, NOT NULL | 작성자 ID |
| `routine_id` | BIGINT | FK → Routine, NOT NULL | 연관 루틴 ID |
| `image_url` | VARCHAR(500) | NOT NULL | 인증 이미지/영상 URL |
| `caption` | TEXT | NULLABLE | 캡션/메모 |
| `like_count` | INT | DEFAULT 0 | 좋아요 수 |
| `comment_count` | INT | DEFAULT 0 | 댓글 수 |
| `created_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 생성 일시 |
| `updated_at` | TIMESTAMP | ON UPDATE CURRENT_TIMESTAMP | 수정 일시 |

**Indexes**
- FK: `user_id` → User(`user_id`)
- FK: `routine_id` → Routine(`routine_id`)
- INDEX: `user_id`, `routine_id`, `created_at`

---

## 5. WeeklyCertification (주간 인증 기록)

참여일 기준 개인 주차별 인증 횟수 추적 (Rolling 7 days)

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `weekly_cert_id` | BIGINT | PK, AUTO_INCREMENT | 주간 인증 기록 ID |
| `user_id` | BIGINT | FK → User, NOT NULL | 사용자 ID |
| `routine_id` | BIGINT | FK → Routine, NOT NULL | 루틴 ID |
| `week_number` | INT | NOT NULL | 참여 후 주차 (1, 2, 3...) |
| `week_start_date` | DATE | NOT NULL | 해당 주차 시작일 (참여일 기준) |
| `week_end_date` | DATE | NOT NULL | 해당 주차 종료일 |
| `certification_count` | INT | DEFAULT 0 | 해당 주 인증 횟수 |
| `is_success` | BOOLEAN | DEFAULT FALSE | 주간 목표 달성 여부 |
| `moti_earned` | INT | DEFAULT 0 | 해당 주에 획득한 모티 (0 or 2/3/4) |
| `created_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 생성 일시 |
| `updated_at` | TIMESTAMP | ON UPDATE CURRENT_TIMESTAMP | 수정 일시 |

**Indexes**
- UNIQUE: (`user_id`, `routine_id`, `week_number`)
- FK: `user_id` → User(`user_id`)
- FK: `routine_id` → Routine(`routine_id`)
- INDEX: `user_id`, `routine_id`, `week_number`, `week_start_date`

---

## 6. Like (좋아요)

피드 좋아요 기록

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `like_id` | BIGINT | PK, AUTO_INCREMENT | 좋아요 고유 ID |
| `feed_id` | BIGINT | FK → Feed, NOT NULL | 피드 ID |
| `user_id` | BIGINT | FK → User, NOT NULL | 사용자 ID |
| `created_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 생성 일시 |

**Indexes**
- UNIQUE: (`feed_id`, `user_id`)
- FK: `feed_id` → Feed(`feed_id`)
- FK: `user_id` → User(`user_id`)
- INDEX: `feed_id`, `user_id`

---

## 7. Comment (댓글)

피드 댓글

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `comment_id` | BIGINT | PK, AUTO_INCREMENT | 댓글 고유 ID |
| `feed_id` | BIGINT | FK → Feed, NOT NULL | 피드 ID |
| `user_id` | BIGINT | FK → User, NOT NULL | 작성자 ID |
| `content` | TEXT | NOT NULL | 댓글 내용 |
| `created_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 생성 일시 |
| `updated_at` | TIMESTAMP | ON UPDATE CURRENT_TIMESTAMP | 수정 일시 |

**Indexes**
- FK: `feed_id` → Feed(`feed_id`)
- FK: `user_id` → User(`user_id`)
- INDEX: `feed_id`, `created_at`

---

## 8. Follow (팔로우)

사용자 간 팔로우 관계

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `follow_id` | BIGINT | PK, AUTO_INCREMENT | 팔로우 관계 ID |
| `follower_user_id` | BIGINT | FK → User, NOT NULL | 팔로우하는 사람 |
| `following_user_id` | BIGINT | FK → User, NOT NULL | 팔로우 당하는 사람 |
| `created_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 생성 일시 |

**Indexes**
- UNIQUE: (`follower_user_id`, `following_user_id`)
- FK: `follower_user_id` → User(`user_id`)
- FK: `following_user_id` → User(`user_id`)
- INDEX: `follower_user_id`, `following_user_id`

---

## 9. Product (상품)

스토어 제품 정보

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `product_id` | BIGINT | PK, AUTO_INCREMENT | 제품 고유 ID |
| `name` | VARCHAR(200) | NOT NULL | 제품명 |
| `brand` | VARCHAR(100) | NULLABLE | 브랜드명 |
| `price` | INT | NOT NULL | 판매 가격 |
| `original_price` | INT | NULLABLE | 원가 |
| `discount_rate` | INT | DEFAULT 0 | 할인율 (%) |
| `image_url` | VARCHAR(500) | NULLABLE | 제품 이미지 URL |
| `description` | TEXT | NULLABLE | 제품 설명 |
| `category` | ENUM('EXERCISE', 'STUDY', 'READING') | NULLABLE | 연관 카테고리 |
| `external_link` | VARCHAR(500) | NOT NULL | 외부 구매 링크 |
| `created_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 생성 일시 |
| `updated_at` | TIMESTAMP | ON UPDATE CURRENT_TIMESTAMP | 수정 일시 |

**Indexes**
- INDEX: `category`, `price`

---

## 10. MotiTransaction (모티 거래 내역)

모티 적립 및 사용 기록 (주간 보상 방식)

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `transaction_id` | BIGINT | PK, AUTO_INCREMENT | 거래 고유 ID |
| `user_id` | BIGINT | FK → User, NOT NULL | 사용자 ID |
| `routine_id` | BIGINT | FK → Routine, NULLABLE | 연관 루틴 ID |
| `weekly_cert_id` | BIGINT | FK → WeeklyCertification, NULLABLE | 연관 주간 인증 ID |
| `type` | ENUM('EARN', 'SPEND') | NOT NULL | 거래 유형 |
| `amount` | INT | NOT NULL | 모티 수량 (주간 보상: 2/3/4) |
| `description` | VARCHAR(200) | NULLABLE | 거래 설명 |
| `created_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 생성 일시 |

**Indexes**
- FK: `user_id` → User(`user_id`)
- FK: `routine_id` → Routine(`routine_id`)
- FK: `weekly_cert_id` → WeeklyCertification(`weekly_cert_id`)
- INDEX: `user_id`, `created_at`, `type`

---

## 11. RoutineStats (클럽 통계)

클럽별 날짜 기반 통계 (방 폭파 조건 확인용)

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `stat_id` | BIGINT | PK, AUTO_INCREMENT | 통계 고유 ID |
| `routine_id` | BIGINT | FK → Routine, NOT NULL | 루틴 ID |
| `date` | DATE | NOT NULL | 통계 날짜 |
| `daily_certification_count` | INT | DEFAULT 0 | 해당 날짜 인증 수 |
| `active_participants` | INT | DEFAULT 0 | 활성 참여자 수 |
| `last_14days_cert_count` | INT | DEFAULT 0 | 최근 14일 인증 수 (캐시) |
| `updated_at` | TIMESTAMP | ON UPDATE CURRENT_TIMESTAMP | 수정 일시 |

**Indexes**
- UNIQUE: (`routine_id`, `date`)
- FK: `routine_id` → Routine(`routine_id`)
- INDEX: `routine_id`, `date`

---

## ERD 관계도

```
User (1) ─────< (N) Routine (creator)
User (N) ─────< (M) Routine (via RoutineParticipant)
User (1) ─────< (N) Feed
Routine (1) ──< (N) Feed
User (1) ─────< (N) WeeklyCertification
Routine (1) ──< (N) WeeklyCertification
Feed (1) ─────< (N) Like
Feed (1) ─────< (N) Comment
User (1) ─────< (N) Like
User (1) ─────< (N) Comment
User (N) ─────< (M) User (via Follow, self-referencing)
User (1) ─────< (N) MotiTransaction
Feed (1) ─────< (N) MotiTransaction
Routine (1) ──< (N) MotiTransaction
```

---

## 주요 비즈니스 로직

### 1. 루틴 생성
- 생성자가 시작 날짜(`start_date`) 설정
- 종료일 없는 지속형 클럽 구조
- 난이도(EASY/STANDARD/HARD) 선택 필수

### 2. 루틴 참여 (중도 참여 가능)
- 언제든 참여 가능 (모집 인원 제한 없음)
- 참여 시 `RoutineParticipant` 생성
- `joined_at` 기준으로 개인 Week1 시작
- `Routine.current_participants++`

### 3. 인증 (Feed 생성)
1. 참여일(`joined_at`) 기준 현재 `week_number` 계산
2. `WeeklyCertification` 조회/생성
3. 하루 1회 제한 확인
4. 통과 시:
    - `Feed` 생성
    - `WeeklyCertification.certification_count++`
    - 주차 종료 시점에 보상 지급

### 4. 주간 보상 지급 (매주 일요일 자정)
```
각 사용자의 개인 week_end_date 도달 시:

IF certification_count >= certification_frequency_per_week:
  - WeeklyCertification.is_success = TRUE
  - WeeklyCertification.moti_earned = moti_per_week
  - MotiTransaction 생성 (EARN, amount = moti_per_week)
  - User.moti_balance += moti_per_week
  - RoutineParticipant.consecutive_success_weeks++
ELSE:
  - WeeklyCertification.is_success = FALSE
  - RoutineParticipant.consecutive_success_weeks = 0
```

### 5. 페널티 시스템

#### 14일 연속 미달 (2주 연속 실패)
```
IF 최근 2주 모두 is_success = FALSE:
  - RoutineParticipant.status = 'PENALTY'
  - RoutineParticipant.penalty_count++
  - RoutineParticipant.penalty_start_date = 현재 날짜
  - RoutineParticipant.penalty_end_date = 현재 날짜 + 7일
  - 1주간 MOTI 적립 정지 (인증 가능, 보상 없음)
```

#### 정지 후 다시 실패
```
IF penalty 상태에서 다음 주도 실패:
  - RoutineParticipant.status = 'KICKED'
  - RoutineParticipant.kicked_at = 현재 시간
  - RoutineParticipant.ban_until = 현재 날짜 + 1개월
  - Routine.current_participants--
```

### 6. 방 폭파 조건 (자동 종료)
```
매일 자정 체크:

IF 아래 두 조건 모두 2주 연속 충족:
  1. Routine.current_participants <= 3
  2. 최근 14일간 인증 업로드 수 = 0

THEN:
  - Routine.status = 'CLOSED'
  - 모든 RoutineParticipant.status = 'WITHDRAWN'
```

### 7. 주차 계산 (개인 Rolling 7 days)
```
joined_at = 2025-06-05 (목요일)

Week 1: 2025-06-05 (목) ~ 2025-06-11 (수)
Week 2: 2025-06-12 (목) ~ 2025-06-18 (수)
Week 3: 2025-06-19 (목) ~ 2025-06-25 (수)
...

week_number = FLOOR((현재 날짜 - joined_at) / 7) + 1
week_start_date = joined_at + ((week_number - 1) × 7일)
week_end_date = week_start_date + 6일
```

### 8. 개인 통계 계산
- **이번 주 인증 횟수**: 현재 WeeklyCertification.certification_count
- **개인 달성률**: (certification_count / certification_frequency_per_week) × 100
- **개인 스트릭**: RoutineParticipant.consecutive_success_weeks

### 9. 클럽 통계 (날짜 기반)
- **참여자 수**: Routine.current_participants
- **최근 7일 인증 수**: SUM(RoutineStats.daily_certification_count) WHERE date >= 현재-7일
- **오늘 인증 수**: RoutineStats.daily_certification_count WHERE date = 오늘
- **어제 인증 수**: RoutineStats.daily_certification_count WHERE date = 어제

---

## 난이도별 인증 규칙 및 보상

| 난이도 | 주당 최소 인증 횟수 | 주간 보상 | 비고 |
|--------|-------------------|----------|------|
| EASY | 2회 이상 | 2 MOTI | 기준 이상 인증해도 보상 고정 |
| STANDARD | 3회 이상 | 3 MOTI | 기준 이상 인증해도 보상 고정 |
| HARD | 4회 이상 | 4 MOTI | 기준 이상 인증해도 보상 고정 |

**보상 지급 시점**:
- 개인 기준 주차 종료 시점 (week_end_date 23:59:59)
- 기준 횟수 달성 시 해당 난이도의 고정 모티 지급
- 예: EASY 모드에서 주 5회 인증해도 2 MOTI만 지급

**인증 제한**:
- 하루 1회 제한
- 주차별 횟수 제한 없음 (단, 보상은 고정)

---

## 페널티 및 강퇴 규칙

### 경고 (1주 정지)
- **조건**: 14일 연속 인증 기준 미달 (2주 연속 실패)
- **효과**:
    - 1주간 MOTI 적립 정지
    - 인증은 가능하나 보상 없음
    - 상태: `PENALTY`

### 강퇴
- **조건**: 정지 상태에서 다음 주도 기준 미달
- **효과**:
    - 클럽에서 강제 퇴출
    - 1개월간 해당 클럽 재참여 금지
    - 상태: `KICKED`
    - `ban_until` 설정

### 방 폭파 (자동 종료)
- **조건**: 아래 두 가지가 2주 연속 충족
    1. 활성 참여자 3명 이하
    2. 최근 14일간 인증 0건
- **효과**:
    - 클럽 종료 (`status = CLOSED`)
    - 모든 참여자 자동 탈퇴

