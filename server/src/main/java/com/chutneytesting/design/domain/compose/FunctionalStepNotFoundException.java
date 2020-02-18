package com.chutneytesting.design.domain.compose;

public class FunctionalStepNotFoundException extends RuntimeException {
    public FunctionalStepNotFoundException() {
        super("The functional step id could not be found");
    }
}
