package com.chutneytesting.task.spi;

import com.chutneytesting.task.spi.injectable.Target;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class FinallyAction {
    private final String originalTask;
    private final String actionIdentifier;
    private final Optional<Target> target;
    private final Map<String, Object> inputs;

    private FinallyAction(String originalTask, String actionIdentifier, Optional<Target> target, Map<String, Object> inputs) {
        this.originalTask = originalTask;
        this.actionIdentifier = actionIdentifier;
        this.target = target;
        this.inputs = inputs;
    }

    public static class Builder {
        private final String orginalTask;
        private final String identifier;
        private Optional<Target> target = Optional.empty();
        private final Map<String, Object> inputs = new HashMap<>();

        private Builder(String identifier, String orginalTask) {
            this.identifier = identifier;
            this.orginalTask = orginalTask;
        }

        public static Builder forAction(String identifier, String name) {
            return new Builder(identifier, name);
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
            return new FinallyAction(orginalTask, identifier, target, Collections.unmodifiableMap(inputs));
        }
    }

    public String getOriginalTask() {
        return originalTask;
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
