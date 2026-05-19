# Collaboration Weight (협업 가중치)

**Summary**: 두 뮤지션이 함께 연주한 횟수. 관계도의 엣지 굵기를 결정하며, 동시 업데이트 시 Lost Update가 발생하므로 반드시 분산락으로 보호해야 한다.
**Tags**: #domain #concurrency #redis #relationship-graph
**Created**: 2026-05-19
**Last Updated**: 2026-05-19

---

## 정의

```
COLLABORATION.weight = 두 뮤지션이 함께 연주한 총 횟수
```

- 새 협업이 등록될 때마다 `weight += 1`
- 관계도에서 엣지의 굵기/강도를 결정하는 핵심 수치
- `from_musician_id < to_musician_id` 정규화로 대칭 중복 저장 방지

---

## 왜 단순 +1이 안 되는가 (동시성 문제)

`weight++`은 겉으로는 한 줄이지만 CPU 관점에서 3단계다:

```
1. DB에서 weight 값을 읽음   (Read)
2. +1 연산                   (Modify)
3. DB에 다시 씀              (Write)
```

**Lost Update 시나리오**:
1. 스레드 A가 weight=5 읽음
2. 스레드 B가 weight=5 읽음 (A가 아직 안 씀)
3. 스레드 A가 weight=6 씀
4. 스레드 B가 weight=6 씀 ← A의 결과를 덮어씀

결과: 2번 등록했는데 weight는 1만 증가. **관계도 데이터 오염**.

MySQL Repeatable Read 격리수준만으로는 이 문제를 막을 수 없다.

---

## 해결: Redisson 분산락 + Facade 패턴

### 락 키 규칙
```
collab:lock:{min(aId, bId)}:{max(aId, bId)}
```
항상 작은 ID가 앞에 와서 (A→B)와 (B→A)가 같은 락을 잡도록 보장.

### 코드 구조 (절대 원칙)
```
Facade (락 관리)
  └── 락 획득
        └── Service.updateWeightTransactional() ← @Transactional
              └── Read → Modify → Write
        └── 트랜잭션 커밋
  └── 락 해제
```

**락이 트랜잭션을 감싸야 한다. 반대 순서 절대 금지.**

이유: 트랜잭션 안에서 락을 잡으면, 락 해제 시점에 트랜잭션이 아직 커밋 안 된 상태일 수 있어 다음 스레드가 이전 값을 읽는다.

### TTL 설정
```
TTL = p99 Latency × 10 = 0.5s × 10 = 5초
```
Watchdog으로 로직이 길어지면 자동 연장.

---

## 멱등성 보장

같은 협업을 네트워크 오류로 두 번 등록하면 weight가 2 증가해버린다.
→ `Idempotency-Key` 헤더로 방어.

```
POST /api/v1/collaborations
Idempotency-Key: {UUID}   ← 필수
```

동일 키 재요청 시 Redis에서 캐시된 응답을 그대로 반환. weight 중복 증가 없음.

---

## 관련 페이지

- [[entities/collaboration]]
- [[entities/redis]]
- [[decisions/mvp-option-b]]
