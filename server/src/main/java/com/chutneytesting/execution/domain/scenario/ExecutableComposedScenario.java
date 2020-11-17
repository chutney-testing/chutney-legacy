package com.chutneytesting.execution.domain.scenario;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Optional.ofNullable;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ExecutableComposedScenario {

    public final List<ExecutableComposedStep> composedSteps;
    public final Map<String, String> parameters;

    private ExecutableComposedScenario(List<ExecutableComposedStep> composedSteps, Map<String, String> parameters) {
        this.composedSteps = composedSteps;
        this.parameters = parameters;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExecutableComposedScenario that = (ExecutableComposedScenario) o;
        return Objects.equals(composedSteps, that.composedSteps) &&
            Objects.equals(parameters, that.parameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(composedSteps, parameters);
    }

    @Override
    public String toString() {
        return "ExecutableComposedScenario{" +
            "ExecutableComposedSteps=" + composedSteps +
            ", parameters=" + parameters +
            '}';
    }

    public static ExecutableComposedScenarioBuilder builder() {
        return new ExecutableComposedScenarioBuilder();
    }

    public static class ExecutableComposedScenarioBuilder {

        private List<ExecutableComposedStep> executableComposedSteps;
        private Map<String, String> parameters;

        private ExecutableComposedScenarioBuilder() {}

        public ExecutableComposedScenario build() {
            return new ExecutableComposedScenario(
                ofNullable(executableComposedSteps).orElse(emptyList()),
                ofNullable(parameters).orElse(emptyMap())
            );
        }

        public ExecutableComposedScenarioBuilder withComposedSteps(List<ExecutableComposedStep> executableComposedSteps) {
            this.executableComposedSteps = executableComposedSteps;
            return this;
        }

        public ExecutableComposedScenarioBuilder withParameters(Map<String, String> parameters) {
            this.parameters = parameters;
            return this;
        }
    }

}
