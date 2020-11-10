package com.chutneytesting.execution.domain.scenario;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

import com.chutneytesting.design.domain.scenario.compose.StepUsage;
import com.chutneytesting.design.domain.scenario.compose.Strategy;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

public class ExecutableComposedFunctionalStep {

    public final String id;
    public final String name;
    public final List<ExecutableComposedFunctionalStep> steps;
    public final Map<String, String> parameters;
    public final Optional<String> implementation;
    public final Optional<StepUsage> usage;
    public final Strategy strategy;
    public final Map<String, String> dataSet;
    public final List<String> tags;

    private ExecutableComposedFunctionalStep(String id, String name, List<ExecutableComposedFunctionalStep> steps, Map<String, String> parameters, Optional<String> implementation, Optional<StepUsage> usage, Strategy strategy, Map<String, String> dataSet, List<String> tags) {
        this.id = id;
        this.name = name;
        this.steps = steps;
        this.parameters = parameters;
        this.implementation = implementation;
        this.usage = usage;
        this.strategy = strategy;
        this.dataSet = dataSet;
        this.tags = tags;
    }

    // TODO - refactor dataset
    public Map<String, String> dataSetGlobalParameters() {
        return dataSet.entrySet().stream()
            .filter(e -> StringUtils.isBlank(e.getValue()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public static ExecutableComposedFunctionalStepBuilder builder() {
        return new ExecutableComposedFunctionalStepBuilder();
    }

    public static class ExecutableComposedFunctionalStepBuilder {

        private String id;
        private String name;
        private List<ExecutableComposedFunctionalStep> steps;
        private Map<String, String> parameters = new LinkedHashMap<>();
        private Optional<String> implementation;
        private Optional<StepUsage> usage;
        private Strategy strategy;
        private Map<String, String> dataSet = new LinkedHashMap<>();
        private List<String> tags = new ArrayList<>();

        private ExecutableComposedFunctionalStepBuilder() {
        }

        public ExecutableComposedFunctionalStep build() {
            return new ExecutableComposedFunctionalStep(
                ofNullable(id).orElse(""),
                ofNullable(name).orElse(""),
                ofNullable(steps).orElse(emptyList()),
                ofNullable(parameters).orElse(emptyMap()),
                ofNullable(implementation).orElse(empty()),
                ofNullable(usage).orElse(empty()),
                ofNullable(strategy).orElse(Strategy.DEFAULT),
                unmodifiableMap(ofNullable(dataSet).orElse(emptyMap())),
                unmodifiableList(ofNullable(tags).orElse(emptyList()))
            );
        }

        public ExecutableComposedFunctionalStep.ExecutableComposedFunctionalStepBuilder withId(String id) {
            this.id = id;
            return this;
        }

        public ExecutableComposedFunctionalStep.ExecutableComposedFunctionalStepBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public ExecutableComposedFunctionalStep.ExecutableComposedFunctionalStepBuilder withSteps(List<ExecutableComposedFunctionalStep> steps) {
            this.steps = unmodifiableList(steps);
            steps.forEach(ExecutableComposedFunctionalStep ->
                addDataSet(
                    ExecutableComposedFunctionalStep.dataSetGlobalParameters()
                ));
            return this;
        }

        public ExecutableComposedFunctionalStep.ExecutableComposedFunctionalStepBuilder withParameters(Map<String, String> parameters) {
            this.parameters = unmodifiableMap(parameters);
            return this;
        }

        public ExecutableComposedFunctionalStep.ExecutableComposedFunctionalStepBuilder overrideDataSetWith(Map<String, String> dataSet) {
            this.dataSet = dataSet;
            return this;
        }

        public ExecutableComposedFunctionalStep.ExecutableComposedFunctionalStepBuilder withImplementation(Optional<String> implementation) {
            this.implementation = implementation;
            return this;
        }

        public ExecutableComposedFunctionalStep.ExecutableComposedFunctionalStepBuilder withUsage(Optional<StepUsage> usage) {
            this.usage = usage;
            return this;
        }

        public ExecutableComposedFunctionalStep.ExecutableComposedFunctionalStepBuilder withStrategy(Strategy strategy) {
            this.strategy = strategy;
            return this;
        }

        public ExecutableComposedFunctionalStep.ExecutableComposedFunctionalStepBuilder addParameters(Map<String, String> parameters) {
            ofNullable(parameters).ifPresent(this.parameters::putAll);
            return this;
        }

        public ExecutableComposedFunctionalStep.ExecutableComposedFunctionalStepBuilder addDataSet(Map<String, String> dataSet) {
            ofNullable(dataSet).ifPresent(this.dataSet::putAll);
            return this;
        }

        public ExecutableComposedFunctionalStep.ExecutableComposedFunctionalStepBuilder withTags(List<String> tags) {
            this.tags = tags;
            return this;
        }

        public final ExecutableComposedFunctionalStep.ExecutableComposedFunctionalStepBuilder from(ExecutableComposedFunctionalStep instance) {
            this.id = instance.id;
            this.name = instance.name;
            this.steps = instance.steps;
            this.parameters = instance.parameters;
            this.implementation = instance.implementation;
            this.usage = instance.usage;
            this.strategy = instance.strategy;
            this.dataSet = new LinkedHashMap<>(instance.dataSet);
            return this;
        }
    }

    @Override
    public String toString() {
        return "ExecutableComposedFunctionalStep{" +
            "id='" + id + '\'' +
            ", name='" + name + '\'' +
            ", steps=" + steps +
            ", parameters=" + parameters +
            ", implementation=" + implementation +
            ", usage=" + usage +
            ", strategy=" + strategy.toString() +
            ", dataSet=" + dataSet +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExecutableComposedFunctionalStep that = (ExecutableComposedFunctionalStep) o;
        return Objects.equals(id, that.id) &&
            Objects.equals(name, that.name) &&
            Objects.equals(steps, that.steps) &&
            Objects.equals(parameters, that.parameters) &&
            Objects.equals(implementation, that.implementation) &&
            Objects.equals(usage, that.usage) &&
            Objects.equals(strategy, that.strategy) &&
            Objects.equals(dataSet, that.dataSet)
            ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, steps, parameters, implementation, usage, strategy, dataSet);
    }

}
