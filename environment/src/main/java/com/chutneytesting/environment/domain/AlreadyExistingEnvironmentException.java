package com.chutneytesting.environment.domain;

@SuppressWarnings("serial")
public class AlreadyExistingEnvironmentException extends RuntimeException {
    public AlreadyExistingEnvironmentException(String message) {
        super(message);
    }
}
