package com.chutneytesting.design.domain.environment;

@SuppressWarnings("serial")
public class EnvironmentNotFoundException extends RuntimeException {
    public EnvironmentNotFoundException(String message) {
        super(message);
    }
}
