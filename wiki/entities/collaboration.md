# Collaboration (협업 관계)

**Summary**: 두 뮤지션 사이의 협업 관계를 나타내는 Self-referencing 엔티티. weight(협업 횟수)가 관계도의 엣지 굵기를 결정한다.
**Tags**: #domain #entity #relationship-graph #concurrency
**Created**: 2026-05-19
**Last Updated**: 2026-06-05

---

## DB 구조

```sql
COLLABORATION
├── id                BIGINT PK
├── from_musician_id  BIGINT FK NOT NULL  ← 항상 작은 ID
├── to_musician_id    BIGINT FK NOT NULL  ← 항상 큰 ID
├── relation_type     VARCHAR(30)         ← COLLABORATION / MENTOR / BAND_MEMBER
├── weight            INT DEFAULT 1       ← 협업 횟수 (동시성 제어 대상)
├── last_collaborated_at  DATETIME
└── UNIQUE(from_musician_id, to_musician_id, relation_type)
```

---

## 정규화 규칙: from < to

A-B 협업과 B-A 협업은 **같은 행**으로 저장.
`from_musician_id < to_musician_id` 를 항상 보장.

```java
// 락 키도 같은 규칙 적용
private String lockKey(Long aId, Long bId) {
    long min = Math.min(aId, bId);
    long max = Math.max(aId, bId);
    return String.format("collab:lock:%d:%d", min, max);
}
```

이를 어기면 같은 관계가 두 행으로 저장되어 weight가 분산됨.

---

## relation_type 종류

| 값 | 의미 |
| --- | --- |
| `COLLABORATION` | 일반 협업 (세션, 공연 함께) |
| `MENTOR` | 사제 관계 |
| `BAND_MEMBER` | 고정 밴드 멤버 |

새 관계 유형 추가 시 `relation_type` 값만 추가. 스키마 변경 없음.

---

## weight 업데이트 시 주의사항

weight는 반드시 [[concepts/collaboration-weight]] 의 분산락 패턴으로만 수정.
단순 `weight++` 코드 작성 금지.

```
✅ CollaborationFacade.linkOrIncrement()
     → Redisson 락 획득
     → CollaborationCommandService.linkOrIncrement() @Transactional
     → Collaboration.incrementWeight() (불변 객체 → 새 인스턴스 반환)
     → repository.save()
     → 트랜잭션 커밋
     → 락 해제
❌ repository.findById().get().incrementWeight() // 락 없이 직접 수정
```

**구현 완료 API**: `POST /api/v1/collaborations` (Idempotency-Key 필수)

---

## 관계도에서의 역할

- `weight` 값이 높을수록 엣지가 굵게 표시
- `last_collaborated_at` 으로 최근 협업 여부 표시
- depth 파라미터로 1-hop, 2-hop 탐색 가능

캐시 키: `musician:insight:{musicianId}:depth={N}` (TTL 10분)
협업 등록 시 양쪽 뮤지션의 캐시 무효화 필요.

---

## 관련 페이지

- [[concepts/collaboration-weight]]
- [[entities/musician]]
- [[entities/redis]]
