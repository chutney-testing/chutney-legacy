package com.chutneytesting.execution.domain.scenario.composed;

import static java.util.Collections.emptyMap;
import static java.util.Optional.ofNullable;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

public class StepImplementation {

    public final String type;
    public final String target;
    public final Map<String, Object> inputs;
    public final Map<String, Object> outputs;

    public StepImplementation() {
        this(null, null, null, null);
    }

    public StepImplementation(String type, String target, Map<String, Object> inputs, Map<String, Object> outputs) {
        this.type = ofNullable(type).orElse("");
        this.target = ofNullable(target).orElse("");
        this.inputs = ofNullable(inputs).map(Collections::unmodifiableMap).orElse(emptyMap());
        this.outputs = ofNullable(outputs).map(Collections::unmodifiableMap).orElse(emptyMap());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StepImplementation that = (StepImplementation) o;
        return type.equals(that.type) &&
            target.equals(that.target) &&
            inputs.equals(that.inputs) &&
            outputs.equals(that.outputs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, target, inputs, outputs);
    }

    @Override
    public String toString() {
        return "StepImplementation{" +
            "type='" + type + '\'' +
            ", target='" + target + '\'' +
            ", inputs=" + inputs +
            ", outputs=" + outputs +
            '}';
    }
}
