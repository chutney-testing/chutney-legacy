package com.chutneytesting.scenario.domain;

@SuppressWarnings("serial")
public class AlreadyExistingScenarioException extends RuntimeException {

    public AlreadyExistingScenarioException(String message) {
        super(message);
    }

}
