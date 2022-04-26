package com.chutneytesting.scenario.api.raw.dto;

import java.util.Collections;
import java.util.Map;

public class StrategyDto {
    private String type;
    private Map<String, Object> parameters;

    public StrategyDto() {
        this("", Collections.emptyMap());
    }

    public StrategyDto(String type, Map<String, Object> parameters) {
        this.type = type;
        this.parameters = parameters;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }
}
