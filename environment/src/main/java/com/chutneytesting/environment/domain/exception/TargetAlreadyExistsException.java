package com.chutneytesting.environment.domain.exception;

public class TargetAlreadyExistsException extends RuntimeException {
    public TargetAlreadyExistsException(String message) {
        super(message);
    }
}
