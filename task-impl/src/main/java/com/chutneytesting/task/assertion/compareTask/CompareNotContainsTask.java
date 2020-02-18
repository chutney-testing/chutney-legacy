package com.chutneytesting.task.assertion.compareTask;

import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Logger;

public class CompareNotContainsTask implements CompareExecutor {

    @Override
    public TaskExecutionResult compare(Logger logger, String actual, String expected) {
        if (actual.contains(expected)) {
            logger.error("[" + actual + "] CONTAINS [" + expected + "]");
            return TaskExecutionResult.ko();
        } else {
            logger.info("[" + actual + "] NOT CONTAINS [" + expected + "]");
            return TaskExecutionResult.ok();
        }
    }
}
