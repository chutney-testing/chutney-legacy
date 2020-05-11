package com.chutneytesting.engine.domain.execution;

import static java.util.Objects.requireNonNull;

import com.chutneytesting.engine.domain.execution.strategies.StepStrategyDefinition;
import com.chutneytesting.task.spi.injectable.Target;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Immutable tree-like structure composing a Scenario.
 */
public class StepDefinition {

    public final String name;

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
    public final List<StepDefinition> steps;

    public final Map<String, Object> outputs;

    public final String environment;

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
                          String environment) {
        this.name = requireNonNull(name, "The argument <name> must not be null");
        this.type = requireNonNull(type, "The argument <type> must not be null");

        this.strategy = strategy;
        this.target = target;

        this.inputs = inputs != null ? Collections.unmodifiableMap(inputs) : Collections.emptyMap();
        this.steps = steps != null ? Collections.unmodifiableList(steps) : Collections.emptyList();
        this.outputs = outputs != null ? Collections.unmodifiableMap(outputs) : Collections.emptyMap();
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
            ", target=" + target +
            ", strategy=" + strategy +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StepDefinition that = (StepDefinition) o;
        return name.equals(that.name) &&
            type.equals(that.type) &&
            Objects.equals(inputs, that.inputs) &&
            Objects.equals(steps, that.steps) &&
            Objects.equals(outputs, that.outputs) &&
            Objects.equals(target, that.target) &&
            Objects.equals(strategy, that.strategy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type, inputs, steps, outputs, target, strategy);
    }
}
