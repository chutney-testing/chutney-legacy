package com.chutneytesting.design.domain.scenario.compose;

public class ComposableStepCyclicDependencyException extends RuntimeException {
    public ComposableStepCyclicDependencyException(String message) {
        super(message);
    }

    public ComposableStepCyclicDependencyException(String message, Throwable cause) {
        super(message, cause);
    }
}
