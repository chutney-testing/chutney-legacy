package com.chutneytesting.scenario.domain;

@SuppressWarnings("serial")
public class ScenarioNotFoundException  extends RuntimeException {

    public ScenarioNotFoundException(String scenarioId) {
        super("Scenario [" + scenarioId + "] not found !");
    }

    public ScenarioNotFoundException(String scenarioId, Integer version) {
        super("Scenario [" + scenarioId + "] with version [" + version + "] not found !");
    }
}
