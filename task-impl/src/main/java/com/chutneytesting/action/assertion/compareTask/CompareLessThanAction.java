package com.chutneytesting.action.assertion.compareTask;

import java.util.function.BiFunction;

public class CompareLessThanAction extends AbstractCompareNumberAction {

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
