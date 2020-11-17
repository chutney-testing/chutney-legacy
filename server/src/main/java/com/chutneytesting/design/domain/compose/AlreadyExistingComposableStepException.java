package com.chutneytesting.design.domain.compose;

@SuppressWarnings("serial")
public class AlreadyExistingComposableStepException extends RuntimeException {

    public AlreadyExistingComposableStepException(String message) {
        super(message);
    }

}
