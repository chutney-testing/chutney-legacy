package com.chutneytesting.execution.domain.scenario.composed;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;

import com.chutneytesting.design.domain.scenario.compose.Strategy;
import com.google.common.collect.Maps;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;

public class ExecutableComposedStep {

    public final String name;
    public final List<ExecutableComposedStep> steps;
    public final Map<String, String> defaultParameters; // TODO - maybe we dont need it here, everything should be calculated into ONE map for execution
    public final Optional<StepImplementation> stepImplementation;
    public final Strategy strategy;
    public final Map<String, String> executionParameters;

    private ExecutableComposedStep(String name, List<ExecutableComposedStep> steps, Map<String, String> defaultParameters, Optional<StepImplementation> implementation, Strategy strategy, Map<String, String> executionParameters) {
        this.name = name;
        this.steps = steps;
        this.defaultParameters = defaultParameters;
        this.stepImplementation = implementation;
        this.strategy = strategy;
        this.executionParameters = executionParameters;
    }

    public Map<String, String> getEmptyExecutionParameters() {
        return executionParameters.entrySet().stream()
            .filter(e -> StringUtils.isBlank(e.getValue()))
            .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public static ExecutableComposedStepBuilder builder() {
        return new ExecutableComposedStepBuilder();
    }

    public static class ExecutableComposedStepBuilder {

        private String name;
        private List<ExecutableComposedStep> steps = emptyList();
        private Map<String, String> parameters = emptyMap();
        private Optional<StepImplementation> implementation;
        private Strategy strategy;
        private Map<String, String> executionParameters = emptyMap();

        private ExecutableComposedStepBuilder() {}

        public ExecutableComposedStep build() {
            return new ExecutableComposedStep(
                ofNullable(name).orElse(""),
                ofNullable(steps).orElse(emptyList()),
                ofNullable(parameters).orElse(emptyMap()),
                ofNullable(implementation).orElse(empty()),
                ofNullable(strategy).orElse(Strategy.DEFAULT),
                unmodifiableMap(buildExecutionParameters())
            );
        }

        private Map<String, String> buildExecutionParameters() {
            if (executionParameters.isEmpty()) {
                Map<String,String> result = Maps.newHashMap();
                steps.stream()
                    .map(ExecutableComposedStep::getEmptyExecutionParameters)
                    .filter(m -> !m.isEmpty())
                    .forEach(m -> result.putAll(m));
                return result;
            }

            return executionParameters;
        }

        public ExecutableComposedStepBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public ExecutableComposedStepBuilder withSteps(List<ExecutableComposedStep> steps) {
            this.steps = unmodifiableList(steps);
            return this;
        }

        public ExecutableComposedStepBuilder withParameters(Map<String, String> parameters) {
            this.parameters = unmodifiableMap(parameters);
            return this;
        }

        public ExecutableComposedStepBuilder withExecutionParameters(Map<String, String> executionParameters) {
            this.executionParameters = ofNullable(executionParameters).orElse(emptyMap());
            return this;
        }

        public ExecutableComposedStepBuilder withImplementation(Optional<StepImplementation> implementation) {
            this.implementation = implementation;
            return this;
        }

        public ExecutableComposedStepBuilder withStrategy(Strategy strategy) {
            this.strategy = strategy;
            return this;
        }

        public ExecutableComposedStepBuilder addParameters(Map<String, String> parameters) {
            ofNullable(parameters).ifPresent(this.parameters::putAll);
            return this;
        }

        public final ExecutableComposedStepBuilder from(ExecutableComposedStep instance) {
            this.name = instance.name;
            this.steps = instance.steps;
            this.parameters = instance.defaultParameters;
            this.implementation = instance.stepImplementation;
            this.strategy = instance.strategy;
            this.executionParameters = new LinkedHashMap<>(instance.executionParameters);
            return this;
        }
    }

    @Override
    public String toString() {
        return "ExecutableComposedStep{" +
            ", name='" + name + '\'' +
            ", steps=" + steps +
            ", parameters=" + defaultParameters +
            ", implementation=" + stepImplementation +
            ", strategy=" + strategy.toString() +
            ", dataSet=" + executionParameters +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExecutableComposedStep that = (ExecutableComposedStep) o;
        return Objects.equals(name, that.name) &&
            Objects.equals(steps, that.steps) &&
            Objects.equals(defaultParameters, that.defaultParameters) &&
            Objects.equals(stepImplementation, that.stepImplementation) &&
            Objects.equals(strategy, that.strategy) &&
            Objects.equals(executionParameters, that.executionParameters)
            ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, steps, defaultParameters, stepImplementation, strategy, executionParameters);
    }

}
