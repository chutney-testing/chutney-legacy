package com.chutneytesting.design.domain.scenario.compose;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

public class ComposableStep {

    public final String id;
    public final String name;
    public final List<ComposableStep> steps;
    public final Map<String, String> parameters;
    public final Optional<String> implementation;
    public final Optional<StepUsage> usage;
    public final Strategy strategy;
    public final Map<String, String> dataSet;
    public final List<String> tags;

    private ComposableStep(String id, String name, List<ComposableStep> steps, Map<String, String> parameters, Optional<String> implementation, Optional<StepUsage> usage, Strategy strategy, Map<String, String> dataSet, List<String> tags) {
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

    public static ComposableStepBuilder builder() {
        return new ComposableStepBuilder();
    }

    public static class ComposableStepBuilder {

        private String id;
        private String name;
        private List<ComposableStep> steps;
        private Map<String, String> parameters = new LinkedHashMap<>();
        private Optional<String> implementation;
        private Optional<StepUsage> usage;
        private Strategy strategy;
        private Map<String, String> dataSet = new LinkedHashMap<>();
        private List<String> tags = new ArrayList<>();

        private ComposableStepBuilder() {
        }

        public ComposableStep build() {
            return new ComposableStep(
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

        public ComposableStepBuilder withId(String id) {
            this.id = id;
            return this;
        }

        public ComposableStepBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public ComposableStepBuilder withSteps(List<ComposableStep> steps) {
            this.steps = unmodifiableList(steps);
            steps.forEach(composableStep ->
                addDataSet(
                    composableStep.dataSetGlobalParameters()
                ));
            return this;
        }

        public ComposableStepBuilder withParameters(Map<String, String> parameters) {
            this.parameters = unmodifiableMap(parameters);
            return this;
        }

        public ComposableStepBuilder overrideDataSetWith(Map<String, String> dataSet) {
            this.dataSet = dataSet;
            return this;
        }

        public ComposableStepBuilder withImplementation(Optional<String> implementation) {
            this.implementation = implementation;
            return this;
        }

        public ComposableStepBuilder withUsage(Optional<StepUsage> usage) {
            this.usage = usage;
            return this;
        }

        public ComposableStepBuilder withStrategy(Strategy strategy) {
            this.strategy = strategy;
            return this;
        }

        public ComposableStepBuilder addParameters(Map<String, String> parameters) {
            ofNullable(parameters).ifPresent(this.parameters::putAll);
            return this;
        }

        public ComposableStepBuilder addDataSet(Map<String, String> dataSet) {
            ofNullable(dataSet).ifPresent(this.dataSet::putAll);
            return this;
        }

        public ComposableStepBuilder withTags(List<String> tags) {
            this.tags = tags;
            return this;
        }

        public final ComposableStepBuilder from(ComposableStep instance) {
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
        return "ComposableStep{" +
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
        ComposableStep that = (ComposableStep) o;
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
