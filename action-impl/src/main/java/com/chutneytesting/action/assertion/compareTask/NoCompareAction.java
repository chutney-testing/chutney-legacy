package com.chutneytesting.action.assertion.compareTask;

import com.chutneytesting.action.spi.ActionExecutionResult;
import com.chutneytesting.action.spi.injectable.Logger;

class NoCompareAction implements CompareExecutor {

    @Override
    public ActionExecutionResult compare(Logger logger, String actual, String expected) {
        logger.error(
            "Sorry, this mode is not existed in our mode list, please refer to documentation to check it."
        );
        return ActionExecutionResult.ko();
    }
}
