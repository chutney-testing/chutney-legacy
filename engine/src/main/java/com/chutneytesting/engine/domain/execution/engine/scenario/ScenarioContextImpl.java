package com.chutneytesting.engine.domain.execution.engine.scenario;

import java.util.HashMap;

@SuppressWarnings("serial")
public class ScenarioContextImpl extends HashMap<String, Object> implements ScenarioContext {

    public ScenarioContextImpl() {
        super();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getOrDefault(String key, T defaultValue) {
        return (T) super.getOrDefault(key, defaultValue);
    }

}
