package com.chutneytesting.task.context;

import com.chutneytesting.task.spi.Task;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Logger;
import java.util.Map;

public class DebugTask implements Task {

    private final Logger logger;
    private Map<String, Object> inputs;

    public DebugTask(Logger logger, Map<String,Object> inputs) {
        this.logger = logger;
        this.inputs = inputs;
    }

    @Override
    public TaskExecutionResult execute() {
        inputs.entrySet().forEach(entry -> {
            logger.info(entry.getKey() + " : [" + entry.getValue() + "]");
        });
        return TaskExecutionResult.ok();
    }
}
