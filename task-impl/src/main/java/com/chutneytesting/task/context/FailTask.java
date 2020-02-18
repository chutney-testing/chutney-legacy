package com.chutneytesting.task.context;

import com.chutneytesting.task.spi.Task;
import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Logger;
import java.time.Instant;

public class FailTask implements Task {

    private final Logger logger;

    public FailTask(Logger logger) {
        this.logger = logger;
    }

    @Override
    public TaskExecutionResult execute() {
        logger.error("Failed at "+ Instant.now());
        return TaskExecutionResult.ko();
    }

}
