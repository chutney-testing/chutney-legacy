package com.chutneytesting.scenario.domain;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Optional.ofNullable;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ComposableScenario {

    public final List<ComposableStep> composableSteps;
    public final Map<String, String> parameters;

    private ComposableScenario(List<ComposableStep> composableSteps, Map<String, String> parameters) {
        this.composableSteps = composableSteps;
        this.parameters = parameters;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ComposableScenario that = (ComposableScenario) o;
        return Objects.equals(composableSteps, that.composableSteps) &&
            Objects.equals(parameters, that.parameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(composableSteps, parameters);
    }

    @Override
    public String toString() {
        return "ComposableScenario{" +
            "composableSteps=" + composableSteps +
            ", parameters=" + parameters +
            '}';
    }

    public static ComposableScenarioBuilder builder() {
        return new ComposableScenarioBuilder();
    }

    public static class ComposableScenarioBuilder {

        private List<ComposableStep> composableSteps;
        private Map<String, String> parameters;

        private ComposableScenarioBuilder() {}

        public ComposableScenario build() {
            return new ComposableScenario(
                ofNullable(composableSteps).orElse(emptyList()),
                ofNullable(parameters).orElse(emptyMap())
            );
        }

        public ComposableScenarioBuilder withComposableSteps(List<ComposableStep> composableSteps) {
            this.composableSteps = composableSteps;
            return this;
        }

        public ComposableScenarioBuilder withParameters(Map<String, String> parameters) {
            this.parameters = parameters;
            return this;
        }
    }
}
