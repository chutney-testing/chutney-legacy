package com.chutneytesting.task.spi;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Result of a {@link Task} execution.<br>
 * Can be either:
 * <ul>
 * <li>{@link Status#Success} with outputs (key / value)</li>
 * <li>{@link Status#Failure}</li>
 * </ul>
 */
public class TaskExecutionResult {
    public final Status status;
    public final Map<String, Object> outputs;

    private TaskExecutionResult(Status status, Map<String, ?> outputs) {
        this.status = status;
        this.outputs = Collections.unmodifiableMap(outputs);
    }

    private TaskExecutionResult(Status status) {
        this(status, Collections.emptyMap());
    }

    public static TaskExecutionResult ok() {
        return new TaskExecutionResult(Status.Success);
    }

    public static TaskExecutionResult ok(Map<String, ?> outputs) {
        return new TaskExecutionResult(Status.Success, outputs);
    }

    public static TaskExecutionResult ok(String key, Object value) {
        Map<String, Object> outputs = new HashMap<>();
        outputs.put(key, value);
        return new TaskExecutionResult(Status.Success, outputs);
    }

    public static TaskExecutionResult ko() {
        return new TaskExecutionResult(Status.Failure);
    }

    public static TaskExecutionResult ko(Map<String, ?> outputs) {
        return new TaskExecutionResult(Status.Failure, outputs);
    }

    public enum Status {
        Success, Failure
    }
}
