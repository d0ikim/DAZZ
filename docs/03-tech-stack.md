# 03. Tech Stack — 선정 근거와 트레이드오프

> 이 문서는 각 기술 선택의 **방어 논리(Defense)**를 담습니다.
> 새로운 라이브러리/프레임워크 도입 제안은 이 문서의 원칙에 부합해야 합니다.

---

## 1. 전체 스택 한눈에

| 영역 | 기술 | 버전 |
| --- | --- | --- |
| Language | **Java** (또는 Kotlin 검토) | 17+ |
| Framework | **Spring Boot** | 3.x |
| ORM | **Spring Data JPA + Hibernate** | 3.x |
| RDBMS | **MySQL** | 8.0 |
| Cache / Lock | **Redis + Redisson** | 7.x / 3.x |
| Message Broker | **Apache Kafka** | 3.x |
| Resilience | **Resilience4j** | 2.x |
| Container | **Docker + docker-compose** | latest |
| CI/CD | **GitHub Actions** | - |
| Test | **JUnit 5 + Mockito + Testcontainers** | latest |
| Build | **Gradle (Kotlin DSL)** | 8.x |
| Observability | **Micrometer + Prometheus + Grafana** | latest |

---

## 2. Framework: Spring Boot 3.x

**선정 이유**
- Hexagonal Architecture 및 DDD 구현에 필요한 생태계 풍부 (Spring Data, Spring Cloud Stream, Spring Security)
- Java 17 LTS 기준 안정성 + virtual threads (Project Loom) 활용 가능
- AOP 기반 트랜잭션/락/Circuit Breaker 데코레이터 합성에 최적

**대안 검토**
- Micronaut, Quarkus: 시작 시간/메모리 우위 있으나 한국어 자료/팀 친숙도 낮음. 시그니처 프로젝트의 목적(취업/포트폴리오)을 고려할 때 Spring이 합리적

---

## 3. RDBMS: MySQL 8.0

**선정 이유**
- 뮤지션-앨범-협업 관계의 **참조 무결성**이 컨셉의 본질 → ACID 트랜잭션 필수
- 명확한 스키마 + 복합 인덱스 + Window Function (8.0+) 활용
- 운영 비용/생태계/매니지드 서비스(AWS RDS, Aurora) 측면에서 안정적 선택

**대안 검토**
- PostgreSQL: JSONB와 풍부한 인덱스(GIN, BRIN) 매력적이지만, MySQL의 단순성/AWS RDS 운영 편의성 우선
- MongoDB: 관계 그래프의 정합성 깨질 위험 → 컨셉(NFR-01)에 부합하지 않음
- Neo4j: 그래프 DB 매력적이나 1인 프로젝트의 운영 부담 + 트랜잭션 본체는 RDBMS가 더 안전. 그래프 연산은 Redis 캐시로 보완

---

## 4. Cache / Distributed Lock: Redis + Redisson

**선정 이유**
- NFR-02 (응답 0.5s)를 달성하기 위한 필수 계층
- 네트워크 깊이 탐색 결과는 "Write-once, Read-many" 특성 → 캐시 적중률 극대화
- **Redisson**의 **Pub/Sub 기반 분산락**: Spin Lock 대비 CPU/네트워크 효율 우수
- Redisson **Watchdog**: 좀비 락 방지 자동 연장

**캐시 키 네이밍 컨벤션**
```
musician:insight:{musicianId}:depth={N}          (TTL 10분)
musician:profile:{musicianId}                    (TTL 1시간)
collab:lock:{musicianAId}:{musicianBId}          (TTL 5초, 락 전용)
idempotency:musician:{key}                       (TTL 24시간)
```

**대안 검토**
- Memcached: TTL/Pub-Sub/Persistence 약함. 분산락 불가
- 로컬 캐시(Caffeine): Scale-out 환경에서 일관성 문제. **L2 캐시로 함께 사용**은 검토 가능

---

## 5. Messaging: Apache Kafka (가장 논쟁적인 선택)

> **🎯 가장 중요한 기술 결정: 왜 RabbitMQ가 아닌 Kafka인가?**

### 5.1 비즈니스 컨셉 연계

DAZZ는 **생물처럼 변화하는 생태계**다. '새로운 협업 등록'이라는 단 하나의 이벤트가 다발적 후속 작업을 트리거한다:
1. 네트워크 가중치 재계산 (무거운 작업)
2. Redis 캐시 무효화 및 재구축
3. 구독자 알림 발송 (외부 시스템)
4. 통계/분석용 데이터 적재 (확장 대비)

이를 동기 처리하면 NFR-02 (0.5s) 절대 불가 → 비동기 필수.

### 5.2 RabbitMQ와의 트레이드오프

| 항목 | RabbitMQ | **Kafka** |
| --- | --- | --- |
| 메시지 영속성 | 소비 시 삭제 | **디스크 로그로 보존 (Retention)** |
| 재처리(Replay) | 불가 | **가능 (offset 조정)** |
| Fan-out | Exchange 설정 필요 | **Consumer Group으로 자연스럽게** |
| 운영 복잡도 | 낮음 | 높음 (Zookeeper/KRaft, 파티션) |
| 처리량 | 중간 | **매우 높음** |
| 라우팅 유연성 | 강함 (Topic Exchange) | 약함 (단순 토픽) |

### 5.3 최종 방어 논리

> DAZZ 아키텍처의 핵심은 **'안전한 Fan-out'**과 **'장애복구력(Resilience)'**이다.

새 협업 데이터가 insert될 때:
- 알림 서버 일시 다운 → RabbitMQ면 데이터 영영 유실. **Kafka는 디스크에 안전 보관 → 복구 후 Replay**
- 향후 검색 인덱싱, 통계 분석 등 신규 컨슈머 추가 → **핵심 도메인 코드 0줄 수정**, 컨슈머만 붙이면 끝

**감수해야 할 비용**: 인프라 운영 오버헤드, 학습 곡선, 파티션 키 설계 필요 → "전략적 투자"로 정당화

### 5.4 토픽 설계 원칙

```
collaboration.created.v1          # 새 협업 등록
collaboration.weight.recalc.v1    # 가중치 재계산 요청
cache.invalidate.v1               # 캐시 무효화 신호
notification.dispatch.v1          # 알림 발송 요청
```

- 토픽명 끝의 `.v{N}`: 스키마 변경 시 신구 버전 병행 운영
- 파티션 키: 도메인 단위(예: `musicianId`)로 순서 보장
- Schema Registry (Avro/JSON Schema) 도입은 P1

---

## 6. Resilience: Resilience4j 2.x

**선정 이유**
- Spring Cloud Circuit Breaker의 기본 백엔드
- AOP 기반 데코레이터 합성 (`@Retry`, `@CircuitBreaker`, `@TimeLimiter`, `@Bulkhead`)
- Reactive/Sync 모두 지원
- Hystrix 후계자 (Netflix가 공식 Sunset)

**상세 설정 및 정책** → `/docs/08-fault-tolerance.md`

---

## 7. Infra & CI/CD: Docker + GitHub Actions

**선정 이유**
- 각 컴포넌트(Web, MySQL, Redis, Kafka)의 환경 일관성 보장
- 로컬 개발과 프로덕션 환경의 격차 최소화
- GitHub Actions: 별도 CI 서버 없이 무료 한도 내에서 충분

**디렉터리 구조**
```
infra/
├── docker/
│   ├── Dockerfile.app          (멀티스테이지 빌드)
│   └── docker-compose.yml       (로컬 개발용: app + mysql + redis + kafka)
└── .github/
    └── workflows/
        ├── ci.yml               (PR 시 테스트 + 빌드)
        └── cd.yml               (main 머지 시 배포)
```

---

## 8. Git Strategy: GitHub Flow

**선정 이유**
- **1인 프로젝트 효율**: `develop`/`release`/`hotfix` 등의 관리 오버헤드 불필요
- **CD 훈련 목적**: "main은 언제나 배포 가능"이라는 철학 학습
- **빠른 검증**: 새 기능을 `main`에 머지하는 순간 전체 시스템과의 조화 확인

**브랜치 규칙**
- `main`: 배포 가능한 상태만 유지. 직접 push 금지
- `feature/{이슈번호}-{짧은설명}`: 모든 작업은 feature 브랜치에서
- PR Gate: 자동 테스트 통과 + Self-review (1인이므로) 필수

**Git-Flow를 거절한 이유**: 대규모 팀 + 정기 릴리즈 사이클에 최적화된 워크플로우. 1인 + 지속 배포 환경에서는 오히려 속도 저하.

**리스크 관리**
- "main 안정성 우려"에 대한 답: PR 단계 GitHub Actions가 단위 테스트 + 통합 테스트 + 정적 분석을 강제. 통과해야만 머지 가능

---

## 9. 테스트 스택

| 종류 | 도구 | 용도 |
| --- | --- | --- |
| Unit Test | JUnit 5 + Mockito + AssertJ | 도메인 로직, Service |
| Integration Test | Spring Boot Test + **Testcontainers** | DB/Kafka/Redis 실제 컨테이너 |
| API Test | RestAssured / MockMvc | Controller 계층 |
| Performance Test | Gatling (P2) | NFR-02 검증 |

> **Testcontainers 채택 이유**: H2/임베디드 환경의 "프로덕션과 다른 동작" 문제를 회피. 1인 프로젝트라도 NFR-01(무결성)을 신뢰성 있게 검증하려면 실 DB가 필수.
