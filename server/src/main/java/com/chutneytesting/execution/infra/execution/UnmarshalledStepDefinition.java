package com.chutneytesting.execution.infra.execution;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Optional.ofNullable;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import java.util.List;
import java.util.Map;

@JsonDeserialize(builder = UnmarshalledStepDefinition.UnmarshalledStepDefinitionBuilder.class)
class UnmarshalledStepDefinition {

    enum GwtType {
        ROOT_STEP,
        GIVEN, WHEN, THEN
    }

    public final GwtType gwtType;
    public final String name;
    public final String target;
    public final String type;
    public final UnmarshalledStepStrategyDefinition strategy;
    public final Map<String, Object> inputs;
    public final List<UnmarshalledStepDefinition> steps;
    public final Map<String, Object> outputs;

    private UnmarshalledStepDefinition(GwtType gwtType, String name, String target, String type, UnmarshalledStepStrategyDefinition strategy, Map<String, Object> inputs, List<UnmarshalledStepDefinition> steps, Map<String, Object> outputs) {
        this.gwtType = gwtType;
        this.name = name;
        this.target = target;
        this.type = type;
        this.strategy = strategy;
        this.inputs = inputs;
        this.steps = steps;
        this.outputs = outputs;
    }

    public static UnmarshalledStepDefinitionBuilder builder() {
        return new UnmarshalledStepDefinitionBuilder();
    }

    @JsonPOJOBuilder
    public static class UnmarshalledStepDefinitionBuilder {

        private GwtType gwtType;
        private String name;
        private String target;
        private String type;
        private UnmarshalledStepStrategyDefinition strategy;
        private Map<String, Object> inputs;
        private List<UnmarshalledStepDefinition> steps;
        private Map<String, Object> outputs;

        private UnmarshalledStepDefinitionBuilder() {}

        public UnmarshalledStepDefinition build() {
            return new UnmarshalledStepDefinition(
                gwtType,
                ofNullable(name).orElse(""),
                ofNullable(target).orElse(""),
                ofNullable(type).orElse(""),
                ofNullable(strategy).orElse(UnmarshalledStepStrategyDefinition.NONE),
                ofNullable(inputs).orElse(emptyMap()),
                ofNullable(steps).orElse(emptyList()),
                ofNullable(outputs).orElse(emptyMap())
            );
        }

        public UnmarshalledStepDefinitionBuilder withGwtType(GwtType gwtType) {
            this.gwtType = gwtType;
            return this;
        }

        public UnmarshalledStepDefinitionBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public UnmarshalledStepDefinitionBuilder withTarget(String target) {
            this.target = target;
            return this;
        }

        public UnmarshalledStepDefinitionBuilder withType(String type) {
            this.type = type;
            return this;
        }

        public UnmarshalledStepDefinitionBuilder withStrategy(UnmarshalledStepStrategyDefinition strategy) {
            this.strategy = strategy;
            return this;
        }

        public UnmarshalledStepDefinitionBuilder withInputs(Map<String, Object> inputs) {
            this.inputs = inputs;
            return this;
        }

        public UnmarshalledStepDefinitionBuilder withSteps(List<UnmarshalledStepDefinition> steps) {
            this.steps = steps;
            return this;
        }

        public UnmarshalledStepDefinitionBuilder withOutputs(Map<String, Object> outputs) {
            this.outputs = outputs;
            return this;
        }
    }
}
