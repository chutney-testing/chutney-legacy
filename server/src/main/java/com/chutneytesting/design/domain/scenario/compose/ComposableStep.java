package com.chutneytesting.design.domain.scenario.compose;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

import java.util.ArrayList;
import java.util.Collections;
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
    public final Map<String, String> builtInParameters;
    public final Optional<String> implementation;
    public final Strategy strategy;
    public final Map<String, String> enclosedUsageParameters; // TODO - Maybe separate list with blank values
    public final List<String> tags;

    private ComposableStep(String id, String name, List<ComposableStep> steps, Map<String, String> builtInParameters, Optional<String> implementation, Strategy strategy, Map<String, String> enclosedUsageParameters, List<String> tags) {
        this.id = id;
        this.name = name;
        this.steps = steps;
        this.builtInParameters = builtInParameters;
        this.implementation = implementation;
        this.strategy = strategy;
        this.enclosedUsageParameters = enclosedUsageParameters;
        this.tags = tags;
    }

    public boolean hasCyclicDependencies() {
        return checkCyclicDependency(this, new ArrayList<>());
    }

    private boolean checkCyclicDependency(ComposableStep composableStep, List<String> parentsAcc) {
        if (composableStep.steps.isEmpty()) {
            return false;
        }

        parentsAcc.add(composableStep.id);
        List<String> childrenIds = composableStep.steps.stream()
            .map(cs -> cs.id)
            .collect(Collectors.toList());

        if (!Collections.disjoint(childrenIds, parentsAcc)) {
            return true;
        }

        return composableStep.steps.stream()
            .anyMatch(cs -> checkCyclicDependency(cs, new ArrayList<>(parentsAcc)));
    }

    // TODO - refactor dataset
    public Map<String, String> dataSetGlobalParameters() {
        return enclosedUsageParameters.entrySet().stream()
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
        private Map<String, String> builtInParameters = new LinkedHashMap<>();
        private Optional<String> implementation;
        private Strategy strategy;
        private Map<String, String> enclosedUsageParameters = new LinkedHashMap<>();
        private List<String> tags = new ArrayList<>();

        private ComposableStepBuilder() {
        }

        public ComposableStep build() {
            ComposableStep composableStep = new ComposableStep(
                ofNullable(id).orElse(""),
                ofNullable(name).orElse(""),
                ofNullable(steps).orElse(emptyList()),
                ofNullable(builtInParameters).orElse(emptyMap()),
                ofNullable(implementation).orElse(empty()),
                ofNullable(strategy).orElse(Strategy.DEFAULT),
                unmodifiableMap(ofNullable(enclosedUsageParameters).orElse(emptyMap())),
                unmodifiableList(ofNullable(tags).orElse(emptyList()))
            );

            if (composableStep.hasCyclicDependencies()) {
                throw new ComposableStepCyclicDependencyException(composableStep.id, composableStep.name);
            }

            return composableStep;
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
                addEnclosedUsageParameters(
                    composableStep.dataSetGlobalParameters()
                ));
            return this;
        }

        public ComposableStepBuilder withBuiltInParameters(Map<String, String> builtInParameters) {
            this.builtInParameters = unmodifiableMap(builtInParameters);
            return this;
        }

        public ComposableStepBuilder overrideEnclosedUsageParametersWith(Map<String, String> enclosedUsageParameters) {
            this.enclosedUsageParameters = enclosedUsageParameters;
            return this;
        }

        public ComposableStepBuilder withImplementation(Optional<String> implementation) {
            this.implementation = implementation;
            return this;
        }

        public ComposableStepBuilder withStrategy(Strategy strategy) {
            this.strategy = strategy;
            return this;
        }

        public ComposableStepBuilder addBuiltInParameters(Map<String, String> builtInParameters) {
            ofNullable(builtInParameters).ifPresent(this.builtInParameters::putAll);
            return this;
        }

        public ComposableStepBuilder addEnclosedUsageParameters(Map<String, String> enclosedUsageParameters) {
            ofNullable(enclosedUsageParameters).ifPresent(this.enclosedUsageParameters::putAll);
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
            this.builtInParameters = instance.builtInParameters;
            this.implementation = instance.implementation;
            this.strategy = instance.strategy;
            this.enclosedUsageParameters = new LinkedHashMap<>(instance.enclosedUsageParameters);
            return this;
        }
    }

    @Override
    public String toString() {
        return "ComposableStep{" +
            "id='" + id + '\'' +
            ", name='" + name + '\'' +
            ", steps=" + steps +
            ", parameters=" + builtInParameters +
            ", implementation=" + implementation +
            ", strategy=" + strategy.toString() +
            ", dataSet=" + enclosedUsageParameters +
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
            Objects.equals(builtInParameters, that.builtInParameters) &&
            Objects.equals(implementation, that.implementation) &&
            Objects.equals(strategy, that.strategy) &&
            Objects.equals(enclosedUsageParameters, that.enclosedUsageParameters)
            ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, steps, builtInParameters, implementation, strategy, enclosedUsageParameters);
    }

}
