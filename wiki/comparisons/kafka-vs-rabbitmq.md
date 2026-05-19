# Kafka vs RabbitMQ

**Summary**: DAZZ는 Fan-out + 메시지 영속성 + Replay 필요성으로 Kafka를 선택. RabbitMQ는 운영 단순성에서 앞서지만 핵심 요구사항을 충족하지 못한다.
**Tags**: #messaging #kafka #architecture-decision
**Created**: 2026-05-19
**Last Updated**: 2026-05-19

---

## 결론 (바로 보기)

**Kafka 채택. 번복 불가.**

근거: 새 협업 등록 하나가 4개의 후속 작업을 동시에 트리거해야 하고,
알림 서버 다운 시 이벤트가 유실되면 안 된다.

---

## 트리거 시나리오 (왜 단순 큐로는 부족한가)

`collaboration.created` 이벤트 하나가 발생하면:

```
┌─────────────────────────────────┐
│  collaboration.created 이벤트   │
└──────────────┬──────────────────┘
               │ Fan-out
       ┌───────┼───────┬──────────┐
       ▼       ▼       ▼          ▼
  가중치    캐시    구독자      통계
  재계산  무효화   알림(FCM)   적재
```

RabbitMQ는 Exchange 설정으로 Fan-out 가능하지만,
**소비된 메시지는 삭제**된다. 알림 서버가 다운된 순간의 이벤트는 영영 유실.

---

## 항목별 비교

| 항목 | RabbitMQ | **Kafka** | DAZZ 판단 |
| --- | --- | --- | --- |
| 메시지 영속성 | 소비 후 삭제 | 디스크 보존 (Retention) | **Kafka** — 알림 유실 불가 |
| Replay | 불가 | offset 조정으로 가능 | **Kafka** — 장애 복구 필수 |
| Fan-out | Exchange 설정 | Consumer Group으로 자연스럽게 | **Kafka** — 컨슈머 추가가 더 단순 |
| 운영 복잡도 | 낮음 | 높음 (KRaft, 파티션 관리) | RabbitMQ 우위 — 감수함 |
| 처리량 | 중간 | 매우 높음 | **Kafka** |
| 라우팅 유연성 | 강함 (Topic Exchange) | 단순 (토픽 기반) | RabbitMQ 우위 — 불필요 |
| 신규 컨슈머 추가 | 코드 + Exchange 설정 | **컨슈머만 붙이면 끝** | **Kafka** |

---

## Kafka 선택의 핵심 근거

### 1. 장애 복구력 (Resilience)
알림 서버 일시 다운 시:
- RabbitMQ → 그 순간 이벤트 **영영 유실**
- Kafka → 디스크에 보관 → 서버 복구 후 **Replay**로 전송

### 2. 확장성 (Zero Core Code Change)
새 기능 추가 시 (예: 검색 인덱싱, 분석용 데이터 적재):
- RabbitMQ → Exchange 설정 변경 + 핵심 코드 수정
- Kafka → **컨슈머 하나만 추가**, 핵심 도메인 코드 0줄 수정

---

## 토픽 설계

```
collaboration.created.v1         # 새 협업 등록
collaboration.weight.recalc.v1   # 가중치 재계산 요청
cache.invalidate.v1              # 캐시 무효화 신호
notification.dispatch.v1         # 알림 발송 요청
```

- 토픽명 끝 `.v{N}`: 스키마 변경 시 신구 버전 병행 운영
- 파티션 키: `musicianId` 기준 (같은 뮤지션 이벤트 순서 보장)

---

## 감수하는 비용

- 인프라 운영 오버헤드 (KRaft 설정, 파티션 관리)
- 학습 곡선
- 로컬 개발 환경 무거워짐 (docker-compose에 Kafka 포함)

→ "전략적 투자"로 정당화. NFR(0.5s, 99.9%)을 지키려면 비동기 처리가 필수이고,
그 비동기 처리의 신뢰성을 보장하려면 Kafka가 유일한 선택지다.

---

## 관련 페이지

- [[entities/kafka]]
- [[concepts/graceful-degradation]]
- [[comparisons/mysql-vs-mongodb]]
