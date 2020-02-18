package com.chutneytesting.task.assertion.compareTask;

import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Logger;
import java.util.Objects;

public class CompareNotEqualsTask implements CompareExecutor {

    @Override
    public TaskExecutionResult compare(Logger logger, String actual, String expected) {
        if (Objects.equals(actual, expected)) {
            logger.error("[" + expected + "]" + " EQUALS " + "[" + actual+"]");
            return TaskExecutionResult.ko();
        } else {
            logger.info("[" + expected + "]" + " NOT EQUALS " + "[" + actual+"]");
            return TaskExecutionResult.ok();
        }
    }
}
