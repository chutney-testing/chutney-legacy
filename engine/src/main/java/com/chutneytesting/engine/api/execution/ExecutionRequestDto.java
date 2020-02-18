package com.chutneytesting.engine.api.execution;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.chutneytesting.engine.api.execution.StepDefinitionDto.StepStrategyDefinitionDto;
import com.chutneytesting.engine.api.execution.StepDefinitionDto.StrategyPropertiesDto;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Anemic model standing for the request for a Scenario execution.
 */
public class ExecutionRequestDto {
    public final StepDefinitionRequestDto scenario;

    public ExecutionRequestDto(@JsonProperty("scenario") StepDefinitionRequestDto scenario) {
        this.scenario = scenario;
    }

    public static class StepStrategyDefinitionRequestDto {
        @JsonIgnore
        public final StepStrategyDefinitionDto definition;

        public StepStrategyDefinitionRequestDto(@JsonProperty("type") String type,
                                                @JsonProperty("parameters") Map<String, Object> parameters
        ) {
            this.definition = new StepStrategyDefinitionDto(type, new StrategyPropertiesDto(parameters));
        }
    }

    public static class StepDefinitionRequestDto {
        @JsonIgnore
        public final StepDefinitionDto definition;

        public final String name;
        public final TargetDto target;
        public final String type;
        public final Map<String, Object> inputs;
        public final List<StepDefinitionRequestDto> steps;
        public final Map<String, Object> outputs;

        public StepDefinitionRequestDto(
            @JsonProperty("name") String name,
            @JsonProperty("target") TargetDto target,
            @JsonProperty("strategy") StepStrategyDefinitionRequestDto strategy,
            @JsonProperty("type") String type,
            @JsonProperty("inputs") Map<String, Object> inputs,
            @JsonProperty("steps") List<StepDefinitionRequestDto> steps,
            @JsonProperty("outputs") Map<String, Object> outputs) {

            this.name = name;
            this.target = target;
            this.type = type;
            this.inputs = inputs;
            this.steps = steps;
            this.outputs = outputs;

            this.definition = new StepDefinitionDto(
                name,
                target,
                type,
                strategy != null ? strategy.definition : null,
                inputs, steps != null ? steps.stream().map(r -> r.definition).collect(Collectors.toList()) : Collections.emptyList(),
                outputs
            );
        }
    }
}
