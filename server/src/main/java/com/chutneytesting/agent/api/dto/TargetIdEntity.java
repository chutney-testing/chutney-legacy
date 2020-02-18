package com.chutneytesting.agent.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TargetIdEntity {
    public final String name;
    public final String environment;

    public TargetIdEntity(@JsonProperty("name") String name, @JsonProperty("environment") String environment) {
        this.name = name;
        this.environment = environment;
    }
}
