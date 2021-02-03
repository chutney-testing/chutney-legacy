package com.chutneytesting.environment.domain.exception;

@SuppressWarnings("serial")
public class EnvironmentNotFoundException extends RuntimeException {
    public EnvironmentNotFoundException(String message) {
        super(message);
    }
}
