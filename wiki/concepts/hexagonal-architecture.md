# Hexagonal Architecture (헥사고날 아키텍처)

**Summary**: DAZZ의 근간 아키텍처. Domain이 Infrastructure를 모르도록 Port(인터페이스)로 격리한다. 계층 간 역방향 의존 절대 금지.
**Tags**: #architecture #domain #port #adapter
**Created**: 2026-05-19
**Last Updated**: 2026-06-06

---

## 핵심 원칙 한 줄

> **"Domain은 Infrastructure를 모른다."**

Domain 계층에 Spring, JPA, Kafka SDK 등 인프라 코드가 단 한 줄이라도 들어오면 아키텍처 위반.

---

## 계층 구조

```
┌─────────────────────────────────────────┐
│  api (Inbound Adapter)                  │  ← HTTP 요청 수신
│         │                               │
│         ▼                               │
│  application (Use Case + Port 정의)     │  ← 비즈니스 흐름 조율
│         │             ▲                 │
│         ▼             │ (Port 구현)     │
│  domain (Pure POJO)   │                 │  ← 비즈니스 규칙
│                       │                 │
│         infrastructure (Outbound)       │  ← DB, Redis, Kafka
└─────────────────────────────────────────┘
```

화살표 방향 = 유일한 의존 방향. **역방향 import 절대 금지.**

---

## 계층별 허용/금지 import

| 계층 | 허용 | 금지 |
| --- | --- | --- |
| `domain` | java.*, 자기 패키지 | Spring, JPA, Kafka SDK, HTTP |
| `application` | domain, application.port | api, infrastructure |
| `api` | application, api.dto | domain 직접, infrastructure |
| `infrastructure` | application.port, domain (mapper용) | api |

> `api`가 `domain`을 직접 참조하지 않는 이유: Entity 노출 방지. 응답은 항상 DTO.

---

## Port와 Adapter

**Port** = 인터페이스 (application/port/out/ 에 위치)
```java
// application/port/out/MusicianRepository.java
public interface MusicianRepository {
    Optional<Musician> findById(Long id);
    Musician save(Musician musician);
}
```

**Adapter** = 구현체 (infrastructure/ 에 위치)
```java
// infrastructure/persistence/musician/MusicianRepositoryImpl.java
@Repository
@RequiredArgsConstructor
public class MusicianRepositoryImpl implements MusicianRepository {
    private final MusicianJpaRepository jpaRepository;
    private final MusicianPersistenceMapper mapper;

    @Override
    public Optional<Musician> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }
}
```

Domain의 `Musician`과 JPA의 `MusicianJpaEntity`는 별개 클래스. Mapper로만 변환.

---

## 패키지 구조 (실제)

```
com.dazz.backend
├── api/musician/          ← Controller, DTO, Mapper
├── application/musician/  ← QueryService, CommandService, Facade
├── application/port/out/  ← Repository/Cache/Event 인터페이스
├── domain/musician/       ← Musician (POJO), Position, Collaboration
├── domain/shared/         ← BusinessException, DomainEvent
└── infrastructure/
    ├── persistence/       ← JpaEntity, JpaRepository, RepositoryImpl
    ├── cache/             ← RedisCacheAdapter
    └── messaging/         ← KafkaEventPublisher, Consumer
```

---

## 왜 이 아키텍처인가

DAZZ는 다양한 외부 의존성을 가진다:
- 입력: REST API, Kafka Consumer, Scheduler
- 출력: MySQL, Redis, Kafka, S3, YouTube, KakaoPay, Kakao Maps

인프라가 바뀌어도 (MySQL → PostgreSQL) Domain 코드를 건드리지 않으려면
이 격리 구조가 필수다.

---

## 로컬 개발 환경 포트 (Docker)

| 인프라 | 컨테이너 내부 | 로컬 접근 포트 | 비고 |
|---|---|---|---|
| MySQL 8.0 | 3306 | **3306** | - |
| Redis 7 | 6379 | 6379 | - |
| Kafka | 9092 | 9092 | - |

Spring Boot `application.yaml` MySQL 포트: 3306 (기본값 사용).

---

## Spring Security 동작 (Boot 3.x + SecurityConfig)

`SecurityConfig` 적용 결과:

| 경로 | 응답 | 설명 |
|---|---|---|
| `/actuator/health` | 200 | `permitAll()` 명시 |
| 그 외 모든 경로 | **403** | 익명 접근 차단 |

- 지금 403인 이유: JWT 같은 인증 수단이 없어서 "익명 = 접근 불가"로 처리
- JWT 필터 추가 이후: 미인증 요청은 **401**로 전환됨
- `SecurityConfig`의 역할: 기본값 의존 제거 + 인증 정책 명시적 선언 + JWT 추가 확장점 확보

---

## 테스트 전략 (통합 테스트)

**Testcontainers vs Docker Compose 선택:**

| 방식 | 특징 | DAZZ 선택 |
|---|---|---|
| Testcontainers | 테스트 실행 시 컨테이너 자동 생성/삭제 | 사용 안 함 |
| Docker Compose | 개발자가 직접 `docker compose up` | **채택** |

Testcontainers는 CI 환경에 유리하지만, 로컬 개발 단계에서는 `docker compose up` 한 번으로 환경을 고정하는 게 더 빠르고 단순하다.

**Cucumber 설정:**
- `@SpringBootTest(RANDOM_PORT)` — 테스트 전용 랜덤 포트로 앱 기동 (로컬 8080 충돌 방지)
- `@CucumberContextConfiguration` — Spring 컨텍스트를 Cucumber 시나리오와 공유
- `.feature` 파일 위치: `src/test/resources/features/`
- Step 정의 위치: `com.dazz.backend.steps`

**TestAdapter 패턴:**
- 위치: `src/test/java/com/dazz/backend/support/TestAdapter.java`
- 모든 step 정의에서 `testAdapter.get("/path")`로 HTTP 호출 통일
- JWT 인증 필요 시 `getWithToken(path, token)` 메서드 추가로 확장
- **현재 구현된 메서드**:
  - `get(path)` — 쿼리 파라미터 없는 GET
  - `get(path, queryParams)` — `Map<String, Object>` 형태의 쿼리 파라미터 GET
  - `post(path, body)` — JSON 본문 POST
  - `postWithIdempotencyKey(path, body, key)` — `Idempotency-Key` 헤더 포함 POST

**ScenarioContext 패턴:**
- 위치: `src/test/java/com/dazz/backend/support/ScenarioContext.java`
- `@Scope("cucumber-glue")` — Cucumber 시나리오 1개 = 빈 1개 생성/소멸 (시나리오 간 상태 격리)
- 보유 필드: `lastResponse`, `lastMusicianUuid`, `musicianIds`
- 여러 Step 클래스가 동일한 `ScenarioContext` 인스턴스를 `@Autowired`로 공유

**CommonSteps 패턴:**
- 위치: `src/test/java/com/dazz/backend/steps/CommonSteps.java`
- 응답 상태코드(`응답 상태코드는 {int} 이다`), 에러코드(`응답의 에러코드는 {string} 이다`) 검증
- 모든 feature 파일에서 재사용 가능한 공통 Step — 중복 제거 목적

**주의: `@LocalServerPort` 타이밍 버그**

`@LocalServerPort`는 `@Value("${local.server.port}")` 의 alias다.
`local.server.port` 프로퍼티는 Tomcat이 **완전히 기동된 후**에야 등록된다.
그런데 `@Component` 빈은 **컨텍스트 refresh 중 (Tomcat 기동 전)** 생성된다.
→ 빈 생성 시점에 `@LocalServerPort`로 주입하면 `PlaceholderResolutionException` 발생.

**해결: Environment 지연 조회**

```java
@Autowired
private Environment environment;

private int getPort() {
    // 메서드 호출 시점(테스트 실행 시점) = Tomcat 완전 기동 후 → 정상 조회
    return environment.getProperty("local.server.port", Integer.class, 8080);
}
```

HTTP 메서드 안에서 `getPort()`를 호출하므로, 실제 테스트가 시작될 때만 포트를 읽는다.

---

## 관련 페이지

- [[concepts/collaboration-weight]]
- [[entities/musician]]
