package com.chutneytesting.task.context;

import com.chutneytesting.task.spi.Task;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Input;
import com.chutneytesting.task.spi.injectable.Logger;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class DebugTask implements Task {

    private final Logger logger;
    private Map<String, Object> inputs;
    private final Set<String> filter;

    public DebugTask(Logger logger, Map<String,Object> inputs, @Input("filters") Set<String> filter) {
        this.logger = logger;
        this.inputs = inputs;
        this.filter = Optional.ofNullable(filter).orElseGet(Collections::emptySet);
    }

    @Override
    public TaskExecutionResult execute() {
        inputs.entrySet().stream()
            .filter(entry -> filter.isEmpty() || filter.contains(entry.getKey()))
            .forEach(entry -> logger.info(entry.getKey() + " : [" + entry.getValue() + "]"));
        return TaskExecutionResult.ok();
    }
}
