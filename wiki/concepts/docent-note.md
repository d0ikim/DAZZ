# Docent Note (도슨트 노트)

**Summary**: 전공자 시각으로 쓴 뮤지션/앨범 해설. DAZZ를 백과사전이 아닌 '퍼스널 도슨트'로 만드는 핵심 콘텐츠. MVP에서는 미구현.
**Tags**: #domain #content #mvp #curation
**Created**: 2026-05-19
**Last Updated**: 2026-05-19

---

## 정의

단순 정보 나열이 아닌 **전공자의 해석(Insight)** 이 담긴 해설문.

예시:
- ❌ "김재즈는 피아니스트로 2010년 데뷔했다" (정보)
- ✅ "김재즈의 보이싱 방식은 Bill Evans의 인터플레이 개념을 한국 정서로 재해석한 것으로, 그의 트리오 음반에서..." (통찰)

---

## DAZZ에서의 역할

- 관계도에서 두 뮤지션의 협업 지점을 클릭했을 때 나타나는 **맥락 해설**
- "왜 이 두 사람의 협업이 중요한가"를 설명
- 재즈 입문자가 길을 잃지 않고 다음 탐험지로 이동하게 안내

---

## DB 구조

```
DOCENT_NOTE
├── id
├── musician_id (FK, NOT NULL)   ← 대상 뮤지션
├── style_tags (JSON)            ← ["#비밥", "#서정적"]
├── summary (TEXT)               ← 한 줄 요약
├── content (TEXT)               ← 본문
└── author_id (FK)               ← 작성자 USER
```

---

## 누가 쓰는가 (작성 주체)

| 단계 | 작성자 | 방법 |
| --- | --- | --- |
| MVP | 없음 (미구현) | — |
| Phase 2 | Verified User 이상 | 직접 작성 |
| 장기 | 뮤지션 본인 또는 인증된 평론가 | 작성 후 관리자 검수 |

**외부 아티클 스크래핑은 채택 안 함**: 재즈피플 등 외부 콘텐츠를 긁어오면
DAZZ만의 도슨트가 아니므로 컨셉 파괴. → [[decisions/no-docent-note-in-mvp]]

---

## MVP에서의 처리 방식

도슨트 노트는 MVP에서 **인터페이스(Port)만 정의**하고 구현체는 만들지 않는다.

```java
// application/port/out/DocentNoteRepository.java
public interface DocentNoteRepository {
    Optional<DocentNote> findByMusicianId(Long musicianId);
}
```

API 응답에서는 `docentNote: null` 로 반환.
프론트엔드는 null일 때 "도슨트 노트 준비 중" placeholder 표시.

이렇게 하면 나중에 구현체만 붙이면 되고 **핵심 도메인 코드 0줄 수정**.

---

## 관련 페이지

- [[decisions/no-docent-note-in-mvp]]
- [[concepts/cold-start]]
- [[concepts/graceful-degradation]]
