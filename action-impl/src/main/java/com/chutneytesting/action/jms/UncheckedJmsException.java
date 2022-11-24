package com.chutneytesting.action.jms;

@SuppressWarnings("serial")
class UncheckedJmsException extends RuntimeException {

    public UncheckedJmsException(String message, Exception cause) {
        super(message, cause);
    }
}
