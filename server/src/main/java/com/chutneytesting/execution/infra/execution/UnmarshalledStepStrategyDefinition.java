package com.chutneytesting.execution.infra.execution;

import static java.util.Collections.emptyMap;
import static java.util.Optional.ofNullable;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import java.util.Map;

@JsonDeserialize(builder = UnmarshalledStepStrategyDefinition.UnmarshalledStepStrategyDefinitionBuilder.class)
class UnmarshalledStepStrategyDefinition {

    // TODO - because null is mapped to default strategy by the engine, but it shouldn't be
    public static final UnmarshalledStepStrategyDefinition NONE = null; // builder().build(); would be better

    public final String type;
    public final Map<String, Object> parameters;

    private UnmarshalledStepStrategyDefinition(String type, Map<String, Object> parameters) {
        this.type = type;
        this.parameters = parameters;
    }

    public static UnmarshalledStepStrategyDefinitionBuilder builder() {
        return new UnmarshalledStepStrategyDefinitionBuilder();
    }

    @JsonPOJOBuilder
    public static class UnmarshalledStepStrategyDefinitionBuilder {
        private String type;
        private Map<String, Object> parameters;

        private UnmarshalledStepStrategyDefinitionBuilder() {}

        public UnmarshalledStepStrategyDefinition build() {
            return new UnmarshalledStepStrategyDefinition(
              ofNullable(type).orElse("default"),
              ofNullable(parameters).orElse(emptyMap())
            );
        }

        public UnmarshalledStepStrategyDefinitionBuilder withType(String type) {
            this.type = type;
            return this;
        }

        public UnmarshalledStepStrategyDefinitionBuilder withParameters(Map<String, Object> parameters) {
            this.parameters = parameters;
            return this;
        }
    }
}
