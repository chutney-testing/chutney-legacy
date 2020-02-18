package com.chutneytesting.task.assertion.compareTask;

import com.chutneytesting.task.spi.TaskExecutionResult;
import com.chutneytesting.task.spi.injectable.Logger;
import java.util.function.BiFunction;

public abstract class AbstractCompareNumberTask implements CompareExecutor {

    protected abstract BiFunction<Double, Double, Boolean> compareFunction();

    protected abstract String getFunctionName();

    protected abstract String getOppositeFunctionName();

    @Override
    public TaskExecutionResult compare(Logger logger, String actual, String expected) {

        Double actualD = parse(logger, actual);
        Double expectedD = parse(logger, expected);

        Boolean apply = compareFunction().apply(actualD, expectedD);
        if (apply) {
            logger.info("[" + actual + "] " + getFunctionName() + " [" + expected + "]");
            return TaskExecutionResult.ok();
        } else {
            logger.error("[" + actual + "] " + getOppositeFunctionName() + " [" + expected + "]");
            return TaskExecutionResult.ko();
        }
    }

    private Double parse(Logger logger, String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            logger.error("[" + value + "] is Not Numeric");
        }
        return Double.NaN;
    }
}
