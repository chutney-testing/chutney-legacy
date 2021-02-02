package com.chutneytesting.design.domain.scenario.compose;

public class ComposableStepNotFoundException extends RuntimeException {
    public ComposableStepNotFoundException(String composableStepId) {
        super("The composable step id [" + composableStepId + "] could not be found");
    }
}
