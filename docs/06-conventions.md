# 06. Coding Conventions & Anti-Patterns

> 이 문서는 코드 품질의 **최소 기준선**입니다.
> 여기 명시된 규칙을 위반하는 PR은 머지되지 않습니다.
> AI는 코드 생성 시 매번 본 문서의 체크리스트를 적용해야 합니다.

---

## 1. 네이밍 컨벤션

### 1.1 일반 규칙
- 클래스: `PascalCase` (예: `MusicianQueryService`)
- 메서드/변수: `camelCase` (예: `findInsightById`)
- 상수: `UPPER_SNAKE_CASE` (예: `MAX_NETWORK_DEPTH`)
- 패키지: `lowercase` 단어 연결 (예: `com.dazz.application.musician`)
- 한글 변수명 금지

### 1.2 도메인 용어 일관성

비즈니스 용어는 한국어 그대로 영문화 (직역 금지):

| 한국어 | 영문 표기 | 금지 표기 |
| --- | --- | --- |
| 도슨트 노트 | `DocentNote` | `Description`, `Comment` |
| 협업 가중치 | `collaborationWeight` | `relationStrength` |
| 인사이트 | `Insight` | `Detail`, `Summary` |
| 라인업 | `Lineup` | `Members`, `Cast` |
| Sideman | `sideman` | `participant`, `member` |

### 1.3 메서드명 패턴

| 의도 | 접두어 |
| --- | --- |
| 단건 조회 (없으면 예외) | `get` (예: `getMusicianById`) |
| 단건 조회 (Optional) | `find` (예: `findMusicianByUuid`) → `Optional<Musician>` |
| 다건 조회 | `findAll`, `search` |
| 존재 확인 | `exists` (예: `existsByEmail`) |
| 생성 | `create` |
| 수정 | `update` |
| 삭제 (논리) | `delete` |
| 삭제 (물리) | `hardDelete` (지양) |

---

## 2. 의존성 주입 (DI)

### ✅ DO — 생성자 주입 (Lombok `@RequiredArgsConstructor`)
```java
@Service
@RequiredArgsConstructor
public class MusicianQueryService {
    private final MusicianRepository musicianRepository;
    private final CachePort cachePort;
    // ...
}
```

### ❌ DON'T — 필드 주입
```java
@Service
public class MusicianQueryService {
    @Autowired
    private MusicianRepository musicianRepository;  // 금지
}
```

**이유**: 불변성 보장, 테스트 시 생성자 호출로 mock 주입 가능, 순환 참조 조기 탐지

---

## 3. DTO 규칙

### 3.1 record 타입 적극 활용 (Java 17+)
```java
public record MusicianInsightResponse(
    Long musicianId,
    String uuid,
    ProfileDto profile,
    DocentNoteDto docentNote,
    List<NetworkNodeDto> network
) {
    public static MusicianInsightResponse from(MusicianInsight domain) {
        return new MusicianInsightResponse(
            domain.getId(),
            domain.getUuid().toString(),
            ProfileDto.from(domain.getProfile()),
            DocentNoteDto.from(domain.getDocentNote()),
            domain.getNetwork().stream().map(NetworkNodeDto::from).toList()
        );
    }
}
```

### 3.2 DTO에서만 `@Data` 허용
```java
// Entity ❌
@Entity
@Data  // 절대 금지: Setter 전체 공개 + 양방향 toString 무한 루프
public class Musician { ... }

// DTO에서는 record 우선, 어쩔 수 없는 경우만 @Data
@Data
public class LegacyResponseDto { ... }
```

### 3.3 Entity → Controller 반환 금지
```java
// ❌ 절대 금지
@GetMapping("/{id}")
public Musician getById(@PathVariable Long id) { ... }

// ✅ 항상 DTO로 변환
@GetMapping("/{id}")
public ApiResponse<MusicianResponse> getById(@PathVariable Long id) {
    Musician musician = service.getById(id);
    return ApiResponse.success(MusicianResponse.from(musician));
}
```

**이유**: 순환 참조 무한루프 방지, Lazy Loading 예외 방지, API 변경에 강건

---

## 4. 예외 처리

### 4.1 절대 `throw new RuntimeException("...")` 금지

```java
// ❌
if (musician == null) {
    throw new RuntimeException("Musician not found");
}

// ✅ 커스텀 예외 계층 사용
if (musician == null) {
    throw new MusicianNotFoundException(musicianId);
}
```

### 4.2 예외 계층 구조

```java
// domain/shared/BusinessException.java
public abstract class BusinessException extends RuntimeException {
    private final ErrorCode errorCode;

    protected BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    public ErrorCode getErrorCode() { return errorCode; }
}

// domain/musician/exception/MusicianNotFoundException.java
public class MusicianNotFoundException extends BusinessException {
    public MusicianNotFoundException(Long musicianId) {
        super(ErrorCode.MUSICIAN_NOT_FOUND,
              "뮤지션을 찾을 수 없습니다: " + musicianId);
    }
}
```

### 4.3 전역 예외 핸들러
```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException e) {
        return ResponseEntity
            .status(e.getErrorCode().getStatus())
            .body(ApiResponse.error(e.getErrorCode(), e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnknown(Exception e) {
        log.error("Unexpected error", e);
        return ResponseEntity
            .status(500)
            .body(ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR));
    }
}
```

---

## 5. 메서드 설계

### 5.1 단일 책임 원칙
- 메서드는 **하나의 책임**만 갖는다
- **20라인을 넘어가면** 분리를 진지하게 검토 (강제는 아니지만 강한 신호)

### 5.2 매개변수 3개 초과 시 → 객체화
```java
// ❌
public void register(String name, String position, int year, String bio, String url) { ... }

// ✅
public void register(MusicianRegistrationCommand command) { ... }
```

### 5.3 Boolean 매개변수 지양 (의도가 호출부에 드러나지 않음)
```java
// ❌
service.fetch(true, false);

// ✅ enum 또는 명명된 메서드
service.fetchWithCache(NetworkDepth.SHALLOW);
service.fetchWithoutCache(NetworkDepth.SHALLOW);
```

---

## 6. 계층별 책임

### 6.1 Controller — 얇게 유지
```java
@RestController
@RequestMapping("/api/v1/musicians")
@RequiredArgsConstructor
public class MusicianController {
    private final MusicianQueryService queryService;

    @GetMapping("/{id}/insights")
    public ApiResponse<MusicianInsightResponse> getInsight(
        @PathVariable Long id,
        @RequestParam(defaultValue = "true") boolean includeNetwork,
        @RequestParam(defaultValue = "1") @Min(1) @Max(3) int depth
    ) {
        MusicianInsight insight = queryService.getInsight(id, depth, includeNetwork);
        return ApiResponse.success(MusicianInsightResponse.from(insight));
    }
}
```

**Controller의 책임**: HTTP 매핑, 입력 검증, DTO 변환, 위임. **비즈니스 로직 절대 금지**.

### 6.2 Service — Use Case의 오케스트레이션
- 트랜잭션 경계 (`@Transactional`)
- 여러 Repository/Port 호출 조합
- **하나의 메서드 = 하나의 Use Case**

### 6.3 Domain — 비즈니스 규칙
- JPA, Spring 등 인프라 의존성 0
- 불변성 우선 (final 필드, Setter 지양)
- 자기 자신을 검증할 수 있어야 함

---

## 7. JPA 사용 규칙

### 7.1 N+1 문제 방어
```java
// ❌ N+1 발생
List<Musician> musicians = repository.findAll();
musicians.forEach(m -> m.getCollaborations().size());  // 매번 쿼리

// ✅ Fetch Join
@Query("SELECT m FROM Musician m LEFT JOIN FETCH m.collaborations WHERE m.id = :id")
Optional<Musician> findByIdWithCollaborations(@Param("id") Long id);

// ✅ @BatchSize (페이지네이션 호환)
@OneToMany(mappedBy = "musician")
@BatchSize(size = 50)
private List<Collaboration> collaborations;
```

### 7.2 양방향 연관관계 자제
- 필요 없으면 단방향만 선언
- 양방향이 필요하면 **연관관계 편의 메서드** 작성

### 7.3 영속성 컨텍스트와 트랜잭션
- Lazy Loading 호출은 반드시 트랜잭션 내에서
- DTO 변환은 트랜잭션 내에서 수행

---

## 8. 테스트 컨벤션

### 8.1 BDD 스타일 (Given-When-Then)
```java
@Test
@DisplayName("[성공] 존재하는 뮤지션 ID로 인사이트를 조회한다")
void getInsight_success() {
    // Given
    Long musicianId = 102L;
    Musician given = MusicianFixture.aMusician().withId(musicianId).build();
    when(repository.findById(musicianId)).thenReturn(Optional.of(given));

    // When
    MusicianInsight result = service.getInsight(musicianId, 1, true);

    // Then
    assertThat(result.getId()).isEqualTo(musicianId);
    assertThat(result.getNetwork()).isNotEmpty();
}

@Test
@DisplayName("[실패] 존재하지 않는 뮤지션 ID 조회 시 MusicianNotFoundException")
void getInsight_notFound() {
    // Given
    Long unknownId = 999L;
    when(repository.findById(unknownId)).thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> service.getInsight(unknownId, 1, true))
        .isInstanceOf(MusicianNotFoundException.class);
}
```

### 8.2 필수 테스트 케이스
- ✅ 성공 케이스 (Happy Path)
- ✅ **예외 케이스 (Unhappy Path) 최소 1개**
- ✅ 경계값 (Boundary)

### 8.3 테스트 픽스처
- `MusicianFixture.aMusician().with...().build()` 빌더 패턴
- 매번 객체 직접 생성 금지 (재사용성 + 가독성)

### 8.4 통합 테스트는 Testcontainers
```java
@SpringBootTest
@Testcontainers
class MusicianRepositoryIntegrationTest {
    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0");
    // ...
}
```

---

## 9. 로깅 컨벤션

### 9.1 로그 레벨 가이드
| 레벨 | 사용 |
| --- | --- |
| `ERROR` | 즉시 알람 필요한 시스템 오류 (DB 다운, 외부 API 영구 장애) |
| `WARN` | 일시적 장애, 폴백 발동 (Circuit Breaker Open) |
| `INFO` | 비즈니스 이벤트 (협업 등록, 결제 완료) |
| `DEBUG` | 개발 추적용 |

### 9.2 구조화 로그 (JSON 권장)
```java
log.warn("[DAZZ] Circuit breaker opened — service: {}, fallbackUsed: {}, requestId: {}",
    "embedApiCB", "thumbnail", requestId);
```

### 9.3 개인정보 로깅 금지
- 비밀번호, 토큰, 카드번호: 절대 로그에 남기지 않음
- 이메일은 마스킹 (`u***@example.com`)

---

## 10. AI가 자주 저지르는 실수 (Common Pitfalls)

### 10.1 ✅ 반드시 피해야 할 것

1. **Entity의 `@Data` 사용** → Setter 노출 + toString 무한루프. DTO에만 한정
2. **`throw new RuntimeException()`** → 항상 BusinessException 계층 사용
3. **트랜잭션 안에서 외부 API 호출** → 커밋 후 이벤트 발행으로 분리
4. **락 안에서 트랜잭션 시작 (안 함)** → Facade 패턴으로 락이 트랜잭션을 감싸도록 (`/docs/07-concurrency.md`)
5. **`Optional` 반환 후 `.get()`** → `.orElseThrow(() -> new ...)` 사용
6. **`@Transactional` 없는 Service에서 Lazy 컬렉션 접근** → LazyInitializationException
7. **N+1을 무시한 `findAll().forEach(m -> m.getX())`**
8. **DTO 매핑을 Controller가 직접** → 정적 팩토리 메서드 (`from()`)에 위임
9. **양방향 연관관계 자동 추가** → 필요할 때만, 편의 메서드 작성
10. **`@Component` 남발** → 도메인 객체에는 Spring 어노테이션 금지

### 10.2 ✅ 권장 패턴

- Service의 `@Transactional`은 **클래스가 아닌 메서드** 단위 (읽기/쓰기 분리)
- `@Transactional(readOnly = true)` 명시 (성능 + Snapshot 일관성)
- 컬렉션 반환 시 `Collections.unmodifiableList()` 또는 `List.copyOf()`
- 매직 넘버 금지 → 상수화 (예: `MAX_NETWORK_DEPTH = 3`)
- 주석은 **왜(Why)**를 적는다. 무엇(What)은 코드가 말한다

---

## 11. 코드 리뷰 (1인 프로젝트라도) 셀프 체크리스트

PR 작성 후 머지 전 본인이 직접 검토:

- [ ] 메서드 길이 20라인 이하?
- [ ] 생성자 주입 사용?
- [ ] Entity 직접 노출 없음?
- [ ] 커스텀 예외 사용?
- [ ] BDD 스타일 테스트 + Unhappy Path 포함?
- [ ] N+1 의심 코드 없음?
- [ ] 트랜잭션 경계 명확함?
- [ ] 로그 레벨 적절함? 개인정보 노출 없음?
- [ ] 컨셉 정합성 체크리스트 통과? (`/docs/00-overview.md` Section 6)
