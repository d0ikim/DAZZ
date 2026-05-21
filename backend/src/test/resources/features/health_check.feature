Feature: Health Check

  Scenario: 서버 정상 기동 시 health 엔드포인트가 UP 상태를 반환한다
    Given 서버가 실행 중이다
    When 클라이언트가 "/actuator/health" 에 GET 요청을 보낸다
    Then 응답 상태코드는 200 이다
    And 응답 body 의 status 는 "UP" 이다
