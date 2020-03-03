package com.chutneytesting.task.http.jackson;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.github.tomakehurst.wiremock.WireMockServer;

public class WiremockModule extends SimpleModule {

    private static final String NAME = "CustomIntervalModule";

    public WiremockModule() {
        super(NAME);
        addSerializer(WireMockServer.class, new WireMockServerSerializer());
    }
}
