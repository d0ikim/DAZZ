# DAZZ LLM Wiki — Change Log

**규칙**:
- 최신 항목이 맨 위에 온다
- 형식: `YYYY-MM-DD — [action] — [대상 파일] — [한 줄 이유]`
- action 종류: `created` / `updated` / `deleted` / `merged`
- Wiki 페이지를 추가하거나 수정할 때마다 반드시 여기 기록한다

---

## 2026-06

aㅗ- 2026-06-06 — updated — [[concepts/hexagonal-architecture]] — MySQL 로컬 포트 3307 → 3306 정정 (실제 동작 확인 기준) → [[concepts/hexagonal-architecture]]
- 2026-06-06 — created — `test/resources/features/musician_insights.feature` — Happy 2 + Unhappy 2 인수 테스트 시나리오 → [[entities/musician]]
- 2026-06-06 — created — `test/steps/MusicianInsightSteps.java` — 인사이트 조회 인수 테스트 Step 정의 → [[entities/musician]] · [[concepts/hexagonal-architecture]]
- 2026-06-06 — created — `test/steps/CommonSteps.java` — 응답 상태코드/에러코드 검증 공통 Step (모든 feature 재사용) → [[concepts/hexagonal-architecture]]
- 2026-06-06 — created — `test/support/ScenarioContext.java` — @Scope("cucumber-glue") 시나리오 간 상태 공유 컨텍스트 → [[concepts/hexagonal-architecture]]
- 2026-06-06 — updated — `test/support/TestAdapter.java` — get(queryParams), postWithIdempotencyKey() 메서드 추가 → [[concepts/hexagonal-architecture]]
- 2026-06-06 — refactored — `test/steps/MusicianClaimSteps.java` — ScenarioContext 기반으로 리팩토링 (기능 변경 없음) → [[concepts/hexagonal-architecture]]
- 2026-06-06 — updated — `docs/05-api-spec.md` — GET /api/v1/musicians/{musicianId}/insights 인수 테스트 목록 및 depth 파라미터 범위 반영 → [[entities/musician]]
- 2026-06-06 — updated — [[entities/musician]] — 인사이트 조회 인수 테스트 상세, 테스트 지원 클래스 목록, 패키지 구조 업데이트
- 2026-06-06 — updated — [[concepts/hexagonal-architecture]] — TestAdapter/ScenarioContext/CommonSteps 패턴 및 feature 파일 목록 추가

- 2026-06-05 — created — `domain/musician/exception/CollaborationDuplicateException` — C001 협업 중복 예외 → [[entities/collaboration]]
- 2026-06-05 — created — `domain/musician/exception/CollaborationSelfReferenceException` — C002 자기참조 예외 → [[entities/collaboration]]
- 2026-06-05 — created — `domain/musician/exception/CollaborationConcurrentException` — C003 분산락 획득 실패 예외 → [[concepts/collaboration-weight]]
- 2026-06-05 — created — `domain/musician/exception/IdempotencyConflictException` — COM002 동일 키 + 다른 페이로드 예외 → [[concepts/collaboration-weight]]
- 2026-06-05 — updated — `domain/shared/ErrorCode` — C003(COLLABORATION_CONCURRENT), COM002(IDEMPOTENCY_CONFLICT) 추가 → [[entities/collaboration]]
- 2026-06-05 — updated — `domain/musician/Collaboration` — newPair() 정적 팩토리 추가 (weight=1 초기값 강제화) → [[entities/collaboration]]
- 2026-06-05 — created — `application/collaboration/CollaborationCommandService` — linkOrIncrement() @Transactional 서비스, min:max 정규화 → [[concepts/collaboration-weight]]
- 2026-06-05 — created — `application/collaboration/CollaborationFacade` — Redisson 분산락 + 멱등성 캐시 외부 계층 → [[concepts/collaboration-weight]] · [[entities/redis]]
- 2026-06-05 — created — `application/port/out/IdempotencyRepository` — Redis 멱등성 저장소 포트 → [[entities/redis]]
- 2026-06-05 — created — `infrastructure/cache/IdempotencyRepositoryImpl` — Redisson RBucket 기반 구현 (KEY: idempotency:{key}) → [[entities/redis]]
- 2026-06-05 — updated — `api/common/GlobalExceptionHandler` — MissingRequestHeaderException 핸들러 추가 (Idempotency-Key 누락 → 400) → [[concepts/hexagonal-architecture]]
- 2026-06-05 — created — `api/collaboration/CollaborationController` — POST /api/v1/collaborations (신규 201 / 기존 200) → [[entities/collaboration]]
- 2026-06-05 — created — `api/collaboration/dto/CollaborationRequest` — fromMusicianId, toMusicianId, relationType → [[entities/collaboration]]
- 2026-06-05 — created — `api/collaboration/dto/CollaborationResponse` — id, weight, created 플래그 포함 → [[entities/collaboration]]
- 2026-06-05 — created — `test/application/collaboration/CollaborationCommandServiceTest` — 단위 테스트 5개 (신규/증가/정규화/자기참조/뮤지션없음) → [[entities/collaboration]]
- 2026-06-05 — created — `test/api/collaboration/CollaborationControllerTest` — 단위 테스트 6개 (201/200/헤더누락/C002/M001/C003/COM002) → [[entities/collaboration]]
- 2026-06-05 — updated — `docs/05-api-spec.md` — 협업 API 명세 실제 구현과 동기화, C003/COM002 에러코드 추가
- 2026-06-05 — updated — `docs/07-concurrency.md` — 실제 구현 클래스명(CollaborationFacade/CommandService)으로 코드 예시 갱신, 구현 완료 표시
- 2026-06-05 — updated — [[concepts/collaboration-weight]] — 실제 구현 흐름으로 코드 구조 갱신, 구현 완료 표시
- 2026-06-05 — updated — [[entities/collaboration]] — weight 업데이트 패턴 실제 클래스명으로 갱신, 구현 완료 API 명시

## 2026-05

- 2026-05-24 — created — `api/musician/MusicianController` — GET /api/v1/musicians/{id}/insights 컨트롤러 구현 → [[entities/musician]]
- 2026-05-24 — created — `api/musician/dto/MusicianInsightResponse` — 인사이트 응답 DTO (profile/docentNote/network 중첩 record)
- 2026-05-24 — created — `api/musician/mapper/MusicianInsightMapper` — MusicianInsightResult → MusicianInsightResponse 변환
- 2026-05-24 — created — `application/musician/MusicianInsightResult` — 인사이트 서비스 반환 타입 (Musician + NetworkEntry 목록)
- 2026-05-24 — updated — `application/musician/MusicianQueryService` — getInsight(musicianId, includeNetwork, depth) 추가, CollaborationRepository 의존성 추가
- 2026-05-24 — updated — `application/port/out/MusicianRepository` — findAllByIds(List<Long>) 추가 (N+1 방지 배치 조회)
- 2026-05-24 — updated — `infrastructure/persistence/musician/MusicianRepositoryImpl` — findAllByIds 구현
- 2026-05-24 — updated — `api/common/GlobalExceptionHandler` — ConstraintViolationException 핸들러 추가 (depth @Min/@Max 검증)
- 2026-05-24 — created — `test/api/musician/MusicianControllerTest` — Happy Path 2개 + Unhappy Path 2개 (404/400)
- 2026-05-24 — refactored — `domain/club/Club` — domain/performance에서 독립 패키지로 분리 (DDD Bounded Context 정렬)
- 2026-05-24 — created — `infrastructure/persistence/club/*` — ClubJpaEntity, ClubJpaRepository, ClubRepositoryImpl 신규 패키지 생성
- 2026-05-24 — updated — `infrastructure/persistence/performance/PerformanceRepositoryImpl` — ClubRepository 구현 제거, findById 충돌 해소
- 2026-05-24 — created — `infrastructure/persistence/converter/StringListConverter` — List<String> ↔ VARCHAR 변환기
- 2026-05-24 — updated — `domain/group/Group` — genreTags String → List<String>
- 2026-05-24 — updated — `infrastructure/persistence/group/GroupJpaEntity` — genreTags @Convert(StringListConverter) 적용
- 2026-05-24 — refactored — `domain/musician/Musician` — claim() 도메인 가드 추가 (isClaimed 체크를 서비스 → 도메인으로 이동) → [[entities/musician]]
- 2026-05-24 — refactored — `domain/musician/Collaboration` — incrementWeight() 불변 업데이트 메서드 추가 → [[entities/collaboration]]
- 2026-05-24 — created — `test/domain/musician/MusicianTest` — Happy Path / Unhappy Path 단위 테스트 4개 (BUILD SUCCESSFUL) → [[entities/musician]]
- 2026-05-24 — updated — `application/port/out/GroupRepository` — findGroupsByMusicianId 반환타입 List<GroupMember> → List<Group> 수정
- 2026-05-24 — refactored — `domain/musician/Musician` — 잘못된 Lombok 주석 제거 (claim() 내부 new 생성자 직접 호출 관련) → [[entities/musician]]
- 2026-05-24 — refactored — `application/musician/MusicianCommandService` — claim() 검증 순서 복원: M002(isClaimed) → M003(existsByUserId) · [[entities/musician]] · [[concepts/hexagonal-architecture]]
- 2026-05-24 — refactored — `domain/group/Group` — genreTags List.copyOf() 방어적 복사 적용 (불변성 보장)
- 2026-05-24 — created — `test/domain/musician/CollaborationTest` — incrementWeight() 단위 테스트 2개 (새 객체 반환 + 원본 불변 검증) → [[entities/collaboration]]
- 2026-05-23 — created — `infrastructure/config/SwaggerConfig` — springdoc OpenAPI 빈 등록 · [[concepts/hexagonal-architecture]]
- 2026-05-23 — updated — `infrastructure/security/SecurityConfig` — Swagger UI, /api/v1/** 허용 · [[concepts/hexagonal-architecture]]
- 2026-05-23 — created — `api/common/*` — ApiResponse, ErrorResponse, GlobalExceptionHandler · [[concepts/hexagonal-architecture]]
- 2026-05-23 — updated — [[concepts/hexagonal-architecture]] — 실제 패키지 구조 최신화
- 2026-05-23 — updated — `docs/05-api-spec.md` + `docs/index.html` — 에러 코드 전체 정의 (COM/M/A/C/G/P/U 도메인별, 내부코드 포함)
- 2026-05-23 — updated — `wiki/index.html` — Musician 예외 클래스 표 추가, 패키지 구조 업데이트, 변경이력 추가
- 2026-05-23 — created — `domain/musician/exception/*` — MusicianAlreadyClaimedException(M002), MusicianUserAlreadyLinkedException(M003) → [[entities/musician]]
- 2026-05-23 — created — `application/musician/*` — QueryService(readOnly), CommandService, RegisterCommand · [[entities/musician]] · [[concepts/hexagonal-architecture]]
- 2026-05-23 — created — `infrastructure/persistence/*` — JPA Entity 10개, Spring Data Repo 10개, 구현체 7개 · [[concepts/hexagonal-architecture]]
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
- 2026-05-21 — updated — `wiki/concepts/hexagonal-architecture.md` — 로컬 dev 환경 포트 확정 (MySQL 3306, Redis 6379, Kafka 9092) 및 Spring Security 기본 동작 메모 추가
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
