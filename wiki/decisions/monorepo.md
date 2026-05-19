# 결정: 모노레포 구성 (backend + frontend 동일 레포)

**Summary**: backend(Spring Boot)와 frontend(Next.js)를 하나의 GitHub 레포에서 관리. 1인 프로젝트에서 컨텍스트 전환 최소화.
**Tags**: #decision #architecture #monorepo
**Created**: 2026-05-19
**Last Updated**: 2026-05-19

---

## 결정 내용

```
DAZZ/ (단일 레포)
├── backend/    ← Spring Boot API 서버
├── frontend/   ← Next.js 웹 앱
├── docs/       ← 설계 문서
└── wiki/       ← LLM Wiki
```

---

## 채택 이유

1. **1인 프로젝트**: 멀티레포의 장점(팀간 독립 배포)이 불필요
2. **컨텍스트 전환 비용 0**: API 스펙 바꾸면 바로 옆 폴더에서 프론트 수정 가능
3. **GitHub Actions 통합**: 하나의 CI/CD 파이프라인에서 전체 관리
4. **Claude Code 활용**: AI가 백엔드/프론트 코드를 동시에 참조 가능

---

## 레포 구조 규칙

- `backend/`와 `frontend/`는 서로의 코드를 **직접 import 하지 않음**
- 통신은 항상 **HTTP API**를 통해서만
- 각 폴더에 독립적인 `package.json` / `build.gradle.kts` 유지

---

## 배포는 분리

모노레포지만 배포는 별도:
```
backend/  → AWS/GCP 서버 (Spring Boot JAR)
frontend/ → Vercel (Next.js)
```

GitHub Pages(`d0ikim.github.io/DAZZ`)는 Spring Boot 배포 불가 → docs 뷰어 용도로만 사용.

---

## 관련 페이지

- [[decisions/mvp-option-b]]
