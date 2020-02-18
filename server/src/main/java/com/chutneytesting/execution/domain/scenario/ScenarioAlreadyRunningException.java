package com.chutneytesting.execution.domain.scenario;

import com.chutneytesting.execution.domain.state.RunningScenarioState;

@SuppressWarnings("serial")
public class ScenarioAlreadyRunningException extends RuntimeException {

    public ScenarioAlreadyRunningException(RunningScenarioState runningScenarioState) {
        super("Scenario [" + runningScenarioState.scenarioId() + "] is already running since " + runningScenarioState.startTime());
    }
}
