package com.chutneytesting.scenario;

@SuppressWarnings("serial")
public class AlreadyExistingScenarioException extends RuntimeException {

    public AlreadyExistingScenarioException(String message) {
        super(message);
    }

}
