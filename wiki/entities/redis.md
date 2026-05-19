# Redis

**Summary**: DAZZ에서 캐시와 분산락 두 가지 역할을 동시에 담당. Redisson 클라이언트 사용. NFR 0.5s 달성의 핵심 인프라.
**Tags**: #infrastructure #cache #distributed-lock #performance
**Created**: 2026-05-19
**Last Updated**: 2026-05-19

---

## 두 가지 역할

### 역할 1: 캐시 (Cache-Aside 패턴)

조회 API 응답을 캐싱해서 DB 직접 조회 없이 빠르게 반환.

```
요청 → Redis 조회
  ├─ HIT  → 캐시 데이터 반환 (목표: < 100ms)
  └─ MISS → DB 조회 → Redis 저장 → 반환
```

### 역할 2: 분산락 (Redisson Pub/Sub)

협업 가중치 업데이트 시 동시 접근 제어.
Spin Lock(Lettuce)이 아닌 **Pub/Sub 방식** → CPU/네트워크 효율 우수.

---

## 캐시 키 네이밍 규칙

```
musician:insight:{musicianId}:depth={N}    TTL 10분   ← 핵심 탐색 API
musician:profile:{musicianId}              TTL 1시간  ← 기본 프로필
collab:lock:{minId}:{maxId}               TTL 5초    ← 분산락 (락 전용)
idempotency:collaboration:{key}           TTL 24시간 ← 멱등성 키
```

**규칙**: 콜론(`:`)으로 계층 구분. 가장 앞이 도메인, 가장 뒤가 식별자.

---

## Watchdog (좀비 락 방지)

락 TTL이 만료되기 전에 비즈니스 로직이 끝나지 않으면
Redisson Watchdog이 자동으로 TTL 연장.

서버가 갑자기 죽으면 Watchdog도 죽으므로 TTL 만료 후 락 자동 해제.
→ 좀비 락(영원히 잠긴 락) 방지.

---

## TTL 설정 근거

| 키 | TTL | 근거 |
| --- | --- | --- |
| `musician:insight` | 10분 | 협업이 자주 바뀌지 않음. 10분이면 충분히 fresh |
| `musician:profile` | 1시간 | 프로필은 더 안정적. 긴 TTL로 DB 부하 감소 |
| `collab:lock` | 5초 | p99 Latency(0.5s) × 10배 안전 마진 |
| `idempotency` | 24시간 | 하루 이내 중복 요청 방어로 충분 |

---

## 캐시 무효화 시점

협업 등록 이벤트 발생 시 Kafka Consumer가 처리:
```
cache.invalidate.v1 이벤트 수신
→ musician:insight:{aId}:* 삭제
→ musician:insight:{bId}:* 삭제
```

두 뮤지션 모두 무효화해야 함. 한쪽만 하면 관계도가 불일치.

---

## 장애 시 동작

Redis 다운 시:
- 캐시 미스로 처리 → DB 직접 조회 → 서비스 지속
- 분산락 불가 → 쓰기 API (협업 등록) 실패 → 503 반환
- **읽기는 살고, 쓰기만 막힘** → 허용 가능한 장애 수준

---

## 관련 페이지

- [[concepts/collaboration-weight]]
- [[concepts/graceful-degradation]]
- [[entities/collaboration]]
