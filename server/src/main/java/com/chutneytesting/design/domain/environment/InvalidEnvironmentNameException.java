package com.chutneytesting.design.domain.environment;

@SuppressWarnings("serial")
public class InvalidEnvironmentNameException extends RuntimeException {
    public InvalidEnvironmentNameException(String message) {
        super(message + ". NOTE: Environment are stored in files, names must be of the form [A-Z0-9_\\-]{3,20}");
    }
}
