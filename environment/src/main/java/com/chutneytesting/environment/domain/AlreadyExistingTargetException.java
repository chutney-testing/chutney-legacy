package com.chutneytesting.environment.domain;

@SuppressWarnings("serial")
public class AlreadyExistingTargetException extends RuntimeException {

    public AlreadyExistingTargetException(String message) {
        super(message);
    }

}
