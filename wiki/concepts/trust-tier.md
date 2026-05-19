# Trust Tier (신뢰 등급)

**Summary**: DAZZ 사용자/뮤지션의 인증 등급 4단계. 등급에 따라 프로필 편집, 협업 등록, 도슨트 노트 작성 권한이 다르다.
**Tags**: #domain #auth #security #musician
**Created**: 2026-05-19
**Last Updated**: 2026-05-19

---

## 등급 정의

| Tier | DB 값 | 표시 | 검증 방식 |
| --- | --- | --- | --- |
| **Verified Pro** | `VERIFIED_PRO` | 파란 체크 | 학력/활동 증빙 + 관리자 승인 |
| **Verified User** | `VERIFIED_USER` | 회색 체크 | 이메일/실명 인증 |
| **Unverified** | `UNVERIFIED` | 없음 | 가입만 완료 |
| **Public Profile** | `PUBLIC_PROFILE` | 'Unverified' 워터마크 | 시스템이 선등록한 뮤지션 (본인 미확인) |

---

## 권한 매트릭스

| 기능 | Public Profile | Unverified | Verified User | Verified Pro |
| --- | --- | --- | --- | --- |
| 프로필 조회 | ✅ | ✅ | ✅ | ✅ |
| 본인 프로필 편집 | ❌ | ❌ | ❌ | ✅ |
| 협업 관계 등록 | ❌ | ❌ | ❌ | ✅ |
| 도슨트 노트 작성 | ❌ | ❌ | ✅ | ✅ |
| 공연 라인업 등록 | ❌ | ❌ | ❌ | ✅ |
| 프로필 인계(claim) | ❌ | ✅ | ✅ | ✅ |

---

## Public Profile 특수 케이스

시스템(또는 관리자)이 뮤지션 정보를 **본인 없이 먼저 등록**한 상태.

**흐름**:
```
1. 관리자가 '김재즈' 뮤지션 프로필 생성 (user_id = NULL)
2. 실제 김재즈 본인이 가입
3. POST /api/v1/musicians/{id}/claim 으로 본인 인증 요청
4. 관리자 승인 → user_id 연결 + tier = VERIFIED_PRO 승격
```

**왜 필요한가**: 콜드스타트 해결 전략 중 하나. 창업자가 지인 뮤지션 정보를 미리 입력해두고, 뮤지션이 나중에 나타나 인계받는 구조. → [[concepts/cold-start]]

---

## DB 설계 연관

`MUSICIAN.verification_tier` 컬럼에 저장.
`MUSICIAN.user_id`는 UNIQUE + NULLABLE:
- NULL → Public Profile (본인 미연결)
- NOT NULL → 실제 유저와 연결됨

한 유저가 여러 뮤지션 프로필을 갖는 것을 UNIQUE 제약으로 차단.

---

## 코드에서 체크하는 위치

- Controller가 아닌 **Application Service**에서 등급 검증
- Spring Security의 `@PreAuthorize`보다 도메인 규칙으로 처리 권장
  (등급 체크 로직이 비즈니스 규칙이지 인프라 규칙이 아니므로)

```java
// ❌ Controller에서 직접 체크
@PatchMapping("/{id}")
public ApiResponse<?> update(@PathVariable Long id, ...) {
    if (!musician.isVerifiedPro()) throw new ForbiddenException();
}

// ✅ Service에서 도메인 규칙으로 체크
public void updateProfile(Long musicianId, Long requesterId, ...) {
    Musician musician = musicianRepository.get(musicianId);
    musician.validateEditable(requesterId); // 도메인 메서드
}
```

---

## 관련 페이지

- [[concepts/cold-start]]
- [[entities/musician]]
- [[entities/user]]
