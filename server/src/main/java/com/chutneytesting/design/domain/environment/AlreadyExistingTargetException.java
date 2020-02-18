package com.chutneytesting.design.domain.environment;

@SuppressWarnings("serial")
public class AlreadyExistingTargetException extends RuntimeException {

    public AlreadyExistingTargetException(String message) {
        super(message);
    }

}
