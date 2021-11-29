package com.chutneytesting.task.context;

import static java.util.Collections.emptyMap;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.builder.ToStringStyle.NO_CLASS_NAME_STYLE;

import com.chutneytesting.task.spi.Task;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Input;
import com.chutneytesting.task.spi.injectable.Logger;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class ContextPutTask implements Task {

    private final Logger logger;
    private final Map<String, Object> entries;

    public ContextPutTask(Logger logger, @Input("entries") Map<String, Object> entries) {
        this.logger = logger;
        this.entries = ofNullable(entries).orElse(emptyMap());
    }

    @Override
    public TaskExecutionResult execute() {
        entries.forEach((key, value) -> logger.info("Adding to context " + key + " = " + prettyLog(value) + " " + logClassType(value)));
        return TaskExecutionResult.ok(entries);
    }

    private String prettyLog(Object value) {
        if (value == null) {
            return "null";
        } else if (value instanceof String) {
            return value.toString();
        } else if (value instanceof Object[]) {
            return Arrays.toString((Object[]) value);
        } else if (value instanceof List) {
            return Arrays.toString(((List) value).toArray());
        } else if (value instanceof Map) {
            return Arrays.toString(((Map) value).entrySet().toArray());
        } else {
            return ToStringBuilder.reflectionToString(value, NO_CLASS_NAME_STYLE);
        }
    }

    private String logClassType(Object value) {
        return value != null ? "(" + value.getClass().getSimpleName() + ")" : "";
    }
}
