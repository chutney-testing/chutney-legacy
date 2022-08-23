package com.chutneytesting.server.core.domain.scenario;

@SuppressWarnings("serial")
public class AlreadyExistingScenarioException extends RuntimeException {

    public AlreadyExistingScenarioException(String message) {
        super(message);
    }

}
