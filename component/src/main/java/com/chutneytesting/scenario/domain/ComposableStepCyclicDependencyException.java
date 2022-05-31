package com.chutneytesting.scenario.domain;

public class ComposableStepCyclicDependencyException extends RuntimeException {

    public ComposableStepCyclicDependencyException(String id, String composableStepName) {
        super("Cyclic dependency found on composable step #"+id+"[" + composableStepName + "]");
    }
}
