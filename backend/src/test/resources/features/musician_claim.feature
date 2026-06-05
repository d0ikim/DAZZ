Feature: 뮤지션 Claim — EC-01 본인 계정 연결

  Scenario: 성공 — unclaimed 뮤지션에 본인 계정을 연결한다
    Given 뮤지션 "김재즈" (position: "PIANO") 가 등록되어 있다
    When userId 1 인 사용자가 해당 뮤지션을 claim 한다
    Then 응답 상태코드는 200 이다
    And 응답의 claimed 값은 true 이다

  Scenario: 실패 EC-01 — 이미 claim된 뮤지션을 다시 claim 시도하면 409가 반환된다
    Given 뮤지션 "이재즈" (position: "BASS") 가 등록되어 있다
    And 해당 뮤지션이 userId 10 에 이미 연결되어 있다
    When userId 20 인 사용자가 해당 뮤지션을 claim 한다
    Then 응답 상태코드는 409 이다
    And 응답의 에러코드는 "M002" 이다

  Scenario: 실패 EC-01 — 이미 다른 뮤지션과 연결된 userId는 재claim할 수 없다
    Given 뮤지션 "박재즈" (position: "DRUMS") 가 등록되어 있다
    And 해당 뮤지션이 userId 30 에 이미 연결되어 있다
    And 뮤지션 "최재즈" (position: "SAXOPHONE") 가 등록되어 있다
    When userId 30 인 사용자가 해당 뮤지션을 claim 한다
    Then 응답 상태코드는 409 이다
    And 응답의 에러코드는 "M003" 이다