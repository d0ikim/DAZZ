package com.dazz.backend.steps;

import com.dazz.backend.support.ScenarioContext;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

public class CommonSteps {

    @Autowired
    private ScenarioContext context;

    @Then("응답 상태코드는 {int} 이다")
    public void 응답_상태코드(int statusCode) {
        assertThat(context.lastResponse.statusCode()).isEqualTo(statusCode);
    }

    @And("응답의 에러코드는 {string} 이다")
    public void 응답_에러코드(String errorCode) {
        assertThat(context.lastResponse.jsonPath().getString("error.code")).isEqualTo(errorCode);
    }
}