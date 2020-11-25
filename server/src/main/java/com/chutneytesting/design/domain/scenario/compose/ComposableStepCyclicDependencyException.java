package com.chutneytesting.design.domain.scenario.compose;

public class ComposableStepCyclicDependencyException extends RuntimeException {

    public ComposableStepCyclicDependencyException(String composableStepName) {
        super("Cyclic dependency found on functional step [" + composableStepName + "]");
    }
}
