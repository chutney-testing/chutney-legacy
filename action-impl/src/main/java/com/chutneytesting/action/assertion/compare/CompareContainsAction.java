package com.chutneytesting.action.assertion.compare;

import com.chutneytesting.action.spi.ActionExecutionResult;
import com.chutneytesting.action.spi.injectable.Logger;

public class CompareContainsAction implements CompareExecutor {

    @Override
    public ActionExecutionResult compare(Logger logger, String actual, String expected) {
        if (actual.contains(expected)) {
            logger.info("[" + actual + "] CONTAINS [" + expected + "]");
            return ActionExecutionResult.ok();
        } else {
            logger.error("[" + actual + "] NOT CONTAINS [" + expected + "]");
            return ActionExecutionResult.ko();
        }
    }
}
