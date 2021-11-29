package com.chutneytesting.task.spi;

import static java.util.Collections.unmodifiableMap;
import static java.util.Optional.ofNullable;

import com.chutneytesting.task.spi.injectable.Target;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class FinallyAction {

    private final String name;
    private final String type;
    private final Target target;
    private final Map<String, Object> inputs;
    private final Map<String, Object> validations;
    private final String strategyType;
    private final Map<String, Object> strategyProperties;

    @Deprecated
    private FinallyAction(String type, Optional<Target> target, Map<String, Object> inputs) {
        this("", type, target, inputs);
    }

    @Deprecated
    private FinallyAction(String name, String type, Optional<Target> target, Map<String, Object> inputs) {
        this(name, type, target.orElse(null), inputs, null, null, null);
    }

    private FinallyAction(String name, String type, Target target, Map<String, Object> inputs, Map<String, Object> validations) {
        this(name, type, target, inputs, null, null, null);
    }

    private FinallyAction(String name, String type, Target target, Map<String, Object> inputs, Map<String, Object> validations, String strategyType, Map<String, Object> strategyProperties) {
        this.name = name;
        this.type = type;
        this.target = target;
        this.inputs = inputs;
        this.validations = validations;
        this.strategyType = strategyType;
        this.strategyProperties = strategyProperties;
    }

    public static class Builder {
        private final String name;
        private final String type;
        private Target target;
        private final Map<String, Object> inputs = new HashMap<>();
        private final Map<String, Object> validations = new HashMap<>();
        private String strategyType;
        private Map<String, Object> strategyProperties;

        @Deprecated
        private Builder(String type) {
            this.type = type;
            this.name = "";
        }

        private Builder(String type, String name) {
            this.type = type;
            this.name = ofNullable(name).orElse("");
        }

        public static Builder forAction(String type, String name) {
            return new Builder(type, name);
        }

        public static Builder forAction(String type, Class<?> originalTask) {
            return new Builder(type, "Finally action generated for " + originalTask.getSimpleName());
        }

        public Builder withTarget(Target target) {
            this.target = target;
            return this;
        }

        public Builder withInput(String key, Object value) {
            inputs.put(key, value);
            return this;
        }

        public Builder withValidation(String key, Object value) {
            validations.put(key, value);
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
            return new FinallyAction(name, type, target, unmodifiableMap(inputs), unmodifiableMap(validations), strategyType, strategyProperties);
        }
    }

    @Deprecated
    public String originalTask() {
        return name();
    }

    public String name() {
        return name;
    }

    @Deprecated
    public String actionIdentifier() {
        return type();
    }

    public String type() {
        return type;
    }

    public Optional<Target> target() {
        return ofNullable(target);
    }

    public Map<String, Object> inputs() {
        return inputs;
    }

    public Map<String, Object> validations() {
        return validations;
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
            "name='" + name + '\'' +
            ", type=" + type +
            ", targetName=" + target +
            ", inputs=" + inputs +
            ", strategyType=" + strategyType +
            ", strategyProperties=" + strategyProperties +
            '}';
    }
}
