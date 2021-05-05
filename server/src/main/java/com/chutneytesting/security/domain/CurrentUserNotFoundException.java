package com.chutneytesting.security.domain;

public class CurrentUserNotFoundException extends RuntimeException {
    public CurrentUserNotFoundException() {
        super("Current user could not be found");
    }
}
