package com.chutneytesting.cli.infrastruture;

import com.chutneytesting.task.spi.injectable.Target;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Immutable tree-like structure composing a Scenario.
 */
public class StepDefinitionCore {

    public final GwtType gwtType;

    public final String name;

    /**
     * Target on which to execute the current step.
     */
    public final Optional<Target> target;

    /**
     * Environment on which to execute the current step.
     */
    public final String environment;

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
    public final List<StepDefinitionCore> steps;

    public final Map<String, Object> outputs;

    public final StepStrategyDefinitionCore strategy;

    public StepDefinitionCore(GwtType gwtType,
                              String name,
                              Optional<Target> target,
                              String type,
                              StepStrategyDefinitionCore strategy,
                              Map<String, Object> inputs,
                              List<StepDefinitionCore> steps,
                              Map<String, Object> outputs,
                              String environment) {
        this.gwtType = gwtType;
        this.name = name;
        this.target = target;
        this.type = type;
        this.strategy = strategy;
        this.inputs = inputs != null ? Collections.unmodifiableMap(inputs) : Collections.emptyMap();
        this.steps = steps != null ? Collections.unmodifiableList(steps) : Collections.emptyList();
        this.outputs = outputs != null ? Collections.unmodifiableMap(outputs) : Collections.emptyMap();
        this.environment = environment;
    }

    @Override
    public String toString() {
        return "StepDefinition{" +
            "name='" + name + '\'' +
            ", type='" + type + '\'' +
            '}';
    }

    public static class StepStrategyDefinitionCore {
        public final String type;
        public final StrategyPropertiesCore strategyProperties;

        public StepStrategyDefinitionCore(String type, StrategyPropertiesCore strategyProperties) {
            this.type = type;
            this.strategyProperties = strategyProperties;
        }

    }

    /**
     * Strategy properties.
     */
    @SuppressWarnings("serial")
    public static class StrategyPropertiesCore extends HashMap<String, Object> {

        public StrategyPropertiesCore() {
            super();
        }

        public StrategyPropertiesCore(Map<String, Object> data) {
            super(data);
        }

        public <T> T getProperty(String key, Class<T> type) {
            return type.cast(get(key));
        }

        public StrategyPropertiesCore setProperty(String key, Object value) {
            put(key, value);
            return this;
        }
    }
}
