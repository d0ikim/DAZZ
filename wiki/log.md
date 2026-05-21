커밋# DAZZ LLM Wiki — Change Log

**규칙**:
- 최신 항목이 맨 위에 온다
- 형식: `YYYY-MM-DD — [action] — [대상 파일] — [한 줄 이유]`
- action 종류: `created` / `updated` / `deleted` / `merged`
- Wiki 페이지를 추가하거나 수정할 때마다 반드시 여기 기록한다

---

## 2026-05

- 2026-05-21 — updated — `wiki/concepts/hexagonal-architecture.md` — 로컬 dev 환경 포트 확정 (MySQL 3307, Redis 6379, Kafka 9092) 및 Spring Security 기본 동작 메모 추가
- 2026-05-21 — updated — `wiki/entities/kafka.md` — Spring Boot 3.4.x에서 spring-boot-starter-kafka 대신 spring-kafka 직접 사용으로 변경 반영
- 2026-05-19 — created — `wiki/decisions/no-docent-note-in-mvp.md` — MVP에서 도슨트 노트 미구현 결정 및 Post-MVP 조건 정리
- 2026-05-19 — created — `wiki/decisions/monorepo.md` — backend + frontend 모노레포 구성 결정 근거
- 2026-05-19 — created — `wiki/decisions/mvp-option-b.md` — 뮤지션 셀프온보딩 MVP 전략 채택 근거
- 2026-05-19 — created — `wiki/comparisons/redis-lock-vs-db-lock.md` — Redisson Pub/Sub 분산락 채택, DB 비관적 락 탈락 근거
- 2026-05-19 — created — `wiki/comparisons/mysql-vs-mongodb.md` — 참조 무결성 필수로 MySQL 채택, MongoDB 탈락 근거
- 2026-05-19 — created — `wiki/entities/user.md` — User↔Musician 1:1 Optional 관계, JWT 인증, role vs trust-tier 구분
- 2026-05-19 — created — `wiki/entities/kafka.md` — Kafka 토픽 목록, Outbox 패턴, 파티션 키 전략
- 2026-05-19 — created — `wiki/entities/redis.md` — Redis 이중 역할(캐시+락), 키 명명 규칙, TTL 근거
- 2026-05-19 — created — `wiki/entities/collaboration.md` — 자기참조 협업 엔티티, from<to 정규화, 가중치 업데이트 규칙
- 2026-05-19 — created — `wiki/concepts/graceful-degradation.md` — 3단계 폴백(L1/L2/L3), 항상 HTTP 200 반환 전략
- 2026-05-19 — created — `wiki/concepts/docent-note.md` — 도슨트 노트 개념 정의, MVP 미구현 이유
- 2026-05-19 — created — `wiki/concepts/hexagonal-architecture.md` — 헥사고날 아키텍처 레이어 규칙, Port/Adapter 패턴, 패키지 구조
- 2026-05-19 — created — `wiki/comparisons/kafka-vs-rabbitmq.md` — Fan-out + Replay 필요로 Kafka 선택한 근거
- 2026-05-19 — created — `wiki/entities/musician.md` — Musician Aggregate Root 구조, uuid/id 이중 식별자, 도메인 규칙
- 2026-05-19 — created — `wiki/concepts/trust-tier.md` — 사용자 신뢰 등급 4단계 정의 및 권한 매트릭스
- 2026-05-19 — created — `wiki/concepts/collaboration-weight.md` — 협업 가중치 정의, 동시성 문제, 분산락 패턴
- 2026-05-19 — created — `wiki/concepts/cold-start.md` — 콜드스타트 문제 정의 및 Option B 채택 근거
- 2026-05-19 — created — `wiki/index.md` — LLM Wiki 전체 진입점 및 카탈로그 초기 생성
- 2026-05-19 — created — `wiki/log.md` — 변경 이력 추적 파일 초기 생성
