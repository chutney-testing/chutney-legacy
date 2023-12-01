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

package com.chutneytesting.engine.domain.execution;

import static java.util.Objects.requireNonNull;

import com.chutneytesting.action.spi.injectable.StepDefinitionSpi;
import com.chutneytesting.engine.domain.execution.strategies.StepStrategyDefinition;
import com.chutneytesting.action.spi.injectable.Target;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Immutable tree-like structure composing a Scenario.
 */
public class StepDefinition implements StepDefinitionSpi {

    public final String name;

    /**
     * Type of the step, should match an extension.
     */
    public final String type;

    /**
     * Data used by a matched extension, may be empty.
     */
    private final Map<String, Object> inputs;

    /**
     * Sub steps, may be empty.
     */
    public final List<StepDefinition> steps;

    public final Map<String, Object> outputs;

    public final Map<String, Object> validations;

    public final String environment; // TODO - remove from here and pass as engine argument instead

    /**
     * Target on which to execute the current step.
     * Can be null, a step can have no target defined
     */
    private final Target target;

    /**
     * * Can be null, a step can have no strategy definition
     */
    private final StepStrategyDefinition strategy;

    public StepDefinition(String name,
                          Target target,
                          String type,
                          StepStrategyDefinition strategy,
                          Map<String, Object> inputs,
                          List<StepDefinition> steps,
                          Map<String, Object> outputs,
                          Map<String, Object> validations,
                          String environment) {
        this.name = requireNonNull(name, "The argument <name> must not be null");
        this.type = requireNonNull(type, "The argument <type> must not be null");

        this.strategy = strategy;
        this.target = target;

        this.inputs = inputs != null ? Collections.unmodifiableMap(inputs) : Collections.emptyMap();
        this.steps = steps != null ? Collections.unmodifiableList(steps) : Collections.emptyList();
        this.outputs = outputs != null ? Collections.unmodifiableMap(outputs) : Collections.emptyMap();
        this.validations = validations != null ? Collections.unmodifiableMap(validations) : Collections.emptyMap();
        this.environment = environment;
    }

    public Optional<Target> getTarget() {
        return Optional.ofNullable(target);
    }

    public Optional<StepStrategyDefinition> getStrategy() {
        return Optional.ofNullable(strategy);
    }

    @Override
    public String toString() {
        return "StepDefinition{" +
            "name='" + name + '\'' +
            ", type='" + type + '\'' +
            ", inputs=" + inputs +
            ", steps=" + steps +
            ", outputs=" + outputs +
            ", validations=" + validations +
            ", target=" + target +
            ", strategy=" + strategy +
            '}';
    }

    @Override
    public Map<String, Object> inputs() {
        return inputs;
    }
}
