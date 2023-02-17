package com.chutneytesting.scenario.domain.gwt;

import static java.util.Collections.emptyMap;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class Strategy {

    public static final Optional<Strategy> NONE = empty();
    public static final Strategy DEFAULT = new Strategy("", emptyMap());

    public final String type;
    public final Map<String, Object> parameters;

    public Strategy(String type, Map<String, Object> parameters) {
        this.type = ofNullable(type).orElse("");
        this.parameters = ofNullable(parameters).orElse(emptyMap());
    }

    @Override
    public String toString() {
        return "Strategy{" +
            "type='" + type + '\'' +
            ", parameters=" + parameters +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Strategy strategy = (Strategy) o;
        return type.equals(strategy.type) &&
            parameters.equals(strategy.parameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, parameters);
    }

}
