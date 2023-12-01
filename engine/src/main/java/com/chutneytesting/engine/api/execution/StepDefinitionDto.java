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

import static java.util.Optional.ofNullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Immutable tree-like structure composing a Scenario.
 */
public class StepDefinitionDto {

    public final String name;

    /**
     * Target on which to execute the current step.
     */
    private final TargetExecutionDto target;

    /**
     * Type of the step, should match an extension.
     */
    public final String type;

    /**
     * Data used by a matched extension, may be empty.
     */
    public final Map<String, Object> inputs;

    /**
     * Sub steps, may be empty.
     */
    public final List<StepDefinitionDto> steps;

    public final Map<String, Object> outputs;

    public final Map<String, Object> validations;

    public final StepStrategyDefinitionDto strategy;

    public StepDefinitionDto(String name,
                             TargetExecutionDto target,
                             String type,
                             StepStrategyDefinitionDto strategy,
                             Map<String, Object> inputs,
                             List<StepDefinitionDto> steps,
                             Map<String, Object> outputs,
                             Map<String, Object> validations) {
        this.name = name;
        this.target = target;
        this.type = type;
        this.strategy = strategy;
        this.inputs = inputs != null ? Collections.unmodifiableMap(inputs) : Collections.emptyMap();
        this.steps = steps != null ? Collections.unmodifiableList(steps) : Collections.emptyList();
        this.outputs = outputs != null ? Collections.unmodifiableMap(outputs) : Collections.emptyMap();
        this.validations = validations != null ? Collections.unmodifiableMap(validations) : Collections.emptyMap();
    }

    public Optional<TargetExecutionDto> getTarget() {
        return ofNullable(target);
    }

    @Override
    public String toString() {
        return "StepDefinition{" +
            "name='" + name + '\'' +
            ", type='" + type + '\'' +
            '}';
    }

    public static class StepStrategyDefinitionDto {
        public final String type;
        public final StrategyPropertiesDto strategyProperties;

        public StepStrategyDefinitionDto(String type, StrategyPropertiesDto strategyProperties) {
            this.type = type;
            this.strategyProperties = strategyProperties;
        }

    }

    public static class StrategyPropertiesDto extends HashMap<String, Object> {

        public StrategyPropertiesDto() {
            super();
        }

        public StrategyPropertiesDto(Map<String, Object> data) {
            super(data);
        }

        public <T> T getProperty(String key, Class<T> type) {
            return type.cast(get(key));
        }

        public StrategyPropertiesDto setProperty(String key, Object value) {
            put(key, value);
            return this;
        }
    }
}
