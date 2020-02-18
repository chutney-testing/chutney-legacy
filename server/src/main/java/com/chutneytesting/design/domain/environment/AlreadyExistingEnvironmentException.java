package com.chutneytesting.design.domain.environment;

@SuppressWarnings("serial")
public class AlreadyExistingEnvironmentException extends RuntimeException {
    public AlreadyExistingEnvironmentException(String message) {
        super(message);
    }
}
