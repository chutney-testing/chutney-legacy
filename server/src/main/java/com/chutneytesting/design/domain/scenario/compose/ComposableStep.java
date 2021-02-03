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
    public final Map<String, String> defaultParameters; // default parameters defined when editing the component alone
    public final Optional<String> implementation;
    public final Strategy strategy;
    public final Map<String, String> executionParameters; // override default parameters values when the component is used inside another component // TODO - Maybe separate list with blank values
    public final List<String> tags;

    private ComposableStep(String id, String name, List<ComposableStep> steps, Map<String, String> defaultParameters, Optional<String> implementation, Strategy strategy, Map<String, String> executionParameters, List<String> tags) {
        this.id = id;
        this.name = name;
        this.steps = steps;
        this.defaultParameters = defaultParameters;
        this.implementation = implementation;
        this.strategy = strategy;
        this.executionParameters = executionParameters;
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

    public Map<String, String> getEmptyExecutionParameters() {
        return executionParameters.entrySet().stream()
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
        private Map<String, String> defaultParameters = new LinkedHashMap<>();
        private Optional<String> implementation;
        private Strategy strategy;
        private Map<String, String> executionParameters = new LinkedHashMap<>();
        private List<String> tags = new ArrayList<>();

        private ComposableStepBuilder() {
        }

        public ComposableStep build() {
            ComposableStep composableStep = new ComposableStep(
                ofNullable(id).orElse(""),
                ofNullable(name).orElse(""),
                ofNullable(steps).orElse(emptyList()),
                ofNullable(defaultParameters).orElse(emptyMap()),
                ofNullable(implementation).orElse(empty()),
                ofNullable(strategy).orElse(Strategy.DEFAULT),
                unmodifiableMap(ofNullable(executionParameters).orElse(emptyMap())),
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
            bubbleUpEmptyParams(steps);
            return this;
        }

        private void bubbleUpEmptyParams(List<ComposableStep> steps) { // TODO - dont do it while adding steps, do it after in build() eventually
            steps.forEach(composableStep ->
                addExecutionParameters(
                    composableStep.getEmptyExecutionParameters()
                ));
        }

        public ComposableStepBuilder withDefaultParameters(Map<String, String> defaultParameters) {
            this.defaultParameters = unmodifiableMap(defaultParameters);
            return this;
        }

        public ComposableStepBuilder addDefaultParameters(Map<String, String> defaultParameters) {
            ofNullable(defaultParameters).ifPresent(this.defaultParameters::putAll);
            return this;
        }

        public ComposableStepBuilder overrideExecutionParametersWith(Map<String, String> executionParameters) {
            this.executionParameters = executionParameters;
            return this;
        }

        public ComposableStepBuilder addExecutionParameters(Map<String, String> executionParameters) { // TODO - only used in orient, try to remove its use there
            ofNullable(executionParameters).ifPresent(this.executionParameters::putAll);
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

        public ComposableStepBuilder withTags(List<String> tags) {
            this.tags = tags;
            return this;
        }

        public final ComposableStepBuilder from(ComposableStep instance) {
            this.id = instance.id;
            this.name = instance.name;
            this.steps = instance.steps;
            this.defaultParameters = instance.defaultParameters;
            this.implementation = instance.implementation;
            this.strategy = instance.strategy;
            this.executionParameters = new LinkedHashMap<>(instance.executionParameters);
            return this;
        }
    }

    @Override
    public String toString() {
        return "ComposableStep{" +
            "id='" + id + '\'' +
            ", name='" + name + '\'' +
            ", steps=" + steps +
            ", defaultParameters=" + defaultParameters +
            ", implementation=" + implementation +
            ", strategy=" + strategy.toString() +
            ", executionParameters=" + executionParameters +
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
            Objects.equals(defaultParameters, that.defaultParameters) &&
            Objects.equals(implementation, that.implementation) &&
            Objects.equals(strategy, that.strategy) &&
            Objects.equals(executionParameters, that.executionParameters)
            ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, steps, defaultParameters, implementation, strategy, executionParameters);
    }

}
