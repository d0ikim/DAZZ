# Kafka

**Summary**: DAZZ의 비동기 이벤트 버스. 협업 등록 이후 가중치 재계산/캐시 무효화/알림 발송을 Fan-out 처리. 메시지 영속성과 Replay가 핵심 채택 이유.
**Tags**: #infrastructure #messaging #event #async
**Created**: 2026-05-19
**Last Updated**: 2026-05-19

---

## 토픽 목록

| 토픽 | 발행자 | 소비자 | 설명 |
| --- | --- | --- | --- |
| `collaboration.created.v1` | Outbox Poller | 가중치, 캐시, 알림 Consumer | 새 협업 등록 |
| `collaboration.weight.recalc.v1` | 가중치 Consumer | 내부 | 가중치 재계산 요청 |
| `cache.invalidate.v1` | 캐시 Consumer | Redis | 캐시 무효화 신호 |
| `notification.dispatch.v1` | 알림 Consumer | FCM | 구독자 알림 발송 |

토픽명 끝 `.v{N}` = 스키마 버전. 변경 시 신구 병행 운영.

---

## Outbox 패턴 (이벤트 유실 방지)

협업 등록 시 Kafka에 직접 발행하지 않음.
**DB 트랜잭션 내에 Outbox 테이블에 적재 → 별도 Poller가 Kafka 발행**.

```
[트랜잭션]
  1. COLLABORATION 저장
  2. OUTBOX 테이블에 이벤트 적재   ← DB와 함께 ACID 보장
[트랜잭션 커밋]

[Outbox Poller - 별도 스레드]
  3. OUTBOX 테이블 폴링
  4. Kafka 발행
  5. OUTBOX 행 처리 완료 표시
```

**왜 필요한가**: 트랜잭션 커밋 전에 Kafka 발행하면 DB 롤백 시 이벤트만 나가는 불일치 발생.

---

## 파티션 키 규칙

파티션 키: `musicianId` (from_musician_id의 min 값)

같은 뮤지션 관련 이벤트가 같은 파티션 → **순서 보장**.
다른 뮤지션 이벤트는 다른 파티션 → **병렬 처리**.

---

## Consumer 목록 (MVP)

| Consumer 클래스 | 토픽 | 하는 일 |
| --- | --- | --- |
| `CollaborationWeightConsumer` | `collaboration.created.v1` | 관계 가중치 재계산 |
| `CacheInvalidationConsumer` | `cache.invalidate.v1` | Redis 캐시 무효화 |
| `FcmNotificationAdapter` | `notification.dispatch.v1` | FCM 알림 발송 (Phase 2) |

---

## 신규 기능 추가 방법

새 Consumer만 붙이면 됨. **핵심 도메인 코드 0줄 수정.**

```
예: 검색 인덱싱 기능 추가
→ SearchIndexingConsumer 생성
→ collaboration.created.v1 구독
→ 끝
```

이것이 RabbitMQ 대신 Kafka를 선택한 핵심 이유. → [[comparisons/kafka-vs-rabbitmq]]

---

## 관련 페이지

- [[comparisons/kafka-vs-rabbitmq]]
- [[entities/redis]]
- [[concepts/graceful-degradation]]