package com.chutneytesting.task.spi;

import com.chutneytesting.task.spi.injectable.Target;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class FinallyAction {
    private final String actionIdentifier;
    private final Optional<Target> target;
    private final Map<String, Object> inputs;

    private FinallyAction(String actionIdentifier, Optional<Target> target, Map<String, Object> inputs) {
        this.actionIdentifier = actionIdentifier;
        this.target = target;
        this.inputs = inputs;
    }

    public static class Builder {
        private final String identifier;
        private Optional<Target> target = Optional.empty();
        private final Map<String, Object> inputs = new HashMap<>();

        private Builder(String identifier) {
            this.identifier = identifier;
        }

        public static Builder forAction(String identifier) {
            return new Builder(identifier);
        }

        public Builder withTarget(Target target) {
            this.target = Optional.of(target);
            return this;
        }

        public Builder withInput(String key, Object value) {
            inputs.put(key, value);
            return this;
        }

        public FinallyAction build() {
            return new FinallyAction(identifier, target, Collections.unmodifiableMap(inputs));
        }
    }

    public String actionIdentifier() {
        return actionIdentifier;
    }

    public Optional<Target> target() {
        return target;
    }

    public Map<String, Object> inputs() {
        return inputs;
    }

    @Override
    public String toString() {
        return "FinallyAction{" +
            "actionIdentifier='" + actionIdentifier + '\'' +
            ", targetName=" + target +
            ", inputs=" + inputs +
            '}';
    }
}
