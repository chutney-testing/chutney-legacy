package com.chutneytesting.action.spi;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Result of a {@link Action} execution.<br>
 * Can be either:
 * <ul>
 * <li>{@link Status#Success} with outputs (key / value)</li>
 * <li>{@link Status#Failure}</li>
 * </ul>
 */
public class ActionExecutionResult {
    public final Status status;
    public final Map<String, Object> outputs;

    private ActionExecutionResult(Status status, Map<String, ?> outputs) {
        this.status = status;
        this.outputs = Collections.unmodifiableMap(outputs);
    }

    private ActionExecutionResult(Status status) {
        this(status, Collections.emptyMap());
    }

    public static ActionExecutionResult ok() {
        return new ActionExecutionResult(Status.Success);
    }

    public static ActionExecutionResult ok(Map<String, ?> outputs) {
        return new ActionExecutionResult(Status.Success, outputs);
    }

    public static ActionExecutionResult ok(String key, Object value) {
        Map<String, Object> outputs = new HashMap<>();
        outputs.put(key, value);
        return new ActionExecutionResult(Status.Success, outputs);
    }

    public static ActionExecutionResult ko() {
        return new ActionExecutionResult(Status.Failure);
    }

    public static ActionExecutionResult ko(Map<String, ?> outputs) {
        return new ActionExecutionResult(Status.Failure, outputs);
    }

    public enum Status {
        Success, Failure
    }
}
