🔥 Motiday (모티데이)

루틴 인증과 상호 동기부여를 결합한 웰니스 커뮤니티 플랫폼

<br/>
📋 프로젝트 소개<br/>
운동, 공부, 독서 등 개인의 루틴을 함께 실천하며 서로 동기부여를 주고받는 소셜 플랫폼입니다.
주간 목표 달성 시 MOTI(포인트) 를 지급하여 건강한 습관 형성을 돕습니다.
<br/><br/>

✨ 주요 기능<br/>

루틴 클럽 참여: 운동/공부/독서 카테고리별 루틴 생성 및 참여 (정원 30명)<br/>
주간 인증: 개인별 주차 계산 (Rolling 7 Days), 난이도별 목표 달성<br/>
보상 시스템: 목표 달성 시 MOTI 지급, 2주 연속 실패 시 페널티<br/>
소셜 기능: 팔로우, 좋아요, 댓글, 프로필 관리<br/>
제휴 스토어: 카테고리별 영양제 정보 제공<br/>

<br/><br/>
🛠 기술 스택<br/>

Backend: Java 21, Spring Boot 3.4.0, Spring Security, JPA<br/>
Database: MySQL 8.0<br/>
Authentication: JWT (Access Token + Refresh Token)<br/>
Documentation: Swagger UI<br/>

<br/>
⚙️ 실행 방법<br/>
1. 데이터베이스 설정<br/>
sqlCREATE DATABASE motiday;<br/>
2. application.yml 설정<br/>
yamlspring:<br/>
  datasource:<br/>
    url: jdbc:mysql://localhost:3306/motiday<br/>
    username: root<br/>
    password: your_password<br/>

jwt:<br/>
  secret: your-256-bit-secret-key-here<br/>
3. 실행<br/>
bash./gradlew bootRun<br/>
4. API 문서 확인<br/>
http://localhost:8080/swagger-ui.html<br/>

<br/>
🎯 핵심 로직<br/>
Rolling 7 Days 주차 계산<br/>
개인별 참여일 기준으로 주차를 독립적으로 계산합니다.<br/>
자동화 스케줄러 (매일 자정)<br/>

주간 보상 지급 및 스트릭 관리<br/>
페널티 시스템 (2주 연속 실패 시 정지/강퇴)<br/>
방 폭파 체크 (활성 참여자 3명 이하 + 14일간 인증 0건)<br/>

<br/>
👥 팀 프로젝트 (7인) 중 백앤드파트(1인)
