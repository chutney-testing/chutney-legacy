package com.chutneytesting.action.assertion.compareTask;

import com.chutneytesting.action.spi.ActionExecutionResult;
import com.chutneytesting.action.spi.injectable.Logger;

public interface CompareExecutor {

    ActionExecutionResult compare(Logger logger, String actual, String expected);
}
