# 07. Concurrency Control — 분산락, 멱등성, 동시성 방어

> 본 문서는 **'협업 가중치 업데이트'**를 중심으로 동시성 문제를 다룹니다.
> 모든 멀티스레드 환경의 쓰기 로직은 본 문서의 패턴을 따라야 합니다.

> **구현 상태**: ✅ `POST /api/v1/collaborations` 구현 완료
> 핵심 클래스: `CollaborationFacade` (락+멱등성) → `CollaborationCommandService` (@Transactional)

---

## 1. 문제 원인 분석 (Root Cause)

### 1.1 `count++`는 원자적이지 않다

CPU 관점에서 `weight++`은 **Read-Modify-Write 3단계**:
```
1. CPU 레지스터로 메모리에서 weight 값을 읽음   (Read)
2. 레지스터에서 +1 연산                          (Modify)
3. 레지스터 값을 메모리에 다시 씀                (Write)
```

**Context Switching의 함정**:
두 스레드가 거의 동시에 동일한 값을 읽어 메모리에 올리는 순간, 나중에 쓰는 스레드가 먼저 쓴 스레드의 결과를 **덮어쓰는 Lost Update**가 발생.

### 1.2 MySQL 8.0 Repeatable Read만으로는 부족

DAZZ는 MySQL 8.0의 기본 격리수준 **Repeatable Read (RR)**을 사용한다.

**깨지는 시나리오**:
1. 트랜잭션 A가 musician 102의 weight를 조회 (RR Snapshot 읽기)
2. 트랜잭션 B가 동일 행을 조회하고 weight를 1 증가시켜 커밋
3. 트랜잭션 A는 여전히 자기 Snapshot의 **이전 값**을 보고 연산
4. 트랜잭션 A의 UPDATE가 트랜잭션 B의 결과를 덮어씀

> **결론**: 격리수준만으로는 애플리케이션 레벨의 계산 로직(Read-Modify-Write)을 보호할 수 없다. **명시적 Locking 메커니즘이 필수**.

---

## 2. 분산락 전략 (Distributed Lock)

### 2.1 Redisson Pub/Sub 채택

| 구분 | Lettuce 기본 (Spin Lock) | **Redisson (Pub/Sub)** |
| --- | --- | --- |
| 대기 방식 | SETNX 반복 호출 | 락 해제 시 Redis가 신호 발송 |
| CPU 부하 | 높음 (Retry Storm 가능) | 낮음 |
| 네트워크 부하 | 높음 | 낮음 |

**채택 이유**: DAZZ는 고성능 조회가 핵심. CPU 리소스 효율 관리로 NFR-02 (0.5s) 준수.

### 2.2 좀비 락(Zombie Lock) 방어 — TTL 산출

**서버 장애로 락이 해제되지 않는 좀비 락 방지**를 위한 유효시간 설정.

**공식**:
```
TTL = Max(Latency) × Margin(10)
    = 0.5s × 10
    = 5초
```

**근거**:
- DAZZ 핵심 API의 p99 Latency: **0.5s**
- 비정상 케이스(p999) 고려한 안전 마진 10배

### 2.3 Watchdog 자동 연장

Redisson Watchdog 기능을 활성화:
- 비즈니스 로직이 예상보다 길어질 경우 락 수명을 자동 연장
- 로직 실행 도중 락이 풀려 다른 스레드 진입하는 현상 방지

---

## 3. AOP Trap 방어 — Facade 아키텍처

### 3.1 문제: 락과 `@Transactional`의 순서

```java
// ❌ 잘못된 구조: 트랜잭션 안에서 락
@Transactional
public void updateWeight(...) {
    redissonLock.lock();
    try {
        service.logic();
    } finally {
        redissonLock.unlock();
    }
}
```

**왜 깨지는가?**
- 락 해제 → 트랜잭션 커밋 사이에 다른 스레드가 락을 잡고 진입 가능
- 이전 스레드의 변경사항이 DB에 아직 반영되지 않은 상태에서 읽음 → **Lost Update 재발**

### 3.2 해결: Facade 패턴

```java
// ✅ 실제 구현 클래스 (application/collaboration/)
@Component
@RequiredArgsConstructor
public class CollaborationFacade {                      // 1. 락 + 멱등성 관리
    private final RedissonClient redissonClient;
    private final CollaborationCommandService commandService;
    private final IdempotencyRepository idempotencyRepository;

    public CollaborationResult linkOrIncrement(String idempotencyKey, CollaborationLinkCommand command) {
        // 멱등성 캐시 확인 (Redis HIT → 즉시 반환)
        Optional<String> cached = idempotencyRepository.find(idempotencyKey);
        if (cached.isPresent()) {
            return deserializeCached(idempotencyKey, buildPayloadHash(command), cached.get());
        }

        String lockKey = buildLockKey(command.fromMusicianId(), command.toMusicianId());
        RLock lock = redissonClient.getLock(lockKey);
        try {
            if (!lock.tryLock(2, 5, TimeUnit.SECONDS)) {
                throw new CollaborationConcurrentException(command.fromMusicianId(), command.toMusicianId());
            }
            CollaborationResult result = commandService.linkOrIncrement(command); // @Transactional → 커밋
            cacheResult(idempotencyKey, buildPayloadHash(command), result);
            return result;
        } finally {
            if (lock.isHeldByCurrentThread()) lock.unlock(); // 4. 락 해제 (트랜잭션 커밋 후)
        }
    }
}

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CollaborationCommandService {              // 2. @Transactional 비즈니스 로직
    @Transactional
    public CollaborationResult linkOrIncrement(CollaborationLinkCommand command) {
        // 락이 잡혀있는 상태 → 안전한 Read-Modify-Write
        long minId = Math.min(command.fromMusicianId(), command.toMusicianId());
        long maxId = Math.max(command.fromMusicianId(), command.toMusicianId());
        Optional<Collaboration> existing =
            collaborationRepository.findByFromAndToAndType(minId, maxId, command.relationType());
        if (existing.isPresent()) {
            return CollaborationResult.of(collaborationRepository.save(existing.get().incrementWeight()), false);
        }
        return CollaborationResult.of(
            collaborationRepository.save(Collaboration.newPair(minId, maxId, command.relationType())), true);
    }
}
```

**핵심 원칙**:
> **락의 범위 > 트랜잭션의 범위**
> 락을 잡고 → 트랜잭션 시작 → 로직 → 트랜잭션 커밋 → 락 해제
> 데이터 변경사항이 완전히 DB에 반영된 후, 다음 스레드가 진입한다.

---

## 4. Retry 전략 — Backoff & Jitter

### 4.1 왜 Jitter가 필요한가? (Thundering Herd 방지)

**Fixed Backoff의 문제**:
1000개 클라이언트가 동시에 실패 → 1초 후 1000개가 정확히 같은 시점에 재시도 → 서버가 다시 다운.

**Exponential Backoff + Full Jitter의 효과**:
```java
long backoff(int attempt) {
    long exp = Math.min(BASE_DELAY * (1L << attempt), MAX_DELAY);
    return ThreadLocalRandom.current().nextLong(0, exp);  // full jitter
}
```
- 재시도 시점이 무작위로 분산 → 동시 충돌 확률이 1/N으로 감소
- **Retry Storm이 수학적으로 불가능**

### 4.2 DAZZ 설정값

| 파라미터 | 값 | 근거 |
| --- | --- | --- |
| `maxAttempts` | **3** | 첫 시도 + 재시도 2회. 그 이상은 UX 한계 |
| `baseDelay` | **1000ms** | 너무 짧으면 transient 장애 회복 전 재시도 |
| `backoffMultiplier` | **2** | 표준 exponential (1s → 2s → 4s) |
| `jitter` | **Full Jitter** (`random(0, exp)`) | AWS 공식 권장. Equal jitter보다 분산 효과 큼 |
| `maxDelay` | **8000ms** | 8초 초과 시 cap. 무한 증가 방지 |

### 4.3 재시도 가능 vs 불가 예외

#### ✅ 재시도 권장
- HTTP 500/502/503/504
- `ConnectException`, `SocketTimeoutException` (단, 멱등성 보장 필요)

#### ❌ 재시도 금지
- HTTP 400/401/403/404/422
- `IllegalArgumentException` 등 클라이언트 버그
- 비즈니스 예외 (잔액 부족 등)

> **판단 원칙**: "동일 요청을 다시 했을 때 결과가 달라질 가능성이 있는가?"

---

## 5. 멱등성 (Idempotency) 보장

### 5.1 왜 필요한가?

네트워크 타임아웃 등으로 유저가 '협업 등록' 버튼을 여러 번 누름 → 같은 협업이 중복 등록되어 weight가 부풀려짐.

### 5.2 Idempotency-Key 전략

**클라이언트**: 요청마다 고유한 UUID를 Header에 포함
```
POST /api/v1/collaborations
Idempotency-Key: 550e8400-e29b-41d4-a716-446655440000
```

**서버**: Redis에 키-결과 매핑 저장
```
Key:   idempotency:collaboration:{key}
Value: { "status": 202, "body": { ... } }
TTL:   86400 (24시간)
```

**처리 흐름**:
```
1. Redis에서 key 조회
   ├─ HIT  → 캐시된 응답을 그대로 반환
   └─ MISS → 다음 단계
2. 비즈니스 로직 실행
3. 응답 결과를 Redis에 저장 (TTL 24h)
4. 응답 반환
```

### 5.3 TTL = 24시간 근거

- 재즈 협업 데이터 특성상 동일 데이터가 짧은 시간 내에 반복 생성될 확률이 적음
- 일 단위 중복 요청 방어로 데이터 무결성 충분히 확보
- Redis 용량/만료 부담 최소

### 5.4 동일 키 + 다른 페이로드 = 409 Conflict

```java
if (cachedKey.exists() && !cachedKey.payloadEquals(currentRequest)) {
    throw new IdempotencyConflictException(key);
    // → HTTP 409 IDEMPOTENCY_CONFLICT
}
```

---

## 6. 통합 시나리오 — 새 협업 등록 흐름

```
[1] Client → POST /api/v1/collaborations
            Idempotency-Key: {uuid}

[2] Controller
    └─→ idempotencyService.checkOrAcquire(key, payload)
        ├─ HIT  → 캐시된 응답 즉시 반환 (멱등성)
        └─ MISS → 진행

[3] CollaborationFacade.updateWeight()
    ├─ [Redisson Lock 획득] collab:lock:{min}:{max} (TTL 5s + Watchdog)
    │
    ├─ [@Transactional 시작]
    │   ├─ COLLABORATION 테이블 조회 또는 INSERT
    │   ├─ weight 증가
    │   ├─ Outbox 테이블에 'collaboration.created' 이벤트 적재
    │   └─ [트랜잭션 커밋]
    │
    └─ [Redisson Lock 해제]

[4] Outbox Poller → Kafka.publish("collaboration.created.v1")

[5] (병렬 Consumer)
    ├─ 네트워크 가중치 재계산
    ├─ Redis 캐시 무효화 (musician:insight:{aId}:*, {bId}:*)
    └─ 구독자 알림 발송 (FCM)

[6] Controller → 202 Accepted with collaborationId

[7] idempotencyService.cache(key, response, TTL=24h)
```

---

## 7. 모니터링 지표

| 지표 | 임계 알람 | 대응 |
| --- | --- | --- |
| 락 획득 대기 시간 (p99) | > 1초 | 핫스팟 분석 (특정 뮤지션 집중?) |
| 락 획득 실패율 | > 5% | Retry 폭주 가능성, Backoff 조정 |
| Idempotency 캐시 적중률 | < 1% | 클라이언트 재시도 로직 점검 (정상이라면 매우 낮아야 함) |
| Outbox 적재 후 발행 지연 | > 5초 | Poller 성능 점검 |

---

## 8. 안티 패턴 (절대 하지 말 것)

- ❌ `synchronized` 키워드만으로 동시성 제어 (Scale-out 환경에서 무력)
- ❌ Application 메모리 기반 락 (`ReentrantLock` 등)
- ❌ DB 비관적 락 (`SELECT ... FOR UPDATE`)을 광범위하게 사용 — 데드락 + 성능 저하 위험
- ❌ 트랜잭션 내부에 락 코드 작성
- ❌ TTL 없는 Redis 락 (좀비 락 직행)
- ❌ Idempotency-Key 없이 결제/등록 API 노출
