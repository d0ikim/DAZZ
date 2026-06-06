package com.dazz.backend.steps;

import com.dazz.backend.support.ScenarioContext;
import com.dazz.backend.support.TestAdapter;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class MusicianClaimSteps {

    @Autowired
    private TestAdapter testAdapter;

    @Autowired
    private ScenarioContext context;

    @Given("뮤지션 {string} \\(position: {string}\\) 가 등록되어 있다")
    public void 뮤지션이_등록되어_있다(String stageName, String position) {
        Response r = testAdapter.post("/api/v1/musicians",
                Map.of("stageName", stageName, "position", position));
        assertThat(r.statusCode()).isEqualTo(201);
        context.lastMusicianUuid = r.jsonPath().getString("data.uuid");
        context.musicianIds.add(r.jsonPath().getLong("data.id"));
    }

    @And("해당 뮤지션이 userId {long} 에 이미 연결되어 있다")
    public void 해당_뮤지션이_이미_연결되어_있다(long userId) {
        Response r = testAdapter.post("/api/v1/musicians/" + context.lastMusicianUuid + "/claim",
                Map.of("userId", userId));
        assertThat(r.statusCode()).isEqualTo(200);
    }

    @When("userId {long} 인 사용자가 해당 뮤지션을 claim 한다")
    public void 사용자가_claim_한다(long userId) {
        context.lastResponse = testAdapter.post(
                "/api/v1/musicians/" + context.lastMusicianUuid + "/claim",
                Map.of("userId", userId));
    }

    @And("응답의 claimed 값은 true 이다")
    public void 응답의_claimed_값은_true이다() {
        assertThat(context.lastResponse.jsonPath().getBoolean("data.claimed")).isTrue();
    }
}