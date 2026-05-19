# User (서비스 사용자)

**Summary**: DAZZ 서비스에 가입한 사용자. Musician과 1:1 Optional 관계. 역할은 USER / ADMIN 두 가지.
**Tags**: #domain #entity #auth
**Created**: 2026-05-19
**Last Updated**: 2026-05-19

---

## DB 구조

```sql
USER
├── id                BIGINT PK AUTO_INCREMENT
├── email             VARCHAR(255) UNIQUE NOT NULL  ← 로그인 식별자
├── password_hash     VARCHAR(255) NOT NULL          ← BCrypt
├── display_name      VARCHAR(100) NOT NULL
├── role              VARCHAR(20) DEFAULT 'USER'     ← USER / ADMIN
└── email_verified_at DATETIME NULLABLE
```

---

## User ↔ Musician 관계

```
USER (1) ──── (0..1) MUSICIAN
```

- User가 없어도 Musician은 존재 가능 (Public Profile)
- Musician이 없어도 User는 존재 가능 (일반 팬 사용자)
- 한 User는 **최대 하나의 Musician**만 연결 (MUSICIAN.user_id UNIQUE)

**연결 흐름**:
```
1. User 가입 (email/password)
2. "나는 뮤지션이에요" → Musician 프로필 신청
3. 관리자 승인 → MUSICIAN.user_id = User.id 연결
4. verification_tier = VERIFIED_PRO 승격
```

또는:
```
1. 관리자가 먼저 Musician 등록 (user_id = NULL)
2. 해당 뮤지션 본인이 가입
3. /claim API로 인계 요청
4. 관리자 승인 → 연결
```

---

## 인증 방식

- JWT Bearer Token
- Access Token: 30분
- Refresh Token: 14일
- 발급: `POST /auth/login`
- 갱신: `POST /auth/refresh`

---

## role 구분

| role | 권한 |
| --- | --- |
| `USER` | 일반 사용자. Trust Tier에 따라 권한 세분화 |
| `ADMIN` | 관리자. Musician 승인/거절, 데이터 관리 |

**주의**: role은 시스템 접근 권한. 뮤지션 기능 권한은 `MUSICIAN.verification_tier`로 별도 관리. 두 개념 혼용 금지.

---

## 관련 페이지

- [[entities/musician]]
- [[concepts/trust-tier]]
