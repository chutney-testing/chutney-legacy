package com.chutneytesting.task.assertion.compareTask;

import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Logger;

public class CompareContainsTask implements CompareExecutor {

    @Override
    public TaskExecutionResult compare(Logger logger, String actual, String expected) {
        if (actual.contains(expected)) {
            logger.info("[" + actual + "] CONTAINS [" + expected + "]");
            return TaskExecutionResult.ok();
        } else {
            logger.error("[" + actual + "] NOT CONTAINS [" + expected + "]");
            return TaskExecutionResult.ko();
        }
    }
}
