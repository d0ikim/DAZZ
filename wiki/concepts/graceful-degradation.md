# Graceful Degradation (우아한 장애 대응)

**Summary**: 외부 API 장애 시에도 HTTP 200을 유지하며 부분 데이터를 반환한다. 장애 상황에서도 'K-Jazz Insight Navigator' 컨셉을 포기하지 않는다.
**Tags**: #resilience #fault-tolerance #circuit-breaker #ux
**Created**: 2026-05-19
**Last Updated**: 2026-05-19

---

## 핵심 원칙

> **"5xx는 사용자를 빈 화면에 세운다. 200 + 부분 데이터는 사용자를 다음 골목으로 안내한다."**

- HTTP 상태코드는 가급적 **200 유지**
- 장애 정보는 응답 본문 `meta.degradation` 으로 신호
- 프론트엔드는 `meta`를 보고 placeholder/retry 버튼 표시

---

## 3단계 Fallback

### Level 1 — Best Effort (캐시 우회)
**언제**: 서킷 하나가 막 Open된 순간
**동작**: Redis에서 직전 성공 응답을 꺼내 반환

```json
{
  "success": true,
  "data": { "profile": {...}, "network": [...] },
  "meta": {
    "dataSource": "cache",
    "isStale": true,
    "cachedAt": "2026-05-19T10:00:00Z"
  }
}
```

**UX**: "5분 전 데이터 기준입니다" (정보톤, 알람톤 아님)

---

### Level 2 — Reduced Function (관계도 비활성화)
**언제**: 관계도 그래프 서킷 Open
**동작**: `profile` + `docentNote` 정상, `network: []` 반환

```json
{
  "success": true,
  "data": { "profile": {...}, "network": [] },
  "meta": {
    "degradation": "L2_REDUCED",
    "unavailableFields": ["network"],
    "retryAfter": 30
  }
}
```

**UX**: "관계도는 잠시 후 표시됩니다 · 프로필과 도슨트 노트는 정상이에요"

---

### Level 3 — Minimal (큐레이션으로 우회)
**언제**: docent + network 서킷 모두 Open
**동작**: profile만 + `recommendation` 필드 추가 (비슷한 뮤지션 추천)

```json
{
  "success": true,
  "data": {
    "profile": {...},
    "recommendation": {
      "title": "이런 아티스트는 어떠세요?",
      "items": [...]
    }
  },
  "meta": { "degradation": "L3_MINIMAL" }
}
```

**UX**: 도슨트 자리에 큐레이션 카드 + "다른 아티스트 둘러보기"

**핵심**: 에러가 아니라 **다음 탐험지 안내** → 컨셉 마지막까지 유지

---

## Circuit Breaker 설정 요약

| 인스턴스 | 대상 | failureRateThreshold | waitDuration |
| --- | --- | --- | --- |
| `embedApiCB` | YouTube/SoundCloud | 50% | 30s |
| `mapsApiCB` | Kakao/Naver Maps | 50% | 15s |
| `paymentPgCB` | KakaoPay/Toss | **40%** (보수적) | 60s |

데코레이터 합성 순서: `@Retry → @CircuitBreaker → @TimeLimiter → 외부 API`

---

## 관련 페이지

- [[entities/redis]]
- [[comparisons/kafka-vs-rabbitmq]]
- [[concepts/docent-note]]
