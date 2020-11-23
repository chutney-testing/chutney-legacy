package com.chutneytesting.design.domain.scenario.compose;

public class ComposableStepNotFoundException extends RuntimeException {
    public ComposableStepNotFoundException() {
        super("The functional step id could not be found");
    }
}
