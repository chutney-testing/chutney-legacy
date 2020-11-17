package com.chutneytesting.execution.domain.scenario.composed;

import static java.util.Collections.emptyMap;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public class StepImplementation {

    public static final Optional<StepImplementation> NONE = empty();

    public final String type;
    public final String target;
    public final Map<String, Object> inputs;
    public final Map<String, Object> outputs;

    public StepImplementation(String type, String target, Map<String, Object> inputs, Map<String, Object> outputs, String xRef) {
        this.type = ofNullable(type).orElse("");
        this.target = ofNullable(target).orElse("");
        this.inputs = ofNullable(inputs).map(Collections::unmodifiableMap).orElse(emptyMap());
        this.outputs = ofNullable(outputs).map(Collections::unmodifiableMap).orElse(emptyMap());
    }

}
