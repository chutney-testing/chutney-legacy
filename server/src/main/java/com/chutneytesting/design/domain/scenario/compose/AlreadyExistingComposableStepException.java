package com.chutneytesting.design.domain.scenario.compose;

@SuppressWarnings("serial")
public class AlreadyExistingComposableStepException extends RuntimeException {

    public AlreadyExistingComposableStepException(String message) {
        super(message);
    }

}
