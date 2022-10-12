package com.chutneytesting.action.assertion.compare;

import java.util.function.BiFunction;

public class CompareGreaterThanAction extends AbstractCompareNumberAction {

    @Override
    protected BiFunction<Double, Double, Boolean> compareFunction() {
        return (d1, d2) -> d1 > d2;
    }

    @Override
    protected String getFunctionName() {
        return "IS GREATER THAN";
    }

    @Override
    protected String getOppositeFunctionName() {
        return "IS LESS THAN";
    }
}
