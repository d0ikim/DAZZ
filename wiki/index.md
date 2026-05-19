# DAZZ LLM Wiki — Index

**목적**: AI(Claude)가 DAZZ 프로젝트의 지식을 탐색하기 위한 진입점.
**규칙**: 새 wiki 페이지를 만들면 반드시 이 파일에 한 줄 등록한다.
**탐색 방법**: 질문과 관련된 항목을 찾아 해당 파일을 읽는다.

---

## Concepts (핵심 개념)

| 파일 | 한 줄 요약 |
| --- | --- |
| [[concepts/hexagonal-architecture]] | 왜 Hexagonal인가, 계층별 의존성 규칙 |
| [[concepts/docent-note]] | 도슨트 노트란 무엇이고 누가 왜 쓰는가 |
| [[concepts/collaboration-weight]] | 협업 가중치의 정의, 동시성 문제, 분산락 |
| [[concepts/cold-start]] | 초기 데이터 문제와 MVP Option B 해결 전략 |
| [[concepts/trust-tier]] | 사용자 신뢰 등급 4단계와 권한 매트릭스 |
| [[concepts/graceful-degradation]] | 3단계 Fallback 전략, 장애 시에도 컨셉 유지 |

## Entities (등장 인물 / 시스템)

| 파일 | 한 줄 요약 |
| --- | --- |
| [[entities/musician]] | Musician Aggregate Root 구조와 비즈니스 규칙 |
| [[entities/collaboration]] | Collaboration Self-referencing 관계와 weight |
| [[entities/redis]] | 캐시 + 분산락 이중 역할, 키 네이밍 규칙 |
| [[entities/kafka]] | 이벤트 토픽 설계, RabbitMQ 대신 선택한 이유 |
| [[entities/user]] | User ↔ Musician 1:1 Optional 관계 |

## Comparisons (기술 선택 비교)

| 파일 | 한 줄 요약 |
| --- | --- |
| [[comparisons/kafka-vs-rabbitmq]] | Fan-out + Replay 필요 → Kafka 선택 |
| [[comparisons/mysql-vs-mongodb]] | 관계 정합성 필수 → MySQL 선택 |
| [[comparisons/redis-lock-vs-db-lock]] | Redisson Pub/Sub vs SELECT FOR UPDATE |

## Decisions (결정 로그)

| 파일 | 한 줄 요약 |
| --- | --- |
| [[decisions/mvp-option-b]] | 뮤지션 셀프온보딩 퍼스트 선택 이유 |
| [[decisions/monorepo]] | backend + frontend 같은 레포 구성 이유 |
| [[decisions/no-docent-note-in-mvp]] | MVP에서 도슨트 노트 미구현 이유 |

---

## Log

최신 항목이 위에 온다. 형식: `YYYY-MM-DD — [action] — [대상]`

- 2026-05-19 — created — wiki 초기 구조 및 index 생성
