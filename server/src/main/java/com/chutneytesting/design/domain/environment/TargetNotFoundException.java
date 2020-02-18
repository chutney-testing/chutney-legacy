package com.chutneytesting.design.domain.environment;

@SuppressWarnings("serial")
public class TargetNotFoundException extends RuntimeException {
    public TargetNotFoundException(String message) {
        super(message);
    }
}
