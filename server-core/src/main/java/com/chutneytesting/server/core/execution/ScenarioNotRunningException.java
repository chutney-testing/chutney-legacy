package com.chutneytesting.server.core.execution;

public class ScenarioNotRunningException extends RuntimeException {

    public ScenarioNotRunningException(String scenarioId) {
        super("Scenario [" + scenarioId + "] is not running");
    }
}
