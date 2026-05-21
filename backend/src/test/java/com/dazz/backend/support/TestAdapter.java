package com.dazz.backend.support;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class TestAdapter {

    @Autowired
    private Environment environment;

    // Tomcat이 완전히 뜬 후(테스트 실행 시점)에 조회 — 빈 생성 시점에는 아직 미등록
    private int getPort() {
        return environment.getProperty("local.server.port", Integer.class, 8080);
    }

    public Response get(String path) {
        return RestAssured.given()
                .port(getPort())
                .baseUri("http://localhost")
                .when()
                .get(path);
    }

    public Response post(String path, Object body) {
        return RestAssured.given()
                .port(getPort())
                .baseUri("http://localhost")
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post(path);
    }
}
