package com.chutneytesting.task.assertion.compareTask;

import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Logger;

public interface CompareExecutor {

    TaskExecutionResult compare(Logger logger, String actual, String expected);
}
