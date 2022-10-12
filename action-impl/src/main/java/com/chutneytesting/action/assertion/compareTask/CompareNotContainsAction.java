package com.chutneytesting.action.assertion.compareTask;

import com.chutneytesting.action.spi.ActionExecutionResult;
import com.chutneytesting.action.spi.injectable.Logger;

public class CompareNotContainsAction implements CompareExecutor {

    @Override
    public ActionExecutionResult compare(Logger logger, String actual, String expected) {
        if (actual.contains(expected)) {
            logger.error("[" + actual + "] CONTAINS [" + expected + "]");
            return ActionExecutionResult.ko();
        } else {
            logger.info("[" + actual + "] NOT CONTAINS [" + expected + "]");
            return ActionExecutionResult.ok();
        }
    }
}
