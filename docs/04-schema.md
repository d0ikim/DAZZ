# 04. Schema — Data Model & ERD (MVP)

> 이 문서는 DAZZ MVP의 **데이터 구조**와 그 **설계 근거**를 정의합니다.
> 스키마 변경은 반드시 이 문서를 먼저 갱신한 후 마이그레이션을 진행하세요.
> **기준**: ERD 이미지 (DAZZ_ERD.png) + 요구사항 필수 필드(EC-01, EC-02) 추가

---

## 1. 핵심 엔티티 한눈에

```
USER ──(1:1 Optional)── MUSICIAN ──┬── (1:N) ── COLLABORATION (self-ref via from/to)
                                   ├── (M:N via ALBUM_PARTICIPATION) ── ALBUM
                                   ├── (M:N via PERFORMANCE_LINEUP) ── PERFORMANCE ──(N:1)── CLUB
                                   └── (M:N via GROUP_MEMBER) ── GROUP
```

---

## 2. 테이블 명세

### 2.1 `USER` (서비스 사용자)

| 컬럼 | 타입 | 제약 | 설명 |
| --- | --- | --- | --- |
| `id` | BIGINT PK | AUTO_INCREMENT | |
| `email` | VARCHAR(255) | UNIQUE NOT NULL | 로그인 식별자 |
| `password` | VARCHAR(255) | NOT NULL | BCrypt 해시값 |
| `nickname` | VARCHAR(100) | NOT NULL | 표시 이름 |
| `role` | VARCHAR(20) | NOT NULL DEFAULT 'USER' | USER / ADMIN |
| `created_at` | DATETIME | NOT NULL | |

### 2.2 `MUSICIAN` (뮤지션 — Aggregate Root)

| 컬럼 | 타입 | 제약 | 설명 |
| --- | --- | --- | --- |
| `id` | BIGINT PK | AUTO_INCREMENT | 내부 식별자 |
| `uuid` | CHAR(36) | UNIQUE NOT NULL | **EC-02 방지용 외부 식별자** |
| `user_id` | BIGINT FK | UNIQUE, NULLABLE | USER 1:1 Optional |
| `stage_name` | VARCHAR(100) | NOT NULL | 활동명 |
| `real_name` | VARCHAR(100) | NULLABLE | 본명 (선택 공개) |
| `position` | VARCHAR(30) | NOT NULL | Vocal/Piano/Bass... |
| `bio` | TEXT | NULLABLE | 본인 작성 소개 |
| `sns_url` | VARCHAR(500) | NULLABLE | SNS 링크 |
| `profile_image_url` | VARCHAR(500) | NULLABLE | 이미지 URL |
| `verification_tier` | VARCHAR(20) | NOT NULL DEFAULT 'PUBLIC_PROFILE' | EC-01 신뢰등급 |
| `created_at` | DATETIME | NOT NULL | |

**핵심 설계 결정**:
- `uuid`: 협업 데이터 입력 시 동명이인 오염 방지의 핵심 (EC-02)
- `user_id` UNIQUE + NULLABLE: 시스템 선등록(NULL) → 본인 인계 시 연결. 한 유저가 여러 뮤지션 권한 갖는 것 차단 (EC-01)
- `verification_tier` 기본값 `PUBLIC_PROFILE`: 시스템 선등록 상태를 명확히 표현

**VerificationTier 값**:
```
PUBLIC_PROFILE  → 시스템 선등록, 본인 계정 미연결
UNVERIFIED      → 본인이 계정을 연결했으나 미인증
VERIFIED_USER   → 이메일/실명 인증 완료
VERIFIED_PRO    → 학력/활동 증빙 + 관리자 승인 완료
```

### 2.3 `COLLABORATION` (협업 관계 — Self-referencing)

| 컬럼 | 타입 | 제약 | 설명 |
| --- | --- | --- | --- |
| `id` | BIGINT PK | AUTO_INCREMENT | |
| `from_musician_id` | BIGINT FK | NOT NULL | Source 뮤지션 |
| `to_musician_id` | BIGINT FK | NOT NULL | Target 뮤지션 |
| `relation_type` | VARCHAR(30) | NOT NULL | COLLABORATION / MENTOR / BAND_MEMBER |
| `weight` | INT | NOT NULL DEFAULT 1 | 협업 횟수 (동시성 제어 대상) |

**제약**:
- UNIQUE: `(from_musician_id, to_musician_id, relation_type)`
- `weight` 컬럼이 동시성 제어의 핵심 대상 → `/docs/07-concurrency.md`

### 2.4 `ALBUM` (앨범)

| 컬럼 | 타입 | 제약 | 설명 |
| --- | --- | --- | --- |
| `id` | BIGINT PK | AUTO_INCREMENT | |
| `title` | VARCHAR(255) | NOT NULL | |
| `release_date` | DATE | NULLABLE | |
| `cover_image_url` | VARCHAR(500) | NULLABLE | |
| `album_review` | TEXT | NULLABLE | 곡 해석 및 해설 (도슨트 관점 코멘트) |

### 2.5 `ALBUM_PARTICIPATION` (앨범 참여 — N:M)

| 컬럼 | 타입 | 제약 | 설명 |
| --- | --- | --- | --- |
| `id` | BIGINT PK | AUTO_INCREMENT | |
| `album_id` | BIGINT FK | NOT NULL | |
| `musician_id` | BIGINT FK | NOT NULL | |
| `participation_type` | VARCHAR(30) | NOT NULL | LEADER / SIDEMAN / COMPOSER |

**인덱스**:
- `idx_participation_musician`: `(musician_id, participation_type)` — Sideman 이력 조회
- UNIQUE: `(album_id, musician_id, participation_type)` — 중복 방지

### 2.6 `GROUP` (밴드/팀)

| 컬럼 | 타입 | 제약 | 설명 |
| --- | --- | --- | --- |
| `id` | BIGINT PK | AUTO_INCREMENT | |
| `group_name` | VARCHAR(100) | NOT NULL | |
| `genre_tags` | VARCHAR(255) | NULLABLE | 콤마 구분 장르 태그 |
| `description` | TEXT | NULLABLE | |

### 2.7 `GROUP_MEMBER` (그룹-뮤지션 N:M)

| 컬럼 | 타입 | 제약 | 설명 |
| --- | --- | --- | --- |
| `id` | BIGINT PK | AUTO_INCREMENT | |
| `group_id` | BIGINT FK | NOT NULL | |
| `musician_id` | BIGINT FK | NOT NULL | |
| `role` | VARCHAR(50) | NULLABLE | 팀 내 역할 (e.g. 리더, 베이시스트) |

**인덱스**:
- UNIQUE: `(group_id, musician_id)` — 중복 방지

### 2.8 `CLUB` (공연장)

| 컬럼 | 타입 | 제약 | 설명 |
| --- | --- | --- | --- |
| `id` | BIGINT PK | AUTO_INCREMENT | |
| `name` | VARCHAR(100) | NOT NULL | |
| `location` | VARCHAR(255) | NULLABLE | 주소 |
| `instagram_url` | VARCHAR(500) | NULLABLE | |

### 2.9 `PERFORMANCE` (공연)

| 컬럼 | 타입 | 제약 | 설명 |
| --- | --- | --- | --- |
| `id` | BIGINT PK | AUTO_INCREMENT | |
| `club_id` | BIGINT FK | NOT NULL | |
| `start_time` | DATETIME | NOT NULL | 공연 시작 시각 |
| `title` | VARCHAR(255) | NOT NULL | |
| `genre` | VARCHAR(50) | NULLABLE | 공연 주 장르 |
| `set_list` | TEXT | NULLABLE | 곡 리스트 정보 |

**인덱스**:
- `idx_performance_club_time`: `(club_id, start_time)` — 클럽별 공연 일정 조회

### 2.10 `PERFORMANCE_LINEUP` (공연-뮤지션 N:M)

| 컬럼 | 타입 | 제약 | 설명 |
| --- | --- | --- | --- |
| `id` | BIGINT PK | AUTO_INCREMENT | |
| `performance_id` | BIGINT FK | NOT NULL | |
| `musician_id` | BIGINT FK | NOT NULL | 뮤지션 프로필과 직접 연결 |
| `set_info` | VARCHAR(100) | NULLABLE | "1부", "2부" 등 상세 |

> `musician_id` NOT NULL: 라인업 클릭 시 반드시 뮤지션 프로필로 연결 보장

---

## 3. MVP 제외 테이블 (Phase 2 이후)

| 테이블 | 이유 |
| --- | --- |
| `DOCENT_NOTE` | 뮤지션 데이터 충분히 쌓인 후 Verified User 기여 방식으로 구현 |
| `SUBSCRIPTION` | 알림 기능 Phase 2 |

---

## 4. 마이그레이션 도구

- **Flyway** 사용
- 파일명: `V{YYYYMMDDHHmm}__{설명}.sql` (예: `V202605220900__create_musician_table.sql`)
- 마이그레이션은 **절대 수정하지 않음**: 잘못된 경우 새 마이그레이션으로 수정

---

## 5. 데이터 모델 진화 정책

1. 컬럼 추가: 새 마이그레이션, 기본값 명시 또는 Nullable
2. 컬럼 삭제: 2단계 (① 코드에서 사용 제거 배포 → ② 한 사이클 후 DROP)
3. 테이블 분리/통합: 도메인 이벤트 발행 + 데이터 마이그레이션 스크립트
