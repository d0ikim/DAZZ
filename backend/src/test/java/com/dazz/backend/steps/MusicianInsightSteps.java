package com.dazz.backend.steps;

import com.dazz.backend.support.ScenarioContext;
import com.dazz.backend.support.TestAdapter;
import io.cucumber.java.en.And;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class MusicianInsightSteps {

    @Autowired
    private TestAdapter testAdapter;

    @Autowired
    private ScenarioContext context;

    @And("두 뮤지션 사이에 {string} 협업 관계가 등록되어 있다")
    public void 협업_관계_등록(String relationType) {
        assertThat(context.musicianIds).hasSizeGreaterThanOrEqualTo(2);
        Long fromId = context.musicianIds.get(0);
        Long toId = context.musicianIds.get(1);

        var r = testAdapter.postWithIdempotencyKey(
                "/api/v1/collaborations",
                Map.of("fromMusicianId", fromId, "toMusicianId", toId, "relationType", relationType),
                UUID.randomUUID().toString()
        );
        assertThat(r.statusCode()).isIn(200, 201);
    }

    @When("첫 번째 등록된 뮤지션의 인사이트를 includeNetwork {word}, depth {int} 로 조회한다")
    public void 인사이트_조회(String includeNetwork, int depth) {
        Long musicianId = context.musicianIds.get(0);
        context.lastResponse = testAdapter.get(
                "/api/v1/musicians/" + musicianId + "/insights",
                Map.of("includeNetwork", includeNetwork, "depth", depth)
        );
    }

    @When("첫 번째 등록된 뮤지션의 인사이트를 includeNetwork {word}, depth {int} 으로 조회한다")
    public void 인사이트_조회_으로(String includeNetwork, int depth) {
        인사이트_조회(includeNetwork, depth);
    }

    @When("musicianId {long} 로 인사이트를 직접 조회한다")
    public void 존재하지않는_뮤지션_인사이트_조회(long musicianId) {
        context.lastResponse = testAdapter.get(
                "/api/v1/musicians/" + musicianId + "/insights",
                Map.of("includeNetwork", "true", "depth", 1)
        );
    }

    @And("인사이트 응답의 profile.stageName 은 {string} 이다")
    public void 프로필_stageName_검증(String expected) {
        assertThat(context.lastResponse.jsonPath().getString("data.profile.stageName"))
                .isEqualTo(expected);
    }

    @And("인사이트 응답의 profile.position 은 {string} 이다")
    public void 프로필_position_검증(String expected) {
        assertThat(context.lastResponse.jsonPath().getString("data.profile.position"))
                .isEqualTo(expected);
    }

    @And("인사이트 응답의 network 는 1건 이상이다")
    public void 네트워크_비어있지않음() {
        List<?> network = context.lastResponse.jsonPath().getList("data.network");
        assertThat(network).isNotEmpty();
    }

    @And("인사이트 응답의 network[0].name 은 {string} 이다")
    public void 네트워크_첫번째_name_검증(String expected) {
        assertThat(context.lastResponse.jsonPath().getString("data.network[0].name"))
                .isEqualTo(expected);
    }

    @And("인사이트 응답의 network 는 비어있다")
    public void 네트워크_비어있음() {
        List<?> network = context.lastResponse.jsonPath().getList("data.network");
        assertThat(network).isEmpty();
    }
}