package com.chutneytesting.design.domain.scenario;

@SuppressWarnings("serial")
public class ScenarioNotFoundException  extends RuntimeException {

    public ScenarioNotFoundException(String scenarioId) {
        super("Scenario [" + scenarioId + "] not found !");
    }

}
