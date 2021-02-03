package com.chutneytesting.environment.domain.exception;

@SuppressWarnings("serial")
public class TargetNotFoundException extends RuntimeException {
    public TargetNotFoundException(String message) {
        super(message);
    }
}
