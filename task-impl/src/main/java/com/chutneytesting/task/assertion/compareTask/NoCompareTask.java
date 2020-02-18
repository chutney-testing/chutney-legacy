package com.chutneytesting.task.assertion.compareTask;

import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Logger;

class NoCompareTask implements CompareExecutor {

    @Override
    public TaskExecutionResult compare(Logger logger, String actual, String expected) {
        logger.error(
            "Sorry, this mode is not existed in our mode list, please refer to documentation to check it."
        );
        return TaskExecutionResult.ko();
    }
}
