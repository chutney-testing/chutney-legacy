package com.chutneytesting.execution.domain.scenario;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Optional.ofNullable;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ExecutableComposedScenario {

    public final List<ExecutableComposedFunctionalStep> functionalSteps;
    public final Map<String, String> parameters;

    private ExecutableComposedScenario(List<ExecutableComposedFunctionalStep> functionalSteps, Map<String, String> parameters) {
        this.functionalSteps = functionalSteps;
        this.parameters = parameters;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExecutableComposedScenario that = (ExecutableComposedScenario) o;
        return Objects.equals(functionalSteps, that.functionalSteps) &&
            Objects.equals(parameters, that.parameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(functionalSteps, parameters);
    }

    @Override
    public String toString() {
        return "ExecutableComposedScenario{" +
            "ExecutableComposedFunctionalSteps=" + functionalSteps +
            ", parameters=" + parameters +
            '}';
    }

    public static ExecutableComposedScenarioBuilder builder() {
        return new ExecutableComposedScenarioBuilder();
    }

    public static class ExecutableComposedScenarioBuilder {

        private List<ExecutableComposedFunctionalStep> executableComposedFunctionalSteps;
        private Map<String, String> parameters;

        private ExecutableComposedScenarioBuilder() {}

        public ExecutableComposedScenario build() {
            return new ExecutableComposedScenario(
                ofNullable(executableComposedFunctionalSteps).orElse(emptyList()),
                ofNullable(parameters).orElse(emptyMap())
            );
        }

        public ExecutableComposedScenarioBuilder withFunctionalSteps(List<ExecutableComposedFunctionalStep> ExecutableComposedFunctionalSteps) {
            this.executableComposedFunctionalSteps = ExecutableComposedFunctionalSteps;
            return this;
        }

        public ExecutableComposedScenarioBuilder withParameters(Map<String, String> parameters) {
            this.parameters = parameters;
            return this;
        }
    }

}
