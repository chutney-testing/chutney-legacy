package com.chutneytesting.design.domain.compose;

@SuppressWarnings("serial")
public class AlreadyExistingFunctionalStepException extends RuntimeException {

    public AlreadyExistingFunctionalStepException(String message) {
        super(message);
    }

}
