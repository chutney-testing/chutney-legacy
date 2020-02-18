package com.chutneytesting.task.assertion.compareTask;

import java.util.function.BiFunction;

public class CompareLessThanTask extends AbstractCompareNumberTask {

    @Override
    protected BiFunction<Double, Double, Boolean> compareFunction() {
        return (d1, d2) -> d1 < d2;
    }

    @Override
    protected String getFunctionName() {
        return "IS LESS THAN";
    }

    @Override
    protected String getOppositeFunctionName() {
        return "IS GREATER THAN";
    }
}
