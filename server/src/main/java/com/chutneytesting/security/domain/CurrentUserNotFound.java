package com.chutneytesting.security.domain;

public class CurrentUserNotFound extends RuntimeException {
    public CurrentUserNotFound() {
        super("Current user could not be found");
    }
}
