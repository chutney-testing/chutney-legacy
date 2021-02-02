package com.chutneytesting.environment.domain;

@SuppressWarnings("serial")
public class TargetNotFoundException extends RuntimeException {
    public TargetNotFoundException(String message) {
        super(message);
    }
}
