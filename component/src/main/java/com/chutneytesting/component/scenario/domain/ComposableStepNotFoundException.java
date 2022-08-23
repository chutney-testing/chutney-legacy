package com.chutneytesting.component.scenario.domain;

public class ComposableStepNotFoundException extends RuntimeException {
    public ComposableStepNotFoundException(String id) {
        super("Composable step id [" + id + "] could not be found");
    }
}
