package com.chutneytesting.action.assertion.compareTask;

import com.chutneytesting.action.spi.ActionExecutionResult;
import com.chutneytesting.action.spi.injectable.Logger;
import java.util.Objects;

public class CompareEqualsAction implements CompareExecutor {

    @Override
    public ActionExecutionResult compare(Logger logger, String actual, String expected) {
        if (Objects.equals(actual, expected)) {
            logger.info("[" + expected + "] EQUALS [" + actual + "]");
            return ActionExecutionResult.ok();
        } else {
            logger.error("[" + expected + "] NOT EQUALS [" + actual + "]");
            return ActionExecutionResult.ko();
        }
    }
}
