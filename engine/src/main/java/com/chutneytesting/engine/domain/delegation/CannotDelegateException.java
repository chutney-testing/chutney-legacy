package com.chutneytesting.engine.domain.delegation;

@SuppressWarnings("serial")
public class CannotDelegateException extends RuntimeException {

    public CannotDelegateException(String message) {
        super(message);
    }

}
