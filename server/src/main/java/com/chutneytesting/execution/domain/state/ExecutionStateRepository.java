package com.chutneytesting.execution.domain.state;

import java.util.Optional;
import java.util.Set;

public interface ExecutionStateRepository {

    Set<RunningScenarioState> runningScenarios();

    void notifyExecutionStart(String scenarioId);

    void notifyExecutionEnd(String scenarioId);

    Optional<RunningScenarioState> runningState(String scenarioId);
}
