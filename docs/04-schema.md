# 04. Schema — Data Model & ERD

> 이 문서는 DAZZ의 **데이터 구조**와 그 **설계 근거**를 정의합니다.
> 스키마 변경은 반드시 이 문서를 먼저 갱신한 후 마이그레이션을 진행하세요.

---

## 1. 핵심 엔티티 한눈에

```
USER ──(1:1 Optional)── MUSICIAN ──┬── (1:N) ── COLLABORATION (self-ref via from/to)
                                   ├── (M:N via ALBUM_PARTICIPATION) ── ALBUM
                                   └── (M:N via PERFORMANCE_LINEUP) ── PERFORMANCE
                                                                       │
                                                                       └─ (N:1) VENUE

DOCENT_NOTE ──(N:1)── MUSICIAN
SUBSCRIPTION ──(M:N)── USER × MUSICIAN
```

---

## 2. 핵심 테이블 명세

### 2.1 `MUSICIAN` (뮤지션 — Aggregate Root)

| 컬럼 | 타입 | 제약 | 설명 |
| --- | --- | --- | --- |
| `id` | BIGINT PK | AUTO_INCREMENT | 내부 식별자 |
| `uuid` | CHAR(36) | UNIQUE NOT NULL | **EC-02 방지용 외부 식별자** |
| `user_id` | BIGINT FK | **UNIQUE, NULLABLE** | USER 1:1 Optional |
| `stage_name` | VARCHAR(100) | NOT NULL | 활동명 |
| `real_name` | VARCHAR(100) | NULLABLE | 본명 (선택 공개) |
| `primary_position` | VARCHAR(30) | NOT NULL | Vocal/Piano/Bass... |
| `birth_year` | SMALLINT | NULLABLE | 동명이인 구분용 |
| `bio` | TEXT | NULLABLE | 본인 작성 소개 |
| `profile_image_url` | VARCHAR(500) | NULLABLE | S3 URL |
| `verification_tier` | VARCHAR(20) | NOT NULL DEFAULT 'UNVERIFIED' | EC-01 신뢰등급 |
| `created_at`, `updated_at` | DATETIME | NOT NULL | 감사 |

**핵심 설계 결정**:
- `user_id`의 `UNIQUE + NULLABLE`: 시스템이 먼저 뮤지션 정보를 등록(Nullable)하고, 나중에 실제 본인이 나타나 USER와 연결 가능. 한 유저가 여러 뮤지션 권한 갖는 것 차단(UNIQUE).
- `uuid`: 협업 데이터 입력 시 동명이인 오염 방지의 핵심.

### 2.2 `USER` (서비스 사용자)

| 컬럼 | 타입 | 제약 | 설명 |
| --- | --- | --- | --- |
| `id` | BIGINT PK | AUTO_INCREMENT | |
| `email` | VARCHAR(255) | UNIQUE NOT NULL | 로그인 식별자 |
| `password_hash` | VARCHAR(255) | NOT NULL | BCrypt |
| `display_name` | VARCHAR(100) | NOT NULL | |
| `role` | VARCHAR(20) | NOT NULL DEFAULT 'USER' | USER / ADMIN |
| `email_verified_at` | DATETIME | NULLABLE | |
| `created_at`, `updated_at` | DATETIME | NOT NULL | |

### 2.3 `ALBUM` (앨범)

| 컬럼 | 타입 | 제약 | 설명 |
| --- | --- | --- | --- |
| `id` | BIGINT PK | AUTO_INCREMENT | |
| `title` | VARCHAR(255) | NOT NULL | |
| `release_date` | DATE | NULLABLE | |
| `cover_image_url` | VARCHAR(500) | NULLABLE | |
| `description` | TEXT | NULLABLE | |
| `genre_primary` | VARCHAR(50) | NULLABLE | 주 장르 |
| `created_at`, `updated_at` | DATETIME | NOT NULL | |

> ⚠️ `ALBUM`에 **`leader_id` 컬럼을 두지 않는다**. 모든 참여 관계는 `ALBUM_PARTICIPATION`을 거친다.

### 2.4 `ALBUM_PARTICIPATION` (앨범 참여 — 핵심 N:M 관계)

| 컬럼 | 타입 | 제약 | 설명 |
| --- | --- | --- | --- |
| `id` | BIGINT PK | AUTO_INCREMENT | |
| `album_id` | BIGINT FK | **NOT NULL** | 고아 데이터 방지 |
| `musician_id` | BIGINT FK | **NOT NULL** | 고아 데이터 방지 |
| `participation_type` | VARCHAR(30) | NOT NULL | LEADER / SIDEMAN / FEATURING / PRODUCER |
| `instrument` | VARCHAR(30) | NULLABLE | 연주 악기 (멀티 악기 대응) |
| `track_numbers` | VARCHAR(100) | NULLABLE | 참여 트랙 번호 (CSV 또는 JSON) |
| `created_at` | DATETIME | NOT NULL | |

**인덱스**:
- `idx_participation_musician_type`: `(musician_id, participation_type)` — Sideman 이력 조회 핵심
- `idx_participation_album`: `(album_id)` — 앨범 참여진 조회
- UNIQUE: `(album_id, musician_id, participation_type, instrument)` — 동일 역할 중복 방지

**왜 N:M을 별도 엔티티로?** → `/docs/02-architecture.md` 및 W1 유연성/DIP 관점:
- **결합도**: 새 역할(예: 빅밴드 컨덕터) 추가 시 `participation_type` 값만 추가 → 스키마 변경 없음
- **책임**: `ALBUM`은 "앨범 메타", `ALBUM_PARTICIPATION`은 "누가 어떤 역할" — 책임 분리

### 2.5 `COLLABORATION` (협업 관계 — Self-referencing)

| 컬럼 | 타입 | 제약 | 설명 |
| --- | --- | --- | --- |
| `id` | BIGINT PK | AUTO_INCREMENT | |
| `from_musician_id` | BIGINT FK | NOT NULL | 자가 참조 |
| `to_musician_id` | BIGINT FK | NOT NULL | 자가 참조 |
| `relation_type` | VARCHAR(30) | NOT NULL | COLLABORATION / MENTOR / BAND_MEMBER |
| `weight` | INT | NOT NULL DEFAULT 1 | 협업 횟수 (동시성 제어 대상) |
| `last_collaborated_at` | DATETIME | NULLABLE | 최근 협업 시점 |
| `created_at`, `updated_at` | DATETIME | NOT NULL | |

**제약**:
- `from_musician_id < to_musician_id` (정규화): 대칭 관계 중복 저장 방지 (또는 DB 트리거)
- UNIQUE: `(from_musician_id, to_musician_id, relation_type)`

**weight 컬럼이 동시성 제어의 핵심 대상** → `/docs/07-concurrency.md`

### 2.6 `PERFORMANCE` & `PERFORMANCE_LINEUP`

```
PERFORMANCE
  - id, venue_id (FK NOT NULL), performance_datetime, title, concept, ticket_price
  - genre_primary, description, created_at, updated_at

PERFORMANCE_LINEUP
  - id, performance_id (FK NOT NULL), musician_id (FK NOT NULL)
  - role (LEADER / SIDEMAN), instrument, created_at
  - UNIQUE(performance_id, musician_id, role)
```

> **`musician_id`가 NOT NULL인 이유**: "도슨트가 안내하는 길에 오류가 없도록" — 라인업 클릭 시 반드시 뮤지션 프로필로 연결 보장.

### 2.7 `VENUE` (공연장)

```
VENUE
  - id, name, address, latitude, longitude
  - operating_hours, has_jam_session (bool), jam_session_info
  - phone, sns_url, image_urls (JSON)
```

### 2.8 `DOCENT_NOTE` (도슨트 노트)

| 컬럼 | 타입 | 제약 | 설명 |
| --- | --- | --- | --- |
| `id` | BIGINT PK | AUTO_INCREMENT | |
| `musician_id` | BIGINT FK | NOT NULL | 또는 album_id (둘 중 하나만 NOT NULL 제약, CHECK) |
| `style_tags` | JSON | NULLABLE | ["#비밥", "#서정적"] |
| `summary` | TEXT | NOT NULL | 한 줄 요약 |
| `content` | TEXT | NOT NULL | 본문 |
| `author_id` | BIGINT FK | NOT NULL | 작성자 USER |
| `created_at`, `updated_at` | DATETIME | NOT NULL | |

### 2.9 `SUBSCRIPTION` (구독)

```
SUBSCRIPTION
  - id, user_id (FK NOT NULL), musician_id (FK NOT NULL)
  - notify_new_collaboration (bool), notify_new_performance (bool)
  - created_at
  - UNIQUE(user_id, musician_id)
```

---

## 3. 인덱스 전략

### 3.1 필수 인덱스 (P0)

| 테이블 | 인덱스 | 목적 |
| --- | --- | --- |
| `ALBUM_PARTICIPATION` | `(musician_id, participation_type)` | Sideman 이력 조회 |
| `ALBUM_PARTICIPATION` | `(album_id)` | 앨범 참여진 조회 |
| `COLLABORATION` | `(from_musician_id, weight DESC)` | 가중치 높은 협업 순 |
| `PERFORMANCE` | `(performance_datetime, venue_id)` | 공연 일정 조회 |
| `MUSICIAN` | `(uuid)` UNIQUE | 외부 식별자 조회 |
| `MUSICIAN` | `(stage_name)` | 이름 검색 (FULLTEXT 검토) |

### 3.2 검토 인덱스 (P1, 트래픽 확인 후)

- `DOCENT_NOTE`의 `style_tags` JSON 인덱스 (MySQL 8.0 Multi-Valued Index)
- `MUSICIAN.stage_name`의 FULLTEXT 인덱스 (한글 검색)

---

## 4. 반정규화 정책 (Denormalization)

**기본 원칙**: 정규화 우선. 성능 측정 후 필요한 경우에만 반정규화.

**예상 반정규화 후보** (P2, 측정 후 결정):
- `ALBUM_PARTICIPATION`에 `album_title`, `album_cover_url` 중복 저장 → 목록 조회 시 JOIN 제거
- `MUSICIAN`에 `total_collaboration_count` 집계 컬럼 → 매번 COUNT 회피
  - 단, **Kafka 이벤트로 갱신**하여 정합성 보장

---

## 5. 마이그레이션 도구

- **Flyway** 사용 (Liquibase 대비 SQL 직관성 우위)
- 파일명: `V{YYYYMMDDHHmm}__{설명}.sql` (예: `V202605170900__create_musician_table.sql`)
- 마이그레이션은 **절대 수정하지 않음**: 잘못된 경우 새 마이그레이션으로 롤백

---

## 6. 데이터 모델 진화 정책

1. 컬럼 추가: 새 마이그레이션, 기본값 명시 또는 Nullable
2. 컬럼 삭제: 2단계 (① 코드에서 사용 제거 배포 → ② 한 사이클 후 DROP)
3. 테이블 분리/통합: 도메인 이벤트 발행 + 데이터 마이그레이션 스크립트 + 한 사이클 병행 운영

---

## 7. 시드 데이터 정책

`infrastructure/db/seed/`에 다음 데이터 준비 (개발/테스트용):
- 서울예대 출신 대표 뮤지션 20명
- 협업 관계 50건
- 대표 공연장 10곳 (Once In A Blue Moon, All That Jazz, 디바야누스 등)
- 도슨트 노트 30건

이는 **개발 환경 한정**. 프로덕션에는 직접 운영자가 입력하거나 검증 후 임포트.
