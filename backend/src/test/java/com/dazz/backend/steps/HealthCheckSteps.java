package com.dazz.backend.steps;

import com.dazz.backend.support.TestAdapter;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

public class HealthCheckSteps {

    @Autowired
    private TestAdapter testAdapter;

    private Response response;

    @Given("서버가 실행 중이다")
    public void 서버가_실행_중이다() {
        // RANDOM_PORT로 앱이 이미 기동된 상태 — 별도 동작 없음
    }

    @When("클라이언트가 {string} 에 GET 요청을 보낸다")
    public void 클라이언트가_GET_요청을_보낸다(String path) {
        response = testAdapter.get(path);
    }

    @Then("응답 상태코드는 {int} 이다")
    public void 응답_상태코드는(int statusCode) {
        assertThat(response.statusCode()).isEqualTo(statusCode);
    }

    @And("응답 body 의 status 는 {string} 이다")
    public void 응답_body의_status는(String status) {
        assertThat(response.jsonPath().getString("status")).isEqualTo(status);
    }
}
