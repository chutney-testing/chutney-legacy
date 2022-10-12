package com.chutneytesting.action.assertion.compareTask;

import com.chutneytesting.action.spi.ActionExecutionResult;
import com.chutneytesting.action.spi.injectable.Logger;
import java.util.Objects;

public class CompareNotEqualsAction implements CompareExecutor {

    @Override
    public ActionExecutionResult compare(Logger logger, String actual, String expected) {
        if (Objects.equals(actual, expected)) {
            logger.error("[" + expected + "]" + " EQUALS " + "[" + actual+"]");
            return ActionExecutionResult.ko();
        } else {
            logger.info("[" + expected + "]" + " NOT EQUALS " + "[" + actual+"]");
            return ActionExecutionResult.ok();
        }
    }
}
