package com.chutneytesting.design.domain.scenario.compose;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Optional.ofNullable;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ComposableScenario {

    public final List<FunctionalStep> functionalSteps;
    public final Map<String, String> parameters;

    private ComposableScenario(List<FunctionalStep> functionalSteps, Map<String, String> parameters) {
        this.functionalSteps = functionalSteps;
        this.parameters = parameters;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ComposableScenario that = (ComposableScenario) o;
        return Objects.equals(functionalSteps, that.functionalSteps) &&
            Objects.equals(parameters, that.parameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(functionalSteps, parameters);
    }

    @Override
    public String toString() {
        return "ComposableScenario{" +
            "functionalSteps=" + functionalSteps +
            ", parameters=" + parameters +
            '}';
    }

    public static ComposableScenarioBuilder builder() {
        return new ComposableScenarioBuilder();
    }

    public static class ComposableScenarioBuilder {

        private List<FunctionalStep> functionalSteps;
        private Map<String, String> parameters;

        private ComposableScenarioBuilder() {}

        public ComposableScenario build() {
            return new ComposableScenario(
                ofNullable(functionalSteps).orElse(emptyList()),
                ofNullable(parameters).orElse(emptyMap())
            );
        }

        public ComposableScenarioBuilder withFunctionalSteps(List<FunctionalStep> functionalSteps) {
            this.functionalSteps = functionalSteps;
            return this;
        }

        public ComposableScenarioBuilder withParameters(Map<String, String> parameters) {
            this.parameters = parameters;
            return this;
        }
    }
}
