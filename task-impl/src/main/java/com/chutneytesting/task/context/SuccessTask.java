package com.chutneytesting.task.context;

import com.chutneytesting.task.spi.Task;
import com.chutneytesting.task.spi.TaskExecutionResult;

public class SuccessTask implements Task {

    public SuccessTask() {
    }

    @Override
    public TaskExecutionResult execute() {
        return TaskExecutionResult.ok();
    }
}
