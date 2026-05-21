package com.dazz.backend.support;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.http.ContentType;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.stereotype.Component;

@Component
public class TestAdapter {

    @LocalServerPort    // RANDOM_PORT로 뜬 실제 포트번호를 자동주입
    private int port;

    @PostConstruct  // 앱 시작시 RestAssured에 포트/URL 한번만 세팅
    void setUp() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";
    }

    public Response get(String path) {  // 인증없이 GET요청
        return RestAssured.given()
                .when()
                .get(path);
    }

    public Response post(String path, Object body) {    // JSON body로 POST 요청
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post(path);
    }
}