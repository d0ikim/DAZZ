# 08. Fault Tolerance — 장애 대응 및 회복 전략

> 외부 API 의존성 장애가 DAZZ 서비스 전반으로 **전파(propagation)**되는 것을 차단하기 위한 분석과 전략.
> 외부 API를 호출하는 모든 코드는 본 문서의 패턴(Retry + Circuit Breaker + TimeLimiter)을 적용해야 합니다.

---

## 1. 핵심 원칙: 컨셉을 지키는 Graceful Degradation

> **"시스템이 망가졌을 때조차 'K-Jazz Insight Navigator'의 컨셉을 지킨다."**

- HTTP 상태 코드는 가급적 **200을 유지**
- Degradation은 응답 본문의 `meta`로 신호
- 5xx은 클라이언트가 빈 화면을 띄울 수밖에 없음
- 200 + 부분 데이터를 주면 프론트엔드가 "있는 것은 그리고, 없는 것은 placeholder"로 일관 렌더링 가능

---

## 2. Root Cause — 외부 API 지연이 DAZZ를 죽이는 메커니즘

### 2.1 Thread Pool Hell

Tomcat은 기본 200개의 워커 스레드를 풀에 미리 만들어 둔다. 요청 처리 시 스레드 1개 할당, 외부 API 응답 도착까지 `BLOCKED` 상태.

**시나리오** (YouTube 임베드 API가 평소 100ms → 5초 지연):
- **Phase 1 (0~1s)**: 정상 50 req/s × 0.1s = 5개 스레드만 사용. 풀에 195개 여유
- **Phase 2 (1~5s)**: 1초마다 50개 새 요청 유입, 기존 요청은 5초간 점유 → 5초 시점에 250개 필요 (풀은 200개뿐)
- **Phase 3 (5s~)**: 잉여 50개 요청은 `acceptCount` 큐(기본 100)에 적재, 큐도 곧 포화
- **Phase 4**: 큐마저 가득 차면 Tomcat이 TCP 연결 거절 → 사용자는 503 또는 Connection Error

**핵심 통찰**:
> 느린 외부 서버가 DAZZ를 죽이는 이유는 "응답이 느려서"가 아니라, **그 응답을 기다리는 내 스레드가 다른 요청에 쓰일 자원을 독점하기 때문**.
> 방어의 본질은 "외부 서버를 빠르게 만드는 것"이 아니라 **"내 스레드가 너무 오래 외부 응답을 기다리지 않도록 강제하는 것"**.

### 2.2 Connection Timeout vs Read Timeout

| 구분 | Connection Timeout | Read Timeout |
| --- | --- | --- |
| 발생 시점 | TCP 3-way handshake | 연결 성공 후 응답 수신 대기 |
| 원인 | 네트워크 단절, 방화벽, 서버 다운 | 서버는 살아있지만 처리 지연 |
| 비유 | 전화 신호음조차 안 들림 | 통화 연결됐는데 상대가 말 없음 |

### 2.3 DAZZ API별 Timeout 설정

| API | Connect | Read | 근거 |
| --- | --- | --- | --- |
| YouTube/SoundCloud 임베드 | 1s | 3s | 글로벌 CDN, 메타 조회는 여유 |
| Kakao/Naver Maps | 1s | 2s | 국내 API, NFR-02(0.5s) 페이지 로딩 정합 |
| KakaoPay/Toss PG | 2s | 5s | 금융 처리, 카드사 인증 시간 고려 |

**원칙**:
1. **Connect Timeout은 짧게 (1~2s)** — 네트워크 단절은 retry/fallback으로 빠르게 우회
2. **Read Timeout은 API 성격에 따라 차등** — 결제와 임베드를 같은 값으로 묶지 않음
3. **두 값의 합이 사용자 인내 임계점(8~10s)을 넘지 않을 것**

---

## 3. Retry 전략 → `/docs/07-concurrency.md` Section 4 참조

본 문서에서는 외부 API 호출에 적용되는 합성 데코레이터 순서만 다룬다.

---

## 4. Circuit Breaker 설정

### 4.1 API별 독립 인스턴스 (장애 영향도 차등)

#### `embedApiCB` — YouTube / SoundCloud 임베드 (영향도 낮음)
```yaml
slidingWindowType: COUNT_BASED
slidingWindowSize: 10
minimumNumberOfCalls: 5
failureRateThreshold: 50
slowCallRateThreshold: 50
slowCallDurationThreshold: 3000ms
waitDurationInOpenState: 30s
permittedNumberOfCallsInHalfOpenState: 3
automaticTransitionFromOpenToHalfOpenEnabled: true
```
**근거**: 임베드 실패해도 썸네일 + 외부 링크 fallback. 페이지 자체는 정상. 글로벌 장애는 복구 느림 → 30s 대기.

#### `mapsApiCB` — Kakao/Naver Maps (영향도 중간)
```yaml
slidingWindowType: COUNT_BASED
slidingWindowSize: 10
minimumNumberOfCalls: 5
failureRateThreshold: 50
slowCallRateThreshold: 50
slowCallDurationThreshold: 2000ms
waitDurationInOpenState: 15s
permittedNumberOfCallsInHalfOpenState: 3
```
**근거**: 지도 안 보여도 텍스트 주소 fallback. 국내 API는 복구 빠른 편 → 15s.

#### `paymentPgCB` — KakaoPay / Toss (영향도 매우 높음)
```yaml
slidingWindowType: COUNT_BASED
slidingWindowSize: 5
minimumNumberOfCalls: 3
failureRateThreshold: 40
slowCallRateThreshold: 40
slowCallDurationThreshold: 5000ms
waitDurationInOpenState: 60s
permittedNumberOfCallsInHalfOpenState: 1
```
**근거**:
- **표본 5건만으로도 빠르게 감지** (10건까지 기다리면 이미 5명이 결제 실패 경험)
- `failureRateThreshold: 40` — 보수적 설정 (5건 중 2건 실패 시 즉시 차단)
- `permittedNumberOfCallsInHalfOpenState: 1` — 결제 탐색은 단 1건만 (잘못된 결제 결과 위험 최소화)

### 4.2 통합 application.yml

```yaml
resilience4j:
  circuitbreaker:
    instances:
      embedApiCB:
        slidingWindowType: COUNT_BASED
        slidingWindowSize: 10
        minimumNumberOfCalls: 5
        failureRateThreshold: 50
        slowCallRateThreshold: 50
        slowCallDurationThreshold: 3000ms
        waitDurationInOpenState: 30s
        permittedNumberOfCallsInHalfOpenState: 3
        automaticTransitionFromOpenToHalfOpenEnabled: true

      mapsApiCB:
        slidingWindowType: COUNT_BASED
        slidingWindowSize: 10
        minimumNumberOfCalls: 5
        failureRateThreshold: 50
        slowCallRateThreshold: 50
        slowCallDurationThreshold: 2000ms
        waitDurationInOpenState: 15s
        permittedNumberOfCallsInHalfOpenState: 3
        automaticTransitionFromOpenToHalfOpenEnabled: true

      paymentPgCB:
        slidingWindowType: COUNT_BASED
        slidingWindowSize: 5
        minimumNumberOfCalls: 3
        failureRateThreshold: 40
        slowCallRateThreshold: 40
        slowCallDurationThreshold: 5000ms
        waitDurationInOpenState: 60s
        permittedNumberOfCallsInHalfOpenState: 1
        automaticTransitionFromOpenToHalfOpenEnabled: true

  retry:
    instances:
      externalApiRetry:
        maxAttempts: 3
        waitDuration: 1000ms
        exponentialBackoffMultiplier: 2
        retryExceptions:
          - java.net.ConnectException
          - java.net.SocketTimeoutException
          - org.springframework.web.client.HttpServerErrorException
        ignoreExceptions:
          - org.springframework.web.client.HttpClientErrorException.BadRequest
          - com.dazz.domain.shared.BusinessException

  timelimiter:
    instances:
      embedApiCB:
        timeoutDuration: 4s
      mapsApiCB:
        timeoutDuration: 3s
      paymentPgCB:
        timeoutDuration: 6s
```

---

## 5. 데코레이터 합성 순서

```
요청 ──► @Retry ──► @CircuitBreaker ──► @TimeLimiter ──► 외부 API
```

- **TimeLimiter가 가장 안쪽**: slow call로 분류되어야 서킷이 트립됨
- **CircuitBreaker가 중간**: TimeLimiter 실패를 카운트
- **Retry가 바깥**: 서킷이 닫혀 있을 때만 재시도 의미 있음

```java
@Service
@RequiredArgsConstructor
public class ExternalApiService {

    private final YoutubeClient youtubeClient;

    @Retry(name = "externalApiRetry")
    @CircuitBreaker(name = "embedApiCB", fallbackMethod = "embedFallback")
    @TimeLimiter(name = "embedApiCB")
    public CompletableFuture<EmbedResponse> fetchEmbed(String videoUrl) {
        return CompletableFuture.supplyAsync(() -> youtubeClient.getEmbed(videoUrl));
    }

    private CompletableFuture<EmbedResponse> embedFallback(String videoUrl, Throwable e) {
        log.warn("[DAZZ] Embed API degraded — thumbnail fallback: {}", videoUrl, e);
        return CompletableFuture.completedFuture(EmbedResponse.thumbnailFallback(videoUrl));
    }
}
```

---

## 6. 3단계 Fallback 전략 (핵심 기능: 뮤지션 인사이트 조회)

### 6.1 Level 1 (Best Effort) — 캐시된 응답으로 우회

**언제 활성화?**
- `profile` / `docent` / `graph` 서비스 중 하나의 서킷이 막 Open되었거나 TimeLimiter가 트리거된 순간
- `@CircuitBreaker fallbackMethod`이 Redis에서 직전 성공 응답을 꺼내 반환
- 사용자는 페이지가 정상 로드된 것처럼 느낌

**응답**: 200 OK + `meta.isStale=true`
```json
{
  "success": true,
  "data": {
    "musicianId": 102,
    "profile": { "stageName": "김재즈", "primaryPosition": "Piano" },
    "docentNote": { "styleTags": ["#비밥"], "summary": "..." },
    "network": [ { "targetId": 205, "stageName": "이재즈" } ]
  },
  "meta": {
    "dataSource": "cache",
    "cachedAt": "2026-05-17T00:25:00Z",
    "isStale": true
  }
}
```

**UX 메시지**: 프로필 사진 옆 작은 회색 텍스트 — "5분 전 데이터 기준입니다. 새로고침으로 최신화"
- 알람톤이 아닌 **정보톤**
- L1의 미덕은 **"조용함"**

### 6.2 Level 2 (Reduced Function) — network 필드만 비활성화

**언제 활성화?**
- 가장 무거운 컴포넌트인 **관계도 그래프 컴퓨팅 서킷이 Open**
- `profile`과 `docentNote`는 살아있고, `network`만 빈 배열로 반환
- 페이지의 50%는 그대로 작동 → 사용자는 다른 축(이력, 발매 음원, 도슨트 노트)으로 계속 탐험

**응답**: 200 OK + `meta.degradation=L2_REDUCED`
```json
{
  "success": true,
  "data": {
    "musicianId": 102,
    "profile": { ... },
    "docentNote": { ... },
    "network": []
  },
  "meta": {
    "degradation": "L2_REDUCED",
    "unavailableFields": ["network"],
    "retryAfter": 30
  }
}
```

**UX 메시지**: 관계도 자리에 회색 placeholder 카드 + retry 버튼 + "관계도는 잠시 후 표시됩니다 · 프로필과 도슨트 노트는 정상이에요"
- 핵심: **"무엇이 살아있는지를 명시"**
- "오류" 신호가 아니라 "지금 보이는 정보는 신뢰해도 된다"는 신호

### 6.3 Level 3 (Minimal) — 큐레이션으로 우회 안내

**언제 활성화?**
- `docent`와 `network` 양쪽 서킷이 모두 Open
- 사실상 인사이트 기능 마비
- 하지만 컨셉은 "K-Jazz Insight Navigator" → 도슨트가 길을 잃은 순간에도 다른 길을 안내

**응답**: 200 OK + 새 필드 `recommendation`
```json
{
  "success": true,
  "data": {
    "musicianId": 102,
    "profile": {
      "stageName": "김재즈",
      "primaryPosition": "Piano",
      "profileImageUrl": "https://cdn.dazz.kr/p/102.jpg"
    },
    "docentNote": null,
    "network": [],
    "recommendation": {
      "title": "이런 아티스트는 어떠세요?",
      "context": "같은 비밥 계열 피아니스트",
      "items": [
        { "musicianId": 87, "stageName": "박재즈", "primaryPosition": "Piano" },
        { "musicianId": 142, "stageName": "최재즈", "primaryPosition": "Piano" }
      ]
    }
  },
  "meta": {
    "degradation": "L3_MINIMAL",
    "userMessage": "이 아티스트는 잠시 후 다시 시도해보세요"
  }
}
```

**UX 메시지**: 도슨트 노트 자리에 친근한 톤 + 큐레이션 카드 2~3개 + "다른 아티스트 둘러보기" CTA
- **"에러가 발생했습니다"는 사용자를 막다른 길에 세우고, 큐레이션은 사용자를 다음 골목으로 안내한다.**
- DAZZ의 컨셉인 '퍼스널 도슨트'가 마지막까지 깨지지 않는 안전망

### 6.4 세 단계를 관통하는 원칙

> **HTTP 상태 코드는 가급적 200을 유지하고, degradation은 응답 본문의 `meta`로 신호한다.**

L3의 큐레이션 라우팅은 단순한 에러 처리가 아님. **시스템이 망가졌을 때조차 컨셉을 지키는 것**이 진짜 Graceful Degradation.

---

## 7. Bulkhead — 자원 격리

### 7.1 적용 대상

| 기능 | Bulkhead 적용? | 근거 |
| --- | --- | --- |
| `archiveSearch` (무거운 검색) | ✅ 우선 적용 | 무거운 기능이 가벼운 기능 스레드 풀 잠식 방지 |
| `musicianInsight` | ✅ | 핵심 기능 보호 |
| 기타 가벼운 조회 | ❌ | default 풀로 충분 |

### 7.2 설정 예시
```yaml
resilience4j:
  bulkhead:
    instances:
      archiveSearchBulkhead:
        maxConcurrentCalls: 20
        maxWaitDuration: 100ms
      musicianInsightBulkhead:
        maxConcurrentCalls: 50
        maxWaitDuration: 50ms
```

---

## 8. 검증 시나리오 (Chaos Engineering)

| 시나리오 | 도구 | 기대 동작 |
| --- | --- | --- |
| YouTube API 5초 지연 시뮬레이션 | Toxiproxy | 10건 중 5건이 3초 초과 → embedApiCB Open → fallback 응답 |
| KakaoPay 200ms 응답 + 30% 500 에러 | Toxiproxy | 5건 중 2건 실패 → paymentPgCB Open → pending 저장 |
| 네트워크 단절 시뮬레이션 | Toxiproxy | ConnectException → retry 3회 (1s, 2s, 4s) → 최종 실패 시 fallback |
| Redis 다운 | docker stop redis | 캐시 미스 → DB 직접 조회 + 로깅, 서비스 지속 |
| MySQL 다운 | docker stop mysql | 캐시 응답으로 우회 (L1), 새 쓰기는 503 |

---

## 9. 운영 모니터링 지표

| 지표 | 임계 알람 | 대응 |
| --- | --- | --- |
| 서킷브레이커 상태 (`*.state`) | OPEN 전이 | 즉시 알람, 외부 API 모니터링 페이지 확인 |
| 실패율 (`*.failureRate`) | > 30% 5분 지속 | 사전 경고 |
| Slow call 비율 (`*.slowCallRate`) | > 30% 5분 지속 | 사전 경고 |
| 평균 응답 시간 (`*.duration`) | NFR 초과 | 성능 회귀 분석 |
| Fallback 실행 횟수 | 평소 대비 10배 | 정상 처리 문제 발생 |
| Tomcat 활성 스레드 수 | > 80% (160/200) | 풀 확장 또는 트래픽 차단 |

### Prometheus + Grafana 연동
```yaml
management:
  metrics:
    export:
      prometheus:
        enabled: true
  endpoints:
    web:
      exposure:
        include: health,prometheus,circuitbreakers
```

수집 메트릭: `resilience4j_circuitbreaker_state`, `resilience4j_circuitbreaker_calls_total`, `resilience4j_retry_calls_total`

---

## 10. 의사결정 로그

| 결정 | 채택안 | 대안 | 채택 사유 |
| --- | --- | --- | --- |
| 서킷 인스턴스 | 외부 API별 독립 | 통합 1개 | 영향도가 API마다 다름. 결제와 임베드를 같은 임계값으로 묶을 수 없음 |
| Window Type | COUNT_BASED | TIME_BASED | DAZZ 트래픽이 고르지 않음(새벽 적음). 시간 기반은 저트래픽 시 오판 |
| Retry 위치 | 외부 API 호출에만 | 모든 서비스 메서드 | 내부 비즈니스 오류는 retry해도 결과 같음 |
| Fallback 응답 코드 | 200 + meta.degradation | 503 Service Unavailable | 부분 데이터라도 클라이언트 렌더링 가능. 5xx는 빈 화면 |
| Bulkhead 적용 대상 | archiveSearch 우선 | 모든 기능 | 무거운 기능 격리가 ROI 높음 |

---

## 11. 참고 자료

- Michael T. Nygard, *Release It!* — Circuit Breaker 패턴 원전
- AWS Architecture Blog, *Exponential Backoff And Jitter*
- Resilience4j 공식 문서 — https://resilience4j.readme.io
- Tomcat 9 Configuration Reference — Connector `maxThreads`, `acceptCount`
