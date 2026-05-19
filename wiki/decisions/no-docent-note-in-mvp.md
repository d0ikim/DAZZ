# 결정: MVP에서 도슨트 노트 미구현

**Summary**: MVP 단계에서 도슨트 노트는 구현하지 않는다. 인터페이스(데이터 모델)만 정의하고 실제 기능은 Post-MVP로 미룬다.
**Tags**: #decision #mvp #docent-note
**Created**: 2026-05-19
**Last Updated**: 2026-05-19

---

## 결정 내용

MVP에서 도슨트 노트는 **인터페이스만 존재하고 기능은 없다.**

| 항목 | MVP 범위 |
| --- | --- |
| `DOCENT_NOTE` 테이블 스키마 | ✅ 정의 (Post-MVP 대비) |
| 도슨트 노트 작성 UI | ❌ 미구현 |
| 도슨트 노트 조회 API | ❌ 미구현 |
| 뮤지션 프로필에 노트 연결 | ❌ 미구현 |

---

## 탈락 이유

### Option C (도슨트 노트 퍼스트) 탈락과 같은 맥락

도슨트 노트는 **창업자가 지속적으로 글을 써야** 유지되는 기능이다.

- 노트 하나를 제대로 쓰려면: 뮤지션 인터뷰 + 음악 분석 + 편집 → **최소 수 시간**
- MVP 단계에서는 **뮤지션 네트워크 확보**가 더 급한 과제
- "쓸 자신이 없다" = 기능이 있어도 채워지지 않는 빈 껍데기가 됨

빈 껍데기 기능은 DAZZ의 "Insight Navigator" 컨셉을 오히려 해친다.

---

## Post-MVP 조건

도슨트 노트를 구현할 시점:

1. **뮤지션 프로필이 20개 이상 확보**되어 노트가 연결될 대상이 있을 때
2. **창업자 혹은 외부 필진**이 노트를 정기적으로 쓸 의지와 시간이 확보될 때
3. **노트 없이도 서비스가 작동**한다는 것이 검증된 후 (추가 가치 레이어)

---

## 스키마 미리 정의하는 이유

Post-MVP 마이그레이션 비용을 최소화하기 위해 스키마는 미리 잡아둔다.

```sql
DOCENT_NOTE
├── id              BIGINT PK
├── musician_id     BIGINT FK → MUSICIAN
├── author_id       BIGINT FK → USER (관리자 또는 필진)
├── title           VARCHAR(200)
├── body            TEXT
├── style_tags      JSON        ← ["bebop", "post-bop"]
├── published_at    DATETIME NULLABLE
└── created_at      DATETIME
```

MVP에서는 이 테이블을 생성만 해두고 읽거나 쓰는 API는 노출하지 않는다.

---

## 관련 페이지

- [[concepts/docent-note]]
- [[decisions/mvp-option-b]]
- [[concepts/cold-start]]
