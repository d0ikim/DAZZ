# Musician (뮤지션)

**Summary**: DAZZ의 핵심 Aggregate Root. 뮤지션 프로필, 신뢰 등급, UUID 기반 식별을 담당한다.
**Tags**: #domain #aggregate-root #entity
**Created**: 2026-05-19
**Last Updated**: 2026-05-19

---

## 핵심 필드

| 필드 | 타입 | 설명 |
| --- | --- | --- |
| `id` | BIGINT | 내부 식별자. JPA/DB 전용. API에 노출하지 않는다 |
| `uuid` | CHAR(36) | **외부 식별자**. API, 분산락 키 등 외부에 노출되는 ID |
| `user_id` | BIGINT (NULLABLE) | 연결된 USER. NULL이면 Public Profile 상태 |
| `stage_name` | VARCHAR(100) | 활동명. 검색/표시에 사용 |
| `primary_position` | VARCHAR(30) | 주 악기 (Piano, Bass, Drums...) |
| `verification_tier` | VARCHAR(20) | 신뢰 등급. 기본값 `UNVERIFIED` |
| `bio` | TEXT | 본인 작성 소개글 |
| `profile_image_url` | VARCHAR(500) | S3 URL |

---

## 왜 id와 uuid 두 가지를 쓰는가

- **id (BIGINT)**: DB 성능 최적화. JOIN, FK 모두 숫자 키가 빠름
- **uuid (CHAR36)**: 동명이인 오염 방지 + 외부 노출 안전성
  - 협업 등록 시 `fromMusicianUuid`로 특정 → 이름이 같아도 다른 사람
  - 내부 BIGINT id가 외부에 노출되면 순번 추측 공격 가능

**규칙**: API 요청/응답에는 항상 `uuid` 사용. `id`는 코드 내부에서만.

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

## 패키지 위치

```
domain/musician/Musician.java          ← Aggregate Root (순수 POJO)
infrastructure/persistence/musician/
  ├── MusicianJpaEntity.java           ← @Entity (JPA 전용)
  ├── MusicianJpaRepository.java       ← Spring Data
  ├── MusicianRepositoryImpl.java      ← Port 구현체
  └── mapper/MusicianPersistenceMapper.java
```

Domain의 `Musician`과 Infrastructure의 `MusicianJpaEntity`는 **별개 클래스**.
서로 직접 참조하지 않고 Mapper를 통해 변환.

---

## 관련 페이지

- [[concepts/trust-tier]]
- [[concepts/cold-start]]
- [[entities/collaboration]]
- [[entities/user]]
