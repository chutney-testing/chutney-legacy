package com.chutneytesting.execution.domain.scenario;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

import com.chutneytesting.design.domain.scenario.compose.Strategy;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

public class ExecutableComposedStep {

    public final String name;
    public final List<ExecutableComposedStep> steps;
    public final Map<String, String> parameters;
    public final Optional<String> implementation;
    public final Strategy strategy;
    public final Map<String, String> dataset;

    private ExecutableComposedStep(String name, List<ExecutableComposedStep> steps, Map<String, String> parameters, Optional<String> implementation, Strategy strategy, Map<String, String> dataset) {
        this.name = name;
        this.steps = steps;
        this.parameters = parameters;
        this.implementation = implementation;
        this.strategy = strategy;
        this.dataset = dataset;
    }

    // TODO - refactor dataset
    public Map<String, String> dataSetGlobalParameters() {
        return dataset.entrySet().stream()
            .filter(e -> StringUtils.isBlank(e.getValue()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public static ExecutableComposedStepBuilder builder() {
        return new ExecutableComposedStepBuilder();
    }

    public static class ExecutableComposedStepBuilder {

        private String name;
        private List<ExecutableComposedStep> steps;
        private Map<String, String> parameters = new LinkedHashMap<>();
        private Optional<String> implementation;
        private Strategy strategy;
        private Map<String, String> dataSet = new LinkedHashMap<>();

        private ExecutableComposedStepBuilder() {}

        public ExecutableComposedStep build() {
            return new ExecutableComposedStep(
                ofNullable(name).orElse(""),
                ofNullable(steps).orElse(emptyList()),
                ofNullable(parameters).orElse(emptyMap()),
                ofNullable(implementation).orElse(empty()),
                ofNullable(strategy).orElse(Strategy.DEFAULT),
                unmodifiableMap(ofNullable(dataSet).orElse(emptyMap()))
            );
        }

        public ExecutableComposedStepBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public ExecutableComposedStepBuilder withSteps(List<ExecutableComposedStep> steps) {
            this.steps = unmodifiableList(steps);
            steps.forEach(composedStep ->
                addDataSet(
                    composedStep.dataSetGlobalParameters()
                ));
            return this;
        }

        public ExecutableComposedStepBuilder withParameters(Map<String, String> parameters) {
            this.parameters = unmodifiableMap(parameters);
            return this;
        }

        public ExecutableComposedStepBuilder overrideDataSetWith(Map<String, String> dataSet) {
            this.dataSet = dataSet;
            return this;
        }

        public ExecutableComposedStepBuilder withImplementation(Optional<String> implementation) {
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

        public ExecutableComposedStepBuilder addDataSet(Map<String, String> dataSet) {
            ofNullable(dataSet).ifPresent(this.dataSet::putAll);
            return this;
        }

        public final ExecutableComposedStepBuilder from(ExecutableComposedStep instance) {
            this.name = instance.name;
            this.steps = instance.steps;
            this.parameters = instance.parameters;
            this.implementation = instance.implementation;
            this.strategy = instance.strategy;
            this.dataSet = new LinkedHashMap<>(instance.dataset);
            return this;
        }
    }

    @Override
    public String toString() {
        return "ExecutableComposedStep{" +
            ", name='" + name + '\'' +
            ", steps=" + steps +
            ", parameters=" + parameters +
            ", implementation=" + implementation +
            ", strategy=" + strategy.toString() +
            ", dataSet=" + dataset +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExecutableComposedStep that = (ExecutableComposedStep) o;
        return Objects.equals(name, that.name) &&
            Objects.equals(steps, that.steps) &&
            Objects.equals(parameters, that.parameters) &&
            Objects.equals(implementation, that.implementation) &&
            Objects.equals(strategy, that.strategy) &&
            Objects.equals(dataset, that.dataset)
            ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, steps, parameters, implementation, strategy, dataset);
    }

}
