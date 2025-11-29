# Motiday API 명세서

## 📋 목차
1. [User API](#1-user-api)
2. [Routine API](#2-routine-api)
3. [Feed API](#3-feed-api)
4. [Follow API](#4-follow-api)
5. [Product API](#5-product-api)
6. [Moti Transaction API](#6-moti-transaction-api)

---

## 1. User API

### 1.1 소셜 로그인
```
POST /api/auth/login
```

**Request Body:**
```json
{
  "socialType": "KAKAO",  // KAKAO, GOOGLE, NAVER
  "socialId": "kakao_12345",
  "nickname": "김모티"
}
```

**Response (200 OK):**
```json
{
  "userId": 1,
  "nickname": "김모티",
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

---

### 1.2 프로필 조회
```
GET /api/users/{userId}
```

**Response (200 OK):**
```json
{
  "userId": 1,
  "nickname": "김모티",
  "profileImageUrl": "https://cdn.motiday.com/profiles/user1.jpg",
  "bio": "매일 운동하는 사람",
  "motiBalance": 25
}
```

---

### 1.3 프로필 수정
```
PUT /api/users/{userId}
```

**Headers:**
```
Authorization: Bearer {accessToken}
```

**Request Body:**
```json
{
  "nickname": "박모티",
  "profileImageUrl": "https://cdn.motiday.com/profiles/new.jpg",
  "bio": "새로운 소개글"
}
```

**Response (200 OK):**
```json
{
  "userId": 1,
  "nickname": "박모티",
  "profileImageUrl": "https://cdn.motiday.com/profiles/new.jpg",
  "bio": "새로운 소개글",
  "motiBalance": 25
}
```

---

### 1.4 닉네임 중복 체크
```
GET /api/users/check-nickname?nickname=김모티
```

**Response (200 OK):**
```json
{
  "available": true  // true: 사용 가능, false: 이미 존재
}
```

---

## 2. Routine API

### 2.1 루틴 생성
```
POST /api/routines
```

**Headers:**
```
Authorization: Bearer {accessToken}
```

**Request Body:**
```json
{
  "title": "매주 수요일 러닝할사람",
  "description": "수원 광교호수공원에서 함께 달려요",
  "category": "EXERCISE",  // EXERCISE, STUDY, READING
  "difficulty": "EASY",    // EASY(2회/주), STANDARD(3회/주), HARD(4회/주)
  "startDate": "2025-06-01",
  "region": "수원"
}
```

**Response (201 Created):**
```json
{
  "routineId": 10,
  "title": "매주 수요일 러닝할사람",
  "description": "수원 광교호수공원에서 함께 달려요",
  "category": "EXERCISE",
  "difficulty": "EASY",
  "currentParticipants": 1,
  "maxParticipants": 30,
  "startDate": "2025-06-01",
  "region": "수원",
  "status": "ACTIVE",
  "createdAt": "2025-05-25T10:00:00"
}
```

---

### 2.2 모집 중인 루틴 조회
```
GET /api/routines/recruiting?category={category}
```

**Query Parameters:**
- `category` (optional): EXERCISE, STUDY, READING, null(전체)

**Response (200 OK):**
```json
[
  {
    "routineId": 10,
    "title": "매주 수요일 러닝할사람",
    "description": "수원 광교호수공원에서 함께 달려요",
    "category": "EXERCISE",
    "difficulty": "EASY",
    "currentParticipants": 15,
    "maxParticipants": 30,
    "startDate": "2025-06-01",
    "region": "수원",
    "status": "ACTIVE",
    "createdAt": "2025-05-25T10:00:00"
  },
  ...
]
```

---

### 2.3 마감된 루틴 조회
```
GET /api/routines/closed?category={category}
```

**Query Parameters:**
- `category` (optional): EXERCISE, STUDY, READING, null(전체)

**Response (200 OK):**
```json
[
  {
    "routineId": 5,
    "title": "토익 스터디",
    "description": "매일 문제 풀고 인증해요",
    "category": "STUDY",
    "difficulty": "HARD",
    "currentParticipants": 30,
    "maxParticipants": 30,
    "startDate": "2025-05-01",
    "region": "서울",
    "status": "ACTIVE",
    "createdAt": "2025-04-20T14:00:00"
  },
  ...
]
```

---

### 2.4 내가 참여 중인 루틴
```
GET /api/users/{userId}/routines
```

**Headers:**
```
Authorization: Bearer {accessToken}
```

**Response (200 OK):**
```json
[
  {
    "routineId": 10,
    "title": "매주 수요일 러닝할사람",
    "category": "EXERCISE",
    "difficulty": "EASY",
    "currentParticipants": 15,
    "maxParticipants": 30,
    ...
  },
  ...
]
```

---

### 2.5 루틴 상세 조회
```
GET /api/routines/{routineId}
```

**Response (200 OK):**
```json
{
  "routineId": 10,
  "title": "매주 수요일 러닝할사람",
  "description": "수원 광교호수공원에서 함께 달려요",
  "category": "EXERCISE",
  "difficulty": "EASY",
  "currentParticipants": 15,
  "maxParticipants": 30,
  "startDate": "2025-06-01",
  "region": "수원",
  "status": "ACTIVE",
  "createdAt": "2025-05-25T10:00:00"
}
```

---

### 2.6 루틴 참여
```
POST /api/routines/{routineId}/join
```

**Headers:**
```
Authorization: Bearer {accessToken}
```

**Response (200 OK):**
```json
{
  "participantId": 50,
  "userId": 1,
  "routineId": 10,
  "totalCertificationCount": 0,
  "currentWeekNumber": 1,
  "consecutiveSuccessWeeks": 0,
  "joinedAt": "2025-05-30T15:00:00"
}
```

**Error Response (400 Bad Request):**
```json
{
  "error": "정원이 마감되었습니다. (30/30명)"
}
```

```json
{
  "error": "EXERCISE 카테고리 루틴은 이미 참여 중입니다. 하나의 카테고리에는 1개의 루틴만 참여할 수 있습니다."
}
```

---

### 2.7 루틴 탈퇴
```
DELETE /api/routines/{routineId}/withdraw
```

**Headers:**
```
Authorization: Bearer {accessToken}
```

**Response (200 OK):**
```json
{
  "message": "루틴에서 탈퇴했습니다."
}
```

---

### 2.8 루틴 통계 조회
```
GET /api/routines/{routineId}/stats
```

**Response (200 OK):**
```json
{
  "activeParticipants": 26,
  "last7DaysCertCount": 148,
  "dailyCertificationCount": 18,
  "yesterdayCertificationCount": 22
}
```

---

## 3. Feed API

### 3.1 피드 생성 (인증 업로드)
```
POST /api/feeds
```

**Headers:**
```
Authorization: Bearer {accessToken}
```

**Request Body:**
```json
{
  "routineId": 10,
  "imageUrl": "https://cdn.motiday.com/feeds/cert123.jpg",
  "caption": "오늘도 5km 완주!",
  "isSharedToRoutine": true
}
```

**Response (201 Created):**
```json
{
  "feedId": 100,
  "userId": 1,
  "userNickname": "김모티",
  "userProfileImage": "https://cdn.motiday.com/profiles/user1.jpg",
  "routineId": 10,
  "routineTitle": "매주 수요일 러닝할사람",
  "imageUrl": "https://cdn.motiday.com/feeds/cert123.jpg",
  "caption": "오늘도 5km 완주!",
  "likeCount": 0,
  "commentCount": 0,
  "isLikedByMe": false,
  "createdAt": "2025-05-30T18:00:00"
}
```

---

### 3.2 홈 피드 조회
```
GET /api/feeds
```

**Headers:**
```
Authorization: Bearer {accessToken}
```

**Response (200 OK):**
```json
[
  {
    "feedId": 100,
    "userId": 1,
    "userNickname": "김모티",
    "userProfileImage": "https://cdn.motiday.com/profiles/user1.jpg",
    "routineId": 10,
    "routineTitle": "매주 수요일 러닝할사람",
    "imageUrl": "https://cdn.motiday.com/feeds/cert123.jpg",
    "caption": "오늘도 5km 완주!",
    "likeCount": 15,
    "commentCount": 3,
    "isLikedByMe": true,
    "createdAt": "2025-05-30T18:00:00"
  },
  ...
]
```

---

### 3.3 활동 게시물 조회 (루틴별)
```
GET /api/routines/{routineId}/feeds
```

**Response (200 OK):**
```json
[
  {
    "feedId": 98,
    "userId": 5,
    "userNickname": "박모티",
    "userProfileImage": "https://cdn.motiday.com/profiles/user5.jpg",
    "routineId": 10,
    "routineTitle": "매주 수요일 러닝할사람",
    "imageUrl": "https://cdn.motiday.com/feeds/cert121.jpg",
    "caption": "비 와도 달린다!",
    "likeCount": 8,
    "commentCount": 2,
    "isLikedByMe": false,
    "createdAt": "2025-05-29T19:00:00"
  },
  ...
]
```

---

### 3.4 사용자별 피드 조회 (프로필)
```
GET /api/users/{userId}/feeds
```

**Response (200 OK):**
```json
[
  {
    "feedId": 100,
    "userId": 1,
    "userNickname": "김모티",
    "routineId": 10,
    "routineTitle": "매주 수요일 러닝할사람",
    "imageUrl": "https://cdn.motiday.com/feeds/cert123.jpg",
    "caption": "오늘도 5km 완주!",
    "likeCount": 15,
    "commentCount": 3,
    "isLikedByMe": true,
    "createdAt": "2025-05-30T18:00:00"
  },
  ...
]
```

---

### 3.5 좋아요
```
POST /api/feeds/{feedId}/like
```

**Headers:**
```
Authorization: Bearer {accessToken}
```

**Response (200 OK):**
```json
{
  "message": "좋아요를 눌렀습니다."
}
```

**Error (400 Bad Request):**
```json
{
  "error": "이미 좋아요한 피드입니다."
}
```

---

### 3.6 좋아요 취소
```
DELETE /api/feeds/{feedId}/like
```

**Headers:**
```
Authorization: Bearer {accessToken}
```

**Response (200 OK):**
```json
{
  "message": "좋아요를 취소했습니다."
}
```

---

### 3.7 댓글 작성
```
POST /api/feeds/{feedId}/comments
```

**Headers:**
```
Authorization: Bearer {accessToken}
```

**Request Body:**
```json
{
  "content": "대단해요! 저도 힘내야겠어요"
}
```

**Response (201 Created):**
```json
{
  "commentId": 50,
  "userId": 2,
  "userNickname": "이모티",
  "userProfileImage": "https://cdn.motiday.com/profiles/user2.jpg",
  "content": "대단해요! 저도 힘내야겠어요",
  "createdAt": "2025-05-30T18:30:00"
}
```

---

### 3.8 댓글 목록 조회
```
GET /api/feeds/{feedId}/comments
```

**Response (200 OK):**
```json
[
  {
    "commentId": 50,
    "userId": 2,
    "userNickname": "이모티",
    "userProfileImage": "https://cdn.motiday.com/profiles/user2.jpg",
    "content": "대단해요! 저도 힘내야겠어요",
    "createdAt": "2025-05-30T18:30:00"
  },
  ...
]
```

---

### 3.9 댓글 삭제
```
DELETE /api/feeds/comments/{commentId}
```

**Headers:**
```
Authorization: Bearer {accessToken}
```

**Response (200 OK):**
```json
{
  "message": "댓글을 삭제했습니다."
}
```

**Error (403 Forbidden):**
```json
{
  "error": "댓글 작성자만 삭제할 수 있습니다."
}
```

---

## 4. Follow API

### 4.1 팔로우
```
POST /api/users/{userId}/follow
```

**Headers:**
```
Authorization: Bearer {accessToken}
```

**Response (200 OK):**
```json
{
  "message": "팔로우했습니다."
}
```

**Error (400 Bad Request):**
```json
{
  "error": "자기 자신은 팔로우할 수 없습니다."
}
```

```json
{
  "error": "이미 팔로우 중입니다."
}
```

---

### 4.2 언팔로우
```
DELETE /api/users/{userId}/follow
```

**Headers:**
```
Authorization: Bearer {accessToken}
```

**Response (200 OK):**
```json
{
  "message": "언팔로우했습니다."
}
```

---

### 4.3 팔로워 목록 (나를 팔로우하는 사람들)
```
GET /api/users/{userId}/followers
```

**Response (200 OK):**
```json
[
  {
    "userId": 2,
    "nickname": "이모티",
    "profileImageUrl": "https://cdn.motiday.com/profiles/user2.jpg"
  },
  ...
]
```

---

### 4.4 팔로잉 목록 (내가 팔로우하는 사람들)
```
GET /api/users/{userId}/followings
```

**Response (200 OK):**
```json
[
  {
    "userId": 3,
    "nickname": "박모티",
    "profileImageUrl": "https://cdn.motiday.com/profiles/user3.jpg"
  },
  ...
]
```

---

### 4.5 팔로우 여부 확인
```
GET /api/users/{userId}/follow/status
```

**Headers:**
```
Authorization: Bearer {accessToken}
```

**Response (200 OK):**
```json
{
  "isFollowing": true
}
```

---

## 5. Product API

### 5.1 전체 상품 조회
```
GET /api/products?category={category}
```

**Query Parameters:**
- `category` (optional): EXERCISE, STUDY, READING, null(전체)

**Response (200 OK):**
```json
[
  {
    "productId": 1,
    "name": "아나로민 플러스, 2박스, 20개입",
    "brand": "센트룸",
    "price": 59900,
    "originalPrice": 150000,
    "discountRate": 63,
    "imageUrl": "https://cdn.motiday.com/products/product1.jpg",
    "category": "EXERCISE",
    "externalLink": "https://coupang.com/...",
    "isSpecialDeal": true
  },
  ...
]
```

---

### 5.2 상품 상세 조회
```
GET /api/products/{productId}
```

**Response (200 OK):**
```json
{
  "productId": 1,
  "name": "아나로민 플러스, 2박스, 20개입",
  "brand": "센트룸",
  "price": 59900,
  "originalPrice": 150000,
  "discountRate": 63,
  "imageUrl": "https://cdn.motiday.com/products/product1.jpg",
  "category": "EXERCISE",
  "externalLink": "https://coupang.com/...",
  "isSpecialDeal": true
}
```

---

## 6. Moti Transaction API

### 6.1 모티 거래 내역 조회
```
GET /api/users/{userId}/moti/transactions
```

**Headers:**
```
Authorization: Bearer {accessToken}
```

**Response (200 OK):**
```json
[
  {
    "transactionId": 100,
    "type": "EARN",
    "amount": 2,
    "description": "매주 수요일 러닝할사람 Week 1 달성",
    "createdAt": "2025-06-01T00:00:00"
  },
  {
    "transactionId": 95,
    "type": "SPEND",
    "amount": 15,
    "description": "체험단 신청: 비타민D 체험단",
    "createdAt": "2025-05-28T14:30:00"
  },
  ...
]
```

---

## 📌 공통 에러 응답

### 401 Unauthorized
```json
{
  "error": "인증이 필요합니다.",
  "code": "UNAUTHORIZED"
}
```

### 403 Forbidden
```json
{
  "error": "권한이 없습니다.",
  "code": "FORBIDDEN"
}
```

### 404 Not Found
```json
{
  "error": "리소스를 찾을 수 없습니다.",
  "code": "NOT_FOUND"
}
```

### 500 Internal Server Error
```json
{
  "error": "서버 오류가 발생했습니다.",
  "code": "INTERNAL_SERVER_ERROR"
}
```

---

## 📝 인증 관련

모든 인증이 필요한 API는 다음 헤더를 포함해야 합니다:

```
Authorization: Bearer {accessToken}
```

Access Token 만료 시:
- 401 Unauthorized 응답
- Refresh Token으로 재발급 필요