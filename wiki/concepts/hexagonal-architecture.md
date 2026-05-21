# Hexagonal Architecture (헥사고날 아키텍처)

**Summary**: DAZZ의 근간 아키텍처. Domain이 Infrastructure를 모르도록 Port(인터페이스)로 격리한다. 계층 간 역방향 의존 절대 금지.
**Tags**: #architecture #domain #port #adapter
**Created**: 2026-05-19
**Last Updated**: 2026-05-21

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
| MySQL 8.0 | 3306 | **3307** | 로컬 MySQL 충돌 방지 |
| Redis 7 | 6379 | 6379 | - |
| Kafka | 9092 | 9092 | - |

Spring Boot `application.yaml`에서 MySQL만 3307로 설정. 나머지는 기본값 사용.

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
- `@LocalServerPort`로 RANDOM_PORT 자동 주입 → 포트 하드코딩 제거
- `@PostConstruct`로 RestAssured 설정을 앱 기동 시 한 번만 세팅
- 모든 step 정의에서 `testAdapter.get("/path")`로 HTTP 호출 통일
- JWT 인증 필요 시 `getWithToken(path, token)` 메서드 추가로 확장

---

## 관련 페이지

- [[concepts/collaboration-weight]]
- [[entities/musician]]
