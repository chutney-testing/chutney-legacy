/*
 * Copyright 2017-2023 Enedis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.chutneytesting.engine.api.execution;

import com.chutneytesting.engine.api.execution.StepDefinitionDto.StepStrategyDefinitionDto;
import com.chutneytesting.engine.api.execution.StepDefinitionDto.StrategyPropertiesDto;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Anemic model standing for the request for a Scenario execution.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExecutionRequestDto {

    public final StepDefinitionRequestDto scenario;
    public final String environment;
    public final DatasetDto dataset;

    @JsonCreator
    public ExecutionRequestDto(StepDefinitionRequestDto scenario, String environment, DatasetDto dataset) {
        this.scenario = scenario;
        this.environment = environment;
        this.dataset = dataset;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StepStrategyDefinitionRequestDto {
        @JsonIgnore
        public final StepStrategyDefinitionDto definition;

        @JsonCreator
        public StepStrategyDefinitionRequestDto(@JsonProperty("type") String type,
                                                @JsonProperty("parameters") Map<String, Object> parameters
        ) {
            this.definition = new StepStrategyDefinitionDto(type, new StrategyPropertiesDto(parameters));
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StepDefinitionRequestDto {
        @JsonIgnore
        public final StepDefinitionDto definition;

        public final String name;
        public final TargetExecutionDto target;
        public final String type;
        public final Map<String, Object> inputs;
        public final List<StepDefinitionRequestDto> steps;
        public final Map<String, Object> outputs;
        public final Map<String, Object> validations;

        @JsonCreator
        public StepDefinitionRequestDto(
            String name,
            TargetExecutionDto target,
            @JsonProperty("strategy") StepStrategyDefinitionRequestDto strategy,
            String type,
            Map<String, Object> inputs,
            List<StepDefinitionRequestDto> steps,
            Map<String, Object> outputs,
            Map<String, Object> validations) {

            this.name = name;
            this.target = target;
            this.type = type;
            this.inputs = inputs;
            this.steps = steps;
            this.outputs = outputs;
            this.validations = validations;

            this.definition = new StepDefinitionDto(
                name,
                target,
                type,
                strategy != null ? strategy.definition : null,
                inputs,
                steps != null ? steps.stream().map(r -> r.definition).collect(Collectors.toList()) : Collections.emptyList(),
                outputs,
                validations
            );
        }
    }
}
