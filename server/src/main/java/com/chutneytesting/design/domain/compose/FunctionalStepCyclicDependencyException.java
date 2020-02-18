package com.chutneytesting.design.domain.compose;

public class FunctionalStepCyclicDependencyException extends RuntimeException {
    public FunctionalStepCyclicDependencyException(String message) {
        super(message);
    }

    public FunctionalStepCyclicDependencyException(String message, Throwable cause) {
        super(message, cause);
    }
}
