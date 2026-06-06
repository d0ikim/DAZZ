# Musician (뮤지션)

**Summary**: DAZZ의 핵심 Aggregate Root. 뮤지션 프로필, 신뢰 등급, UUID 기반 식별을 담당한다.
**Tags**: #domain #aggregate-root #entity
**Created**: 2026-05-19
**Last Updated**: 2026-06-06

---

## 핵심 필드 (MVP ERD 기준)

| 필드 | 타입 | 설명 |
| --- | --- | --- |
| `id` | BIGINT | 내부 식별자. JPA/DB 전용. API에 노출하지 않는다 |
| `uuid` | CHAR(36) | **외부 식별자**. API, 분산락 키 등 외부에 노출되는 ID |
| `user_id` | BIGINT (NULLABLE) | 연결된 USER. NULL이면 Public Profile 상태 |
| `stage_name` | VARCHAR(100) | 활동명. 검색/표시에 사용 |
| `position` | VARCHAR(30) | 주 악기 (Piano, Bass, Drums...) |
| `verification_tier` | VARCHAR(20) | 신뢰 등급. 기본값 `PUBLIC_PROFILE` |
| `bio` | TEXT | 본인 작성 소개글 |
| `sns_url` | VARCHAR(500) | SNS 링크 (인스타그램 등) |
| `profile_image_url` | VARCHAR(500) | 프로필 이미지 URL |

---

## 왜 id와 uuid 두 가지를 쓰는가

- **id (BIGINT)**: DB 성능 최적화. JOIN, FK 모두 숫자 키가 빠름
- **uuid (CHAR36)**: 동명이인 오염 방지 + 외부 노출 안전성
  - 협업 등록 시 `fromMusicianUuid`로 특정 → 이름이 같아도 다른 사람
  - 내부 BIGINT id가 외부에 노출되면 순번 추측 공격 가능

**현행 API 설계 (MVP 기준)**:

| 엔드포인트 | 식별자 | 이유 |
| --- | --- | --- |
| `GET /api/v1/musicians/{musicianId}/insights` | BIGINT `id` | 사용자 spec 요구사항 (musicianId: 102 형태) |
| 협업 등록 등 쓰기 API | UUID | 중복 방지 + 외부 노출 안전성 |

> ⚠️ 인사이트 조회 API가 내부 `id`를 path param으로 노출하는 것은 MVP 결정이다.
> Post-MVP에서 UUID 통일 여부를 재검토한다.

---

## user_id UNIQUE + NULLABLE 설계

```
NULL     → Public Profile (시스템 선등록, 본인 미확인)
NOT NULL → 실제 User와 연결됨
UNIQUE   → 한 User가 여러 뮤지션 프로필을 갖는 것 차단
```

**콜드스타트 연관**: 창업자가 지인 뮤지션 정보를 먼저 입력(user_id=NULL),
뮤지션 본인이 나타나 claim → user_id 연결. → [[concepts/cold-start]]

---

## 도메인 규칙 (비즈니스 로직 위치)

Musician은 순수 POJO. Spring/JPA 어노테이션 없음.
비즈니스 규칙은 Musician 객체 메서드로 구현:

```java
// 예시
public void validateEditable(Long requesterId) {
    if (!Objects.equals(this.userId, requesterId)) {
        throw new ForbiddenException();
    }
    if (this.verificationTier != VerificationTier.VERIFIED_PRO) {
        throw new UnverifiedMusicianException(this.id);
    }
}
```

---

## 연관 관계

```
Musician (1) ──── (0..1) User
Musician (1) ──── (N) Collaboration  ← weight 있는 협업 관계
Musician (M) ──── (N via ALBUM_PARTICIPATION) Album
Musician (M) ──── (N via PERFORMANCE_LINEUP) Performance
Musician (1) ──── (N) DocentNote
```

---

## 인사이트 조회 API (핵심 기능)

`GET /api/v1/musicians/{musicianId}/insights?includeNetwork=true&depth=1`

- **응답**: 프로필 + docentNote(Post-MVP, 현재 null) + 협업 네트워크
- **N+1 방지**: `CollaborationRepository.findByMusicianId()` → `MusicianRepository.findAllByIds()` 배치 조회
- **depth**: 1~2 허용. MVP에서는 depth=1 동작만 구현 (직접 협업자). depth=3 이상 → 400 COM001
- **isVerified**: `verificationTier == VERIFIED_USER || VERIFIED_PRO` 시 true
- **인수 테스트**: ✅ Cucumber 시나리오 4개 (`musician_insights.feature`)
  - Happy 1: 협업 네트워크 포함 조회 — profile.stageName, network[0].name 검증
  - Happy 2: `includeNetwork=false` — network 빈 배열 검증
  - Unhappy 1: 존재하지 않는 musicianId → 404 / M001
  - Unhappy 2: `depth=3` 초과 → 400 / COM001
- **인수 테스트 지원 클래스**:
  - `MusicianInsightSteps.java` — 인사이트 조회 시나리오 Step 정의
  - `CommonSteps.java` — 응답 상태코드/에러코드 검증 공통 Step
  - `ScenarioContext.java` — 시나리오 간 상태 공유 컨텍스트 (`@Scope("cucumber-glue")`)

## 패키지 위치

```
api/musician/
  ├── MusicianController.java          ← GET /api/v1/musicians/{id}/insights
  ├── dto/MusicianInsightResponse.java ← 응답 DTO (중첩 record)
  └── mapper/MusicianInsightMapper.java
application/musician/
  ├── MusicianQueryService.java        ← getInsight() 포함
  └── MusicianInsightResult.java       ← 서비스 반환 타입
domain/musician/Musician.java          ← Aggregate Root (순수 POJO)
infrastructure/persistence/musician/
  ├── MusicianJpaEntity.java           ← @Entity (JPA 전용)
  ├── MusicianJpaRepository.java       ← Spring Data
  ├── MusicianRepositoryImpl.java      ← Port 구현체
  └── mapper/MusicianPersistenceMapper.java

test/resources/features/
  └── musician_insights.feature        ← 인수 테스트 시나리오 4개
test/steps/
  ├── MusicianInsightSteps.java        ← 인사이트 전용 Step 정의
  ├── CommonSteps.java                 ← 상태코드/에러코드 공통 Step
  └── (MusicianClaimSteps.java)        ← claim 관련 Step (ScenarioContext 기반 리팩토링)
test/support/
  ├── ScenarioContext.java             ← 시나리오 간 상태 공유 (@Scope("cucumber-glue"))
  └── TestAdapter.java                 ← HTTP 추상화 (get/post/postWithIdempotencyKey)
```

Domain의 `Musician`과 Infrastructure의 `MusicianJpaEntity`는 **별개 클래스**.
서로 직접 참조하지 않고 Mapper를 통해 변환.

---

## 관련 페이지

- [[concepts/trust-tier]]
- [[concepts/cold-start]]
- [[entities/collaboration]]
- [[entities/user]]
