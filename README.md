# DAZZ Documentation Index

> 이 폴더는 `CLAUDE.md`(루트 행동강령)에서 위임된 **상세 문서들**의 모음입니다.
> 작업 유형에 따라 아래 매트릭스를 보고 필요한 문서로 진입하세요.

---

## 📚 문서 목록

| # | 파일 | 다루는 내용 | 누가 / 언제 읽나 |
| --- | --- | --- | --- |
| 00 | [`00-overview.md`](docs/00-overview.md) | 컨셉, 비즈니스 맥락, Non-Goals | **모든 작업의 시작점** |
| 01 | [`01-requirements.md`](docs/01-requirements.md) | User Stories, NFR, Edge Cases | 새 기능 설계 전 |
| 02 | [`02-architecture.md`](docs/02-architecture.md) | Hexagonal 구조, 패키지, 의존성 규칙 | 모듈/계층 설계 |
| 03 | [`03-tech-stack.md`](docs/03-tech-stack.md) | 기술 선정 근거, 트레이드오프 | 새 라이브러리 도입 검토 |
| 04 | [`04-schema.md`](docs/04-schema.md) | ERD, 테이블/인덱스/마이그레이션 | DB 작업 시 |
| 05 | [`05-api-spec.md`](docs/05-api-spec.md) | 엔드포인트, 응답 포맷, 에러 코드 | API 추가/수정 시 |
| 06 | [`06-conventions.md`](docs/06-conventions.md) | 네이밍, 패키지, Anti-Patterns | **모든 코드 작성 시** |
| 07 | [`07-concurrency.md`](docs/07-concurrency.md) | 분산락, 멱등성, Backoff/Jitter | 쓰기 API 작성 시 |
| 08 | [`08-fault-tolerance.md`](docs/08-fault-tolerance.md) | Circuit Breaker, Retry, Fallback | 외부 API 호출 시 |

---

## 🗺️ 작업 시나리오별 진입 경로

### 시나리오 1: 새 기능을 개발한다
1. `00-overview.md` Section 6 (컨셉 정합성 체크리스트) 통과 확인
2. `01-requirements.md`에서 대응되는 User Story 확인
3. `02-architecture.md`에서 영향받는 Bounded Context 식별
4. `06-conventions.md`의 규약대로 구현
5. `05-api-spec.md`에 신규 API 문서화

### 시나리오 2: DB 스키마를 변경한다
1. `04-schema.md`에서 영향 범위 파악
2. 변경안을 문서에 먼저 반영
3. Flyway 마이그레이션 작성 (`V{timestamp}__{설명}.sql`)

### 시나리오 3: 외부 API를 호출하는 코드를 추가한다
1. `08-fault-tolerance.md`의 데코레이터 합성 패턴 적용 필수
   - `@Retry` + `@CircuitBreaker` + `@TimeLimiter`
2. Fallback 메서드 구현
3. `application.yml`에 새 CB 인스턴스 등록 (영향도별 차등)

### 시나리오 4: 쓰기 API를 작성한다 (특히 카운터/가중치)
1. `07-concurrency.md`의 Facade 패턴 적용
   - 락 → 트랜잭션 → 커밋 → 락 해제 순서 엄수
2. `Idempotency-Key` 헤더 처리
3. Outbox 패턴으로 이벤트 발행

### 시나리오 5: 컨셉과 충돌하는 듯한 요구사항을 받았다
1. `00-overview.md` Section 7 (Non-Goals) 재확인
2. Section 6 (컨셉 정합성 체크리스트) 적용
3. 충돌이 확실하면 사용자에게 **질문** (`CLAUDE.md` Section 7)

---

## 🔄 문서 갱신 정책

- 코드 변경이 본 문서들의 규약과 다르다면, **코드를 고치거나 문서를 먼저 갱신**한다 (둘 중 하나)
- "현재 코드와 문서가 다르다"는 상태로 머지하지 않는다
- 큰 설계 변경 시 PR 본문에 **어떤 문서를 갱신했는지** 명시

---

## 🧭 빠른 참조 — 자주 찾는 항목

| 찾고 싶은 것 | 위치 |
| --- | --- |
| 슬로건 / 컨셉 한 줄 정의 | `00-overview.md` Section 1 |
| 신뢰성 등급(Tier) 정의 | `01-requirements.md` Section 4 |
| 패키지 구조 트리 | `02-architecture.md` Section 2 |
| 왜 Kafka인가? | `03-tech-stack.md` Section 5 |
| Idempotency-Key 처리 흐름 | `07-concurrency.md` Section 5 |
| 3단계 Fallback (L1/L2/L3) | `08-fault-tolerance.md` Section 6 |
| 절대 하지 말 것 (Anti-Pattern) | `06-conventions.md` Section 10 |
| 표준 에러 코드 | `05-api-spec.md` Section 2 |
| 핵심 캐시 키 네이밍 | `03-tech-stack.md` Section 4 |
