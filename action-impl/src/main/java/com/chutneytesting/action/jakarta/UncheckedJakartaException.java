package com.chutneytesting.action.jakarta;

@SuppressWarnings("serial")
class UncheckedJakartaException extends RuntimeException {

    public UncheckedJakartaException(String message, Exception cause) {
        super(message, cause);
    }
}
