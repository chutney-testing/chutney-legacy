package com.chutneytesting.design.domain.scenario.compose;

public class FunctionalStepCyclicDependencyException extends RuntimeException {
    public FunctionalStepCyclicDependencyException(String message) {
        super(message);
    }

    public FunctionalStepCyclicDependencyException(String message, Throwable cause) {
        super(message, cause);
    }
}
