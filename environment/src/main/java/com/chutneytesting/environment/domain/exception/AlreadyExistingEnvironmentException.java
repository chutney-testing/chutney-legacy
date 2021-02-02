package com.chutneytesting.environment.domain.exception;

@SuppressWarnings("serial")
public class AlreadyExistingEnvironmentException extends RuntimeException {
    public AlreadyExistingEnvironmentException(String message) {
        super(message);
    }
}
