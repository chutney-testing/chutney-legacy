package com.chutneytesting.task.spi;

import static java.util.Optional.ofNullable;

import com.chutneytesting.task.spi.injectable.Target;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class FinallyAction {
    private final String originalTask;
    private final String actionIdentifier;
    private final Target target;
    private final Map<String, Object> inputs;
    private final String strategyType;
    private final Map<String, Object> strategyProperties;

    @Deprecated
    private FinallyAction(String actionIdentifier, Optional<Target> target, Map<String, Object> inputs) {
        this("", actionIdentifier, target, inputs);
    }

    @Deprecated
    private FinallyAction(String originalTask, String actionIdentifier, Optional<Target> target, Map<String, Object> inputs) {
        this(originalTask, actionIdentifier, target.orElse(null), inputs, null, null);
    }

    private FinallyAction(String originalTask, String actionIdentifier, Target target, Map<String, Object> inputs) {
        this(originalTask, actionIdentifier, target, inputs, null, null);
    }

    private FinallyAction(String originalTask, String actionIdentifier, Target target, Map<String, Object> inputs, String strategyType, Map<String, Object> strategyProperties) {
        this.originalTask = originalTask;
        this.actionIdentifier = actionIdentifier;
        this.target = target;
        this.inputs = inputs;
        this.strategyType = strategyType;
        this.strategyProperties = strategyProperties;
    }

    public static class Builder {
        private final String originalTask;
        private final String identifier;
        private Target target;
        private final Map<String, Object> inputs = new HashMap<>();
        private String strategyType;
        private Map<String, Object> strategyProperties;

        @Deprecated
        private Builder(String identifier) {
            this.identifier = identifier;
            this.originalTask = "";
        }

        private Builder(String identifier, String originalTask) {
            this.identifier = identifier;
            this.originalTask = originalTask;
        }

        public static Builder forAction(String identifier, String name) {
            return new Builder(identifier, name);
        }

        public Builder withTarget(Target target) {
            this.target = target;
            return this;
        }

        public Builder withInput(String key, Object value) {
            inputs.put(key, value);
            return this;
        }

        public Builder withStrategyType(String strategyType) {
            this.strategyType = strategyType;
            return this;
        }

        public Builder withStrategyProperties(Map<String, Object> strategyProperties) {
            this.strategyProperties = strategyProperties;
            return this;
        }

        public FinallyAction build() {
            return new FinallyAction(originalTask, identifier, target, Collections.unmodifiableMap(inputs), strategyType, strategyProperties);
        }
    }

    public String originalTask() {
        return originalTask;
    }

    public String actionIdentifier() {
        return actionIdentifier;
    }

    public Optional<Target> target() {
        return ofNullable(target);
    }

    public Map<String, Object> inputs() {
        return inputs;
    }

    public Optional<String> strategyType() {
        return ofNullable(strategyType);
    }

    public Optional<Map<String, Object>> strategyProperties() {
        return ofNullable(strategyProperties);
    }

    @Override
    public String toString() {
        return "FinallyAction{" +
            "actionIdentifier='" + actionIdentifier + '\'' +
            ", targetName=" + target +
            ", inputs=" + inputs +
            ", strategyType=" + strategyType +
            ", strategyProperties=" + strategyProperties +
            '}';
    }
}
