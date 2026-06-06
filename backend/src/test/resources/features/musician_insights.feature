Feature: 뮤지션 인사이트 조회 — GET /api/v1/musicians/{musicianId}/insights

  Scenario: 성공 — 협업 네트워크 포함 인사이트 조회
    Given 뮤지션 "김재즈" (position: "PIANO") 가 등록되어 있다
    And 뮤지션 "이재즈" (position: "BASS") 가 등록되어 있다
    And 두 뮤지션 사이에 "COLLABORATION" 협업 관계가 등록되어 있다
    When 첫 번째 등록된 뮤지션의 인사이트를 includeNetwork true, depth 1 로 조회한다
    Then 응답 상태코드는 200 이다
    And 인사이트 응답의 profile.stageName 은 "김재즈" 이다
    And 인사이트 응답의 profile.position 은 "PIANO" 이다
    And 인사이트 응답의 network 는 1건 이상이다
    And 인사이트 응답의 network[0].name 은 "이재즈" 이다

  Scenario: 성공 — includeNetwork=false 이면 network 는 빈 배열이다
    Given 뮤지션 "박재즈" (position: "DRUMS") 가 등록되어 있다
    When 첫 번째 등록된 뮤지션의 인사이트를 includeNetwork false, depth 1 로 조회한다
    Then 응답 상태코드는 200 이다
    And 인사이트 응답의 network 는 비어있다

  Scenario: 실패 — 존재하지 않는 뮤지션 ID 요청 시 404 반환
    When musicianId 99999 로 인사이트를 직접 조회한다
    Then 응답 상태코드는 404 이다
    And 응답의 에러코드는 "M001" 이다

  Scenario: 실패 — depth 범위 초과(3) 요청 시 400 반환
    Given 뮤지션 "최재즈" (position: "SAXOPHONE") 가 등록되어 있다
    When 첫 번째 등록된 뮤지션의 인사이트를 includeNetwork true, depth 3 으로 조회한다
    Then 응답 상태코드는 400 이다
    And 응답의 에러코드는 "COM001" 이다