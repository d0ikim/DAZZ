# 05. API Specification

> 모든 API는 본 문서의 **공통 응답 포맷**과 **에러 처리 규약**을 준수합니다.
> 새 엔드포인트 추가 시 본 문서를 먼저 갱신하고 구현에 들어가세요.

---

## 1. 공통 규약

### 1.1 Base URL
```
Local      : http://localhost:8080
Staging    : https://api-stg.dazz.kr
Production : https://api.dazz.kr
```

### 1.2 버저닝
- 경로 기반: `/api/v1/...`
- 호환성 깨지는 변경 시 `v2` 발급, 1 마이너 사이클 병행 운영

### 1.3 인증
- **JWT Bearer Token** (`Authorization: Bearer {token}`)
- 발급: `POST /auth/login`
- 만료: Access 30분 / Refresh 14일

### 1.4 공통 응답 포맷

**성공 응답**
```json
{
  "success": true,
  "data": { ... },
  "meta": {
    "timestamp": "2026-05-17T00:00:00Z",
    "requestId": "uuid-v4"
  }
}
```

**Degradation 응답 (장애 우회 시)** — `/docs/08-fault-tolerance.md` 참조
```json
{
  "success": true,
  "data": { ... },
  "meta": {
    "timestamp": "2026-05-17T00:00:00Z",
    "requestId": "uuid-v4",
    "dataSource": "cache",
    "cachedAt": "2026-05-17T00:00:00Z",
    "isStale": true,
    "degradation": "L1_BEST_EFFORT | L2_REDUCED | L3_MINIMAL",
    "unavailableFields": ["network"],
    "retryAfter": 30
  }
}
```

**에러 응답**
```json
{
  "success": false,
  "error": {
    "code": "MUSICIAN_NOT_FOUND",
    "message": "해당 뮤지션을 찾을 수 없습니다.",
    "details": { "musicianId": 102 }
  },
  "meta": {
    "timestamp": "2026-05-17T00:00:00Z",
    "requestId": "uuid-v4"
  }
}
```

### 1.5 HTTP 상태 코드 정책

| 상황 | 코드 |
| --- | --- |
| 성공 (단순 조회) | 200 |
| 생성 성공 | 201 |
| 비동기 접수 | 202 |
| 부분 응답 + Fallback | **200** (5xx 금지 — 본문 `meta.degradation`으로 신호) |
| 검증 실패 | 400 |
| 인증 실패 | 401 |
| 권한 없음 | 403 |
| 리소스 없음 | 404 |
| 중복 (멱등성 충돌) | 409 |
| 비즈니스 규칙 위반 | 422 |
| 요청 과다 | 429 |
| 서버 오류 (회복 불능) | 500 |

> 핵심 원칙: **부분 데이터라도 200을 유지**. 5xx는 클라이언트가 빈 화면을 띄울 수밖에 없음.

### 1.6 멱등성

쓰기 API는 `Idempotency-Key` 헤더를 **권장**, 결제/협업 등록 등 중복 위험이 큰 API는 **필수**.
- 키 저장소: Redis (`idempotency:{endpoint}:{key}`, TTL 24h)
- 동일 키 재요청 시 캐시된 결과 그대로 반환

---

## 2. 표준 에러 코드

> 에러 코드는 `ErrorCode.java` enum과 **항상 동기화**한다. 새 코드 추가 시 이 표도 갱신.

### 공통 (COM)

| 코드 | 내부코드 | HTTP | 의미 |
| --- | --- | --- | --- |
| `INVALID_INPUT` | COM001 | 400 | 요청 파라미터 검증 실패 / 필수 필드 누락 |
| `IDEMPOTENCY_CONFLICT` | COM002 | 409 | 동일 Idempotency-Key + 다른 페이로드 재요청 |
| `INTERNAL_SERVER_ERROR` | COM999 | 500 | 서버 내부 오류 |

### 뮤지션 (M)

| 코드 | 내부코드 | HTTP | 의미 |
| --- | --- | --- | --- |
| `MUSICIAN_NOT_FOUND` | M001 | 404 | uuid/id에 해당하는 뮤지션 없음 |
| `MUSICIAN_ALREADY_CLAIMED` | M002 | 409 | 이미 본인 계정이 연결된 뮤지션에 재claim 시도 |
| `MUSICIAN_USER_ALREADY_LINKED` | M003 | 409 | 해당 User가 이미 다른 뮤지션 프로필과 연결됨 (EC-01) |
| `MUSICIAN_CLAIM_CONFLICT` | M004 | 409 | 동시 claim 요청 충돌 — 잠시 후 재시도 |
| `MUSICIAN_NOT_APPROVABLE` | M005 | 409 | 승인 불가 — UNVERIFIED 상태가 아닌 뮤지션에 approve 시도 (EC-01) |
| `MUSICIAN_NOT_REJECTABLE` | M006 | 409 | 거절 불가 — UNVERIFIED 상태가 아닌 뮤지션에 reject 시도 (EC-01) |

### 앨범 (A)

| 코드 | 내부코드 | HTTP | 의미 |
| --- | --- | --- | --- |
| `ALBUM_NOT_FOUND` | A001 | 404 | 앨범 없음 |
| `ALBUM_PARTICIPATION_DUPLICATE` | A002 | 409 | 동일 (album, musician, type) 조합 중복 등록 |

### 협업 (C)

| 코드 | 내부코드 | HTTP | 의미 |
| --- | --- | --- | --- |
| `COLLABORATION_DUPLICATE` | C001 | 409 | 동일 (from, to, type) 협업 관계 중복 |
| `COLLABORATION_SELF_REFERENCE` | C002 | 400 | 자기 자신과의 협업 등록 시도 |
| `COLLABORATION_CONCURRENT` | C003 | 409 | 동시 요청 충돌 — 분산락 획득 실패 (잠시 후 재시도) |

### 그룹 (G)

| 코드 | 내부코드 | HTTP | 의미 |
| --- | --- | --- | --- |
| `GROUP_NOT_FOUND` | G001 | 404 | 그룹 없음 |
| `GROUP_MEMBER_DUPLICATE` | G002 | 409 | 이미 해당 그룹의 멤버 |

### 공연 (P)

| 코드 | 내부코드 | HTTP | 의미 |
| --- | --- | --- | --- |
| `PERFORMANCE_NOT_FOUND` | P001 | 404 | 공연 없음 |
| `CLUB_NOT_FOUND` | P002 | 404 | 클럽(공연장) 없음 |

### 사용자 (U)

| 코드 | 내부코드 | HTTP | 의미 |
| --- | --- | --- | --- |
| `USER_NOT_FOUND` | U001 | 404 | 사용자 없음 |
| `USER_EMAIL_DUPLICATE` | U002 | 409 | 이미 사용 중인 이메일 |

---

## 3. 엔드포인트 카탈로그 (MVP)

### 3.1 Auth (인증)

#### `POST /auth/login`
- **요청**
  ```json
  { "email": "user@example.com", "password": "..." }
  ```
- **응답 200**
  ```json
  { "success": true, "data": { "accessToken": "...", "refreshToken": "...", "expiresIn": 1800 } }
  ```

#### `POST /auth/refresh`
- **헤더**: `Authorization: Bearer {refreshToken}`

#### `POST /auth/signup`
- 이메일 인증 메일 발송 흐름 별도

---

### 3.2 Exploration (탐색 / 인사이트) — **핵심 API**

#### `GET /api/v1/musicians/{musicianId}/insights`

> 특정 뮤지션의 **프로필 + 도슨트 노트 + 협업 네트워크**를 한 번에 조회. 서비스의 핵심 기능.

- **인증**: 불필요 (공개 API)
- **구현 상태**: ✅ 구현 완료
- **인수 테스트**: ✅ Cucumber 시나리오 4개 (`musician_insights.feature`)
  - Happy Path 1: 협업 네트워크 포함 인사이트 조회 (200)
  - Happy Path 2: `includeNetwork=false` 시 network 빈 배열 반환 (200)
  - Unhappy Path 1: 존재하지 않는 뮤지션 ID → 404 / M001
  - Unhappy Path 2: `depth` 범위 초과(3 이상) → 400 / COM001
- **Query Parameters**

  | 파라미터 | 타입 | 필수 | 기본값 | 설명 |
  | --- | --- | --- | --- | --- |
  | `includeNetwork` | boolean | N | true | 협업 네트워크 포함 여부 |
  | `depth` | int (1~2) | N | 1 | 네트워크 탐색 깊이 (MVP: 1~2만 허용, 3 이상 400) |

- **응답 200 (Happy Path)**
  ```json
  {
    "success": true,
    "data": {
      "musicianId": 102,
      "uuid": "550e8400-e29b-...",
      "profile": {
        "stageName": "김재즈",
        "position": "PIANO",
        "verificationTier": "VERIFIED_PRO",
        "profileImageUrl": "https://cdn.dazz.kr/p/102.jpg",
        "bio": "..."
      },
      "docentNote": null,
      "network": [
        {
          "targetUuid": "550e8400-e29b-...",
          "stageName": "이재즈",
          "position": "SAX",
          "weight": 7
        }
      ]
    },
    "meta": { ... }
  }
  ```

- **Fallback 응답** (L1/L2/L3 상세) → `/docs/08-fault-tolerance.md`

- **성능 목표**: p99 < 500ms (NFR-02)
- **캐시 키**: `musician:insight:{musicianId}:depth={N}`, TTL 10분

---

### 3.3 Identity (뮤지션 프로필)

#### `GET /api/v1/musicians/{musicianId}`
- 기본 프로필 조회

#### `PATCH /api/v1/musicians/{musicianId}`
- 본인만 가능 (Verified 등급 이상)
- 헤더: `Authorization` 필수

#### `POST /api/v1/musicians/{musicianId}/claim`
- 시스템이 사전 등록한 뮤지션 프로필에 대해 본인 인증 요청

---

### 3.4 Archive (아카이빙 / 검색)

#### `GET /api/v1/search`
- **Query**: `q`, `type=musician|album|performance`, `page`, `size`
- 응답: 통합 검색 결과 (페이지네이션)

#### `GET /api/v1/musicians/{musicianId}/sideman-history`
- US-02 핵심 기능: Sideman 참여 이력
- **Query**: `page`, `size`, `sort=releaseDate,desc`

#### `GET /api/v1/albums/{albumId}/participations`
- 앨범 참여진 전체

---

### 3.5 Curation (큐레이션)

#### `GET /api/v1/curations`
- **Query**: `mood` (예: rainy-piano), `genre`, `limit`
- 응답: 큐레이션된 뮤지션/앨범 리스트

#### `GET /api/v1/curations/{curationId}`

---

### 3.6 Collaboration (협업 — 쓰기 API)

#### `POST /api/v1/collaborations`
- **인증**: 필수 (`Authorization: Bearer {token}`)
- **헤더**: `Idempotency-Key: {uuid}` **필수** — 클라이언트가 매 요청마다 고유한 UUID 생성
- **요청 본문**
  ```json
  {
    "fromMusicianId": 102,
    "toMusicianId": 205,
    "relationType": "COLLABORATION"
  }
  ```
- **응답 201 Created** (신규 협업 관계)
  ```json
  {
    "success": true,
    "data": {
      "id": 1,
      "fromMusicianId": 102,
      "toMusicianId": 205,
      "relationType": "COLLABORATION",
      "weight": 1,
      "created": true
    }
  }
  ```
- **응답 200 OK** (기존 관계 weight 증가, `created: false`)
- **에러**
  | 상황 | HTTP | 코드 |
  | --- | --- | --- |
  | Idempotency-Key 헤더 누락 | 400 | COM001 |
  | 자기 자신과 협업 | 400 | C002 |
  | 뮤지션 없음 | 404 | M001 |
  | 동시 요청 충돌 | 409 | C003 |
  | 동일 키 + 다른 페이로드 | 409 | COM002 |
- **방향 정규화**: `fromMusicianId`/`toMusicianId` 순서 무관하게 내부적으로 `min:max`로 저장
- **동시성/멱등성 상세** → `/docs/07-concurrency.md`

---

### 3.7 Admin — Musician 승인/거절 (EC-01)

> **Base Path**: `/admin/musicians`
> 인증 미구현 단계. 추후 ADMIN 역할 기반 인증으로 교체 예정.

#### `POST /admin/musicians/{uuid}/approve`

> UNVERIFIED 뮤지션을 VERIFIED_USER로 승인.

- **인증**: 추후 관리자 전용 (현재 미인증 허용)
- **경로 변수**: `uuid` — 승인할 뮤지션의 UUID

- **응답 200 (성공)**
  ```json
  {
    "success": true,
    "data": {
      "id": 1,
      "uuid": "550e8400-e29b-41d4-a716-446655440000",
      "stageName": "김재즈",
      "position": "PIANO",
      "verificationTier": "VERIFIED_USER",
      "claimed": true
    }
  }
  ```

- **에러 응답**

  | 상황 | HTTP | 에러코드 |
  | --- | --- | --- |
  | uuid에 해당하는 뮤지션 없음 | 404 | M001 |
  | UNVERIFIED가 아닌 뮤지션에 승인 시도 (이미 VERIFIED_USER/VERIFIED_PRO/PUBLIC_PROFILE) | 409 | M005 |

---

#### `POST /admin/musicians/{uuid}/reject`

> UNVERIFIED 뮤지션을 PUBLIC_PROFILE로 복귀시키고 userId 연결 해제.

- **인증**: 추후 관리자 전용 (현재 미인증 허용)
- **경로 변수**: `uuid` — 거절할 뮤지션의 UUID

- **응답 200 (성공)**
  ```json
  {
    "success": true,
    "data": {
      "id": 1,
      "uuid": "550e8400-e29b-41d4-a716-446655440000",
      "stageName": "김재즈",
      "position": "PIANO",
      "verificationTier": "PUBLIC_PROFILE",
      "claimed": false
    }
  }
  ```

- **에러 응답**

  | 상황 | HTTP | 에러코드 |
  | --- | --- | --- |
  | uuid에 해당하는 뮤지션 없음 | 404 | M001 |
  | UNVERIFIED가 아닌 뮤지션에 거절 시도 (이미 PUBLIC_PROFILE/VERIFIED_USER/VERIFIED_PRO) | 409 | M006 |

---

### 3.8 Performance (공연)

#### `GET /api/v1/performances`
- **Query**: `from`, `to`, `clubId`, `musicianId`, `genre`

#### `POST /api/v1/performances`
- 공연장 운영자 또는 Verified Pro만 가능

---

## 4. 페이지네이션 규약

```
GET /api/v1/...?page=0&size=20&sort=createdAt,desc
```

응답 메타:
```json
{
  "data": {
    "content": [ ... ],
    "page": {
      "number": 0,
      "size": 20,
      "totalElements": 142,
      "totalPages": 8
    }
  }
}
```

---

## 5. 추후 추가 예정 (상세 명세는 별도 문서)

- `GET /api/v1/musicians/{id}/performances` (공연 이력)
- `GET /api/v1/clubs/{id}` (공연장 상세)
- `POST /api/v1/subscriptions` (구독)
- `WebSocket /ws/collaborations` (실시간 협업 피드)
- 결제 흐름 (PG 연동) — 별도 시퀀스 다이어그램 필요

---

## 6. OpenAPI 문서화

- **springdoc-openapi** 사용 (`@Operation`, `@Schema` 활용)
- 자동 생성 위치: `http://localhost:8080/swagger-ui.html`
- 본 마크다운 문서와 실제 코드 어노테이션은 **항상 동기화**되어야 함
