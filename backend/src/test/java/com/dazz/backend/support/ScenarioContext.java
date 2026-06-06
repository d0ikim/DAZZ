package com.dazz.backend.support;

import io.restassured.response.Response;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Scope("cucumber-glue")
public class ScenarioContext {

    public Response lastResponse;
    public String lastMusicianUuid;
    public final List<Long> musicianIds = new ArrayList<>();
}