package com.chutneytesting.scenario.domain;

import static java.util.Collections.emptyMap;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

public class Strategy {

    public static final Strategy DEFAULT = new Strategy("", emptyMap());

    public final String type;
    public final Map<String, Object> parameters;

    public Strategy(String type, Map<String, Object> parameters) {
        this.type = type;
        this.parameters = Collections.unmodifiableMap(parameters);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Strategy strategy = (Strategy) o;
        return Objects.equals(type, strategy.type) &&
            Objects.equals(parameters, strategy.parameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, parameters);
    }

    @Override
    public String toString() {
        return "Strategy{" +
            "type='" + type + '\'' +
            ", parameters=" + parameters +
            '}';
    }
}
