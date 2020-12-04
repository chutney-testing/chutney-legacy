package com.chutneytesting.environment.domain;

@SuppressWarnings("serial")
public class EnvironmentNotFoundException extends RuntimeException {
    public EnvironmentNotFoundException(String message) {
        super(message);
    }
}
