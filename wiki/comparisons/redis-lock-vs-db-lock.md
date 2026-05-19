# Redis 분산락 vs DB 비관적 락

**Summary**: 협업 가중치 업데이트에 Redis 분산락(Redisson) 채택. DB 비관적 락은 데드락 위험과 성능 저하로 탈락.
**Tags**: #concurrency #redis #database #architecture-decision
**Created**: 2026-05-19
**Last Updated**: 2026-05-19

---

## 결론

**Redis 분산락(Redisson Pub/Sub) 채택.**

---

## 항목별 비교

| 항목 | DB 비관적 락 (`SELECT FOR UPDATE`) | **Redis 분산락 (Redisson)** |
| --- | --- | --- |
| 동작 방식 | DB 행 자체를 잠금 | Redis에 별도 락 키 생성 |
| 데드락 위험 | **높음** (두 트랜잭션이 서로 대기) | 낮음 (TTL로 자동 해제) |
| 성능 | DB 커넥션 점유 | DB 커넥션과 분리 |
| Scale-out | 단일 DB에서만 유효 | **여러 서버 인스턴스 간 공유** |
| 좀비 락 방지 | 트랜잭션 롤백 시 자동 해제 | **TTL + Watchdog** |
| 대기 방식 | Spin (반복 재시도) | **Pub/Sub (신호 대기)** |
| CPU 부하 | 높음 | **낮음** |

---

## DB 비관적 락의 문제점

### 데드락 시나리오
```
스레드 A: lock(musician 1), lock(musician 2) 시도
스레드 B: lock(musician 2), lock(musician 1) 시도
→ A는 2를 기다리고, B는 1을 기다림 → 영원히 대기
```

DAZZ 관계도에서 동시 다수 협업 등록 시 발생 가능.

### Scale-out 불가
서버 인스턴스가 2개 이상이면 각 인스턴스의 DB 락이 서로를 모름.

---

## Redis 분산락의 선택 이유

### Pub/Sub 방식 (Spin Lock과의 차이)

**Spin Lock (Lettuce)**:
```
락 획득 실패 → 10ms 대기 → 재시도 → 10ms 대기 → 재시도 ...
→ Redis에 반복 요청 → CPU/네트워크 낭비
```

**Pub/Sub (Redisson)**:
```
락 획득 실패 → Redis 채널 구독 → 락 해제 신호 수신 → 획득 시도
→ 신호 올 때만 깨어남 → 효율적
```

NFR 0.5s를 지키려면 락 대기 중에도 CPU를 낭비하면 안 된다.

---

## 실제 락 키와 TTL

```
키: collab:lock:{min(aId, bId)}:{max(aId, bId)}
TTL: 5초 (= p99 Latency 0.5s × 10배 안전 마진)
Watchdog: 활성화 (로직이 길어지면 TTL 자동 연장)
```

---

## 언제 DB 락이 더 나은가 (참고)

아주 단순한 단일 행 업데이트이고 Scale-out이 없는 경우.
DAZZ는 Scale-out을 NFR로 요구하므로 해당 없음.

---

## 관련 페이지

- [[concepts/collaboration-weight]]
- [[entities/redis]]
- [[entities/collaboration]]
