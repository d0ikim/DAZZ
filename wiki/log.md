커밋# DAZZ LLM Wiki — Change Log

**규칙**:
- 최신 항목이 맨 위에 온다
- 형식: `YYYY-MM-DD — [action] — [대상 파일] — [한 줄 이유]`
- action 종류: `created` / `updated` / `deleted` / `merged`
- Wiki 페이지를 추가하거나 수정할 때마다 반드시 여기 기록한다

---

## 2026-05

- 2026-05-23 — updated — `docs/05-api-spec.md` + `docs/index.html` — 에러 코드 전체 정의 (COM/M/A/C/G/P/U 도메인별, 내부코드 포함)
- 2026-05-23 — updated — `wiki/index.html` — Musician 예외 클래스 표 추가, 패키지 구조 업데이트, 변경이력 추가
- 2026-05-23 — created — `domain/musician/exception/*` — MusicianAlreadyClaimedException(M002), MusicianUserAlreadyLinkedException(M003)
- 2026-05-23 — created — `application/musician/*` — QueryService(readOnly), CommandService, RegisterCommand
- 2026-05-23 — created — `infrastructure/persistence/*` — JPA Entity 10개, Spring Data Repo 10개, 구현체 7개
- 2026-05-23 — created — `db/migration/V900~V905` — Flyway 마이그레이션 전체 테이블
- 2026-05-22 — updated — `docs/index.html` — 엔티티 다이어그램 VENUE→CLUB, GROUP 추가, Phase2 표기; 인덱스 전략 performance_datetime/venue_id→club_id/start_time
- 2026-05-22 — updated — `wiki/index.html` — Collaboration last_collaborated_at 잔존 참조 제거; VENUE.image_urls→CLUB 수정; no-docent-note MVP 범위 ✅→❌ 정정
- 2026-05-22 — created — `domain/shared/*` — BusinessException, ErrorCode, DomainEvent 공통 기반 생성
- 2026-05-22 — created — `domain/musician/*` — Musician(Aggregate Root), Collaboration, Position/VerificationTier/RelationType enum, MusicianNotFoundException
- 2026-05-22 — created — `domain/album/*` — Album, AlbumParticipation, ParticipationType enum
- 2026-05-22 — created — `domain/group/*` — Group, GroupMember (US-03 팀 정보)
- 2026-05-22 — created — `domain/performance/*` — Club, Performance, PerformanceLineup
- 2026-05-22 — created — `domain/user/*` — User, UserRole enum
- 2026-05-22 — created — `application/port/out/*` — 7개 Repository 인터페이스 (Musician/Collaboration/Album/Group/Club/Performance/User)
- 2026-05-22 — updated — `docs/04-schema.md` — MVP ERD 기준으로 전면 재작성
- 2026-05-22 — updated — `wiki/entities/musician.md` — position/sns_url/VerificationTier 기본값 실제 구현과 동기화
- 2026-05-22 — updated — `wiki/index.html` — musician/user/collaboration 인라인 문서 MVP ERD 기준으로 동기화
- 2026-05-21 — updated — `wiki/concepts/hexagonal-architecture.md` — Walking Skeleton 완료: @LocalServerPort 타이밍 버그 원인(빈 생성 시점 vs Tomcat 기동 시점) 및 Environment 지연 조회 해결책 추가
- 2026-05-21 — updated — `wiki/concepts/hexagonal-architecture.md` — TestAdapter 패턴 추가: HTTP 추상화 계층, JWT 확장 지점 설계
- 2026-05-21 — updated — `wiki/concepts/hexagonal-architecture.md` — Cucumber 테스트 전략 추가: RANDOM_PORT 이유, TestcontainersConfiguration vs Docker Compose 선택 근거
- 2026-05-21 — updated — `wiki/concepts/hexagonal-architecture.md` — SecurityConfig 적용 결과 추가: 익명 접근 403, JWT 추가 시 401로 전환 예정
- 2026-05-21 — updated — `wiki/concepts/hexagonal-architecture.md` — 로컬 dev 환경 포트 확정 (MySQL 3307, Redis 6379, Kafka 9092) 및 Spring Security 기본 동작 메모 추가
- 2026-05-21 — updated — `wiki/entities/kafka.md` — Spring Boot 3.4.x에서 spring-boot-starter-kafka 대신 spring-kafka 직접 사용으로 변경 반영
- 2026-05-19 — created — `wiki/decisions/no-docent-note-in-mvp.md` — MVP에서 도슨트 노트 미구현 결정 및 Post-MVP 조건 정리
- 2026-05-19 — created — `wiki/decisions/monorepo.md` — backend + frontend 모노레포 구성 결정 근거
- 2026-05-19 — created — `wiki/decisions/mvp-option-b.md` — 뮤지션 셀프온보딩 MVP 전략 채택 근거
- 2026-05-19 — created — `wiki/comparisons/redis-lock-vs-db-lock.md` — Redisson Pub/Sub 분산락 채택, DB 비관적 락 탈락 근거
- 2026-05-19 — created — `wiki/comparisons/mysql-vs-mongodb.md` — 참조 무결성 필수로 MySQL 채택, MongoDB 탈락 근거
- 2026-05-19 — created — `wiki/entities/user.md` — User↔Musician 1:1 Optional 관계, JWT 인증, role vs trust-tier 구분
- 2026-05-19 — created — `wiki/entities/kafka.md` — Kafka 토픽 목록, Outbox 패턴, 파티션 키 전략
- 2026-05-19 — created — `wiki/entities/redis.md` — Redis 이중 역할(캐시+락), 키 명명 규칙, TTL 근거
- 2026-05-19 — created — `wiki/entities/collaboration.md` — 자기참조 협업 엔티티, from<to 정규화, 가중치 업데이트 규칙
- 2026-05-19 — created — `wiki/concepts/graceful-degradation.md` — 3단계 폴백(L1/L2/L3), 항상 HTTP 200 반환 전략
- 2026-05-19 — created — `wiki/concepts/docent-note.md` — 도슨트 노트 개념 정의, MVP 미구현 이유
- 2026-05-19 — created — `wiki/concepts/hexagonal-architecture.md` — 헥사고날 아키텍처 레이어 규칙, Port/Adapter 패턴, 패키지 구조
- 2026-05-19 — created — `wiki/comparisons/kafka-vs-rabbitmq.md` — Fan-out + Replay 필요로 Kafka 선택한 근거
- 2026-05-19 — created — `wiki/entities/musician.md` — Musician Aggregate Root 구조, uuid/id 이중 식별자, 도메인 규칙
- 2026-05-19 — created — `wiki/concepts/trust-tier.md` — 사용자 신뢰 등급 4단계 정의 및 권한 매트릭스
- 2026-05-19 — created — `wiki/concepts/collaboration-weight.md` — 협업 가중치 정의, 동시성 문제, 분산락 패턴
- 2026-05-19 — created — `wiki/concepts/cold-start.md` — 콜드스타트 문제 정의 및 Option B 채택 근거
- 2026-05-19 — created — `wiki/index.md` — LLM Wiki 전체 진입점 및 카탈로그 초기 생성
- 2026-05-19 — created — `wiki/log.md` — 변경 이력 추적 파일 초기 생성
