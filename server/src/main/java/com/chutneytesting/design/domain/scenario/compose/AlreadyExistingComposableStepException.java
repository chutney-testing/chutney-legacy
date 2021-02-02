package com.chutneytesting.design.domain.scenario.compose;

@SuppressWarnings("serial")
public class AlreadyExistingComposableStepException extends RuntimeException {

    public AlreadyExistingComposableStepException(ComposableStep composableStep) {
        super("The step ["+composableStep.name+"] already exists");
    }

}
