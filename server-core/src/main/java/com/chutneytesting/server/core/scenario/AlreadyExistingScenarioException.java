package com.chutneytesting.server.core.scenario;

@SuppressWarnings("serial")
public class AlreadyExistingScenarioException extends RuntimeException {

    public AlreadyExistingScenarioException(String message) {
        super(message);
    }

}
