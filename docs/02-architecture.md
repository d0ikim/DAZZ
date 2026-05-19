    
# 02. Architecture — Hexagonal + DDD 설계 원칙

> 이 문서는 DAZZ의 **모듈 구조**와 **계층 간 의존성 규칙**을 정의합니다.
> 이 규칙은 모든 신규 코드에 예외 없이 적용됩니다.

---

## 1. 아키텍처 선택: Hexagonal Architecture (Ports & Adapters)

### 1.1 왜 Hexagonal인가?

DAZZ는 다음 특성을 가집니다:
1. **다양한 입력 채널**: REST API, Kafka Consumer, Scheduler
2. **다양한 외부 의존성**: MySQL, Redis, Kafka, S3, YouTube/SoundCloud, KakaoPay/Toss, Kakao/Naver Maps
3. **장기적 컨셉 보호**: 도메인 로직이 인프라 변경(예: MySQL → PostgreSQL)에 영향받지 않아야 함

→ **도메인을 인프라에서 격리**하는 Hexagonal이 최적.

### 1.2 핵심 원칙

> **"Domain은 Infrastructure를 모른다."**

- Domain Layer는 **순수 POJO**로 유지
- 외부 의존성은 **Port (interface)**로 추상화
- 실제 구현은 **Adapter**가 담당 (DB, HTTP Client, Message Broker)

---

## 2. 패키지 구조 (Strict)

```
com.dazz
├── api/                          # Inbound Adapter (HTTP)
│   ├── musician/
│   │   ├── MusicianController.java
│   │   ├── dto/
│   │   │   ├── MusicianInsightResponse.java    (record)
│   │   │   └── MusicianCreateRequest.java      (record)
│   │   └── mapper/
│   │       └── MusicianApiMapper.java
│   └── common/
│       ├── ApiResponse.java
│       ├── ErrorResponse.java
│       └── GlobalExceptionHandler.java
│
├── application/                  # Use Case Layer (Service)
│   ├── musician/
│   │   ├── MusicianQueryService.java
│   │   ├── MusicianCommandService.java
│   │   └── facade/
│   │       └── CollaborationFacade.java        (락 + 트랜잭션 조합)
│   └── port/
│       ├── in/                                  (Use Case 인터페이스, 선택)
│       └── out/                                 (Domain이 의존하는 Port)
│           ├── MusicianRepository.java          (interface)
│           ├── CachePort.java                   (interface)
│           ├── EventPublisherPort.java          (interface)
│           └── NotificationPort.java            (interface)
│
├── domain/                       # Pure Domain (No Spring, No JPA)
│   ├── musician/
│   │   ├── Musician.java                       (Aggregate Root, POJO)
│   │   ├── Position.java                       (Value Object, enum)
│   │   ├── Collaboration.java
│   │   └── exception/
│   │       └── MusicianNotFoundException.java
│   ├── album/
│   │   ├── Album.java
│   │   ├── AlbumParticipation.java
│   │   └── ParticipationType.java
│   ├── performance/
│   │   ├── Performance.java
│   │   └── PerformanceLineup.java
│   └── shared/
│       ├── BusinessException.java              (모든 도메인 예외의 부모)
│       └── DomainEvent.java
│
└── infrastructure/               # Outbound Adapter
    ├── persistence/
    │   ├── musician/
    │   │   ├── MusicianJpaEntity.java          (@Entity, JPA 전용)
    │   │   ├── MusicianJpaRepository.java      (Spring Data)
    │   │   ├── MusicianRepositoryImpl.java     (Port 구현)
    │   │   └── mapper/
    │   │       └── MusicianPersistenceMapper.java
    │   └── config/
    │       └── JpaConfig.java
    ├── cache/
    │   ├── RedisCacheAdapter.java              (CachePort 구현)
    │   └── config/
    │       ├── RedisConfig.java
    │       └── RedissonConfig.java             (분산락용)
    ├── messaging/
    │   ├── kafka/
    │   │   ├── producer/
    │   │   │   └── KafkaEventPublisher.java    (EventPublisherPort 구현)
    │   │   ├── consumer/
    │   │   │   ├── CollaborationEventConsumer.java
    │   │   │   └── CacheInvalidationConsumer.java
    │   │   └── config/
    │   │       └── KafkaConfig.java
    │   └── notification/
    │       └── FcmNotificationAdapter.java     (NotificationPort 구현)
    └── external/
        ├── youtube/
        │   └── YoutubeEmbedClient.java
        ├── maps/
        │   └── KakaoMapsClient.java
        └── payment/
            └── KakaoPayClient.java
```

---

## 3. 의존성 규칙 (절대 위반 금지)

```
┌──────────────────────────────────────────────┐
│  api (Inbound Adapter)                        │
│         │                                     │
│         ▼                                     │
│  application (Use Case + Port 정의)           │
│         │             ▲                       │
│         ▼             │ (Port 구현)           │
│  domain (Pure)        │                       │
│                       │                       │
│              infrastructure (Outbound)        │
└──────────────────────────────────────────────┘

화살표 방향이 유일한 의존 방향. 역방향 import 금지.
```

### 3.1 계층별 허용/금지

| 계층 | 허용 import | 금지 import |
| --- | --- | --- |
| `domain` | java.*, 자기 패키지 | Spring, JPA, jakarta.persistence, HTTP, Kafka SDK |
| `application` | domain, application.port | api, infrastructure |
| `api` | application, api.dto | domain (직접), infrastructure |
| `infrastructure` | application.port, domain (mapper용) | api |

> ⚠️ `api`가 `domain`을 직접 참조하지 않는 이유: Entity 노출 방지. 응답은 항상 DTO로.

---

## 4. Bounded Context (도메인 분리)

DDD 관점에서 다음 컨텍스트로 분리합니다.

| Context | 핵심 책임 | 주요 Aggregate Root |
| --- | --- | --- |
| **Musician Context** | 뮤지션 프로필, 협업 관계 | `Musician`, `Collaboration` |
| **Archive Context** | 앨범, 참여 이력 | `Album`, `AlbumParticipation` |
| **Performance Context** | 공연, 라인업, 공연장 | `Performance`, `Venue` |
| **Curation Context** | 도슨트 노트, 큐레이션 | `DocentNote`, `Curation` |
| **Identity Context** | 사용자, 인증, 권한 | `User`, `MusicianClaim` |
| **Notification Context** | 알림 발송 | `Subscription`, `NotificationEvent` |

> 컨텍스트 간 통신은 **Kafka 도메인 이벤트**로 처리. 직접 메서드 호출 금지.

---

## 5. Core Flow — 뮤지션 인사이트 조회 (Happy Path)

```
1. Client → GET /api/v1/musicians/{id}/insights?includeNetwork=true&depth=2
2. MusicianController → MusicianQueryService.getInsight(id, depth)
3. Service → CachePort.get("musician:insight:{id}:depth=2")
   ├─ HIT  → DTO 매핑 후 즉시 반환 (목표: < 100ms)
   └─ MISS → 다음 단계
4. Service → MusicianRepository.findInsightById(id, depth)
   ├─ Profile + DocentNote + Network(N+1 방지: Fetch Join)
5. Service → CachePort.set(...) (TTL 10분)
6. Service → API DTO 매핑
7. Controller → 200 OK with ApiResponse.success(dto)
```

### 5.1 새 협업 등록 시 비동기 흐름

```
1. POST /api/v1/collaborations → CommandService
2. CollaborationFacade.register() {
     [락 획득] Redisson lock("collab:lock:{musicianA}:{musicianB}")
       [트랜잭션 시작] @Transactional
         - DB 저장 (필수, ACID)
         - Outbox 패턴으로 이벤트 적재
       [트랜잭션 커밋]
     [락 해제]
   }
3. Outbox Poller → Kafka.publish("collaboration.created.v1")
4. Consumer (병렬) {
     - 관계 가중치 재계산 컨슈머
     - 캐시 무효화 컨슈머
     - 구독자 알림 컨슈머 (FCM)
   }
```

---

## 6. 트랜잭션 경계 규칙

- **하나의 Use Case = 하나의 트랜잭션**: Service 메서드에 `@Transactional` 부착
- **읽기 전용 조회**: `@Transactional(readOnly = true)` 명시
- **락 + 트랜잭션 조합**: 반드시 **Facade 패턴**으로 락이 트랜잭션을 감싸도록 (`/docs/07-concurrency.md` 참조)
- **외부 API 호출**: 트랜잭션 내에서 호출 금지 → 트랜잭션 커밋 후 이벤트 발행으로 분리

---

## 7. NotificationPort에 대한 결정

> `NotificationPort`는 **확장 대비용으로 인터페이스만 정의**합니다. MVP 단계에서는 구현체를 만들지 않습니다.
> 단, 도메인 이벤트(`SubscriptionUpdated`, `PerformanceLineupConfirmed`)는 Kafka로 발행되어 향후 컨슈머만 붙이면 됩니다.

이 결정의 의미:
- 지금: 알림 기능은 미구현, 단 도메인 이벤트는 흐른다
- 나중: 별도 Notification Service를 만들어 Kafka 컨슈머로 붙이기만 하면 끝 (핵심 도메인 코드 0줄 수정)
