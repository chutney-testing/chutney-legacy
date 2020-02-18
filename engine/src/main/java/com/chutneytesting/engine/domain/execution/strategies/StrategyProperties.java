package com.chutneytesting.engine.domain.execution.strategies;

import java.util.HashMap;
import java.util.Map;

/**
 * Strategy parameters.
 */
@SuppressWarnings("serial")
public class StrategyProperties extends HashMap<String, Object> {

    public StrategyProperties() {
        super();
    }

    public StrategyProperties(Map<String, Object> data) {
        super(data);
    }

    public <T> T getProperty(String key, Class<T> type) {
        return type.cast(get(key));
    }

    public StrategyProperties setProperty(String key, Object value) {
        put(key, value);
        return this;
    }
}
