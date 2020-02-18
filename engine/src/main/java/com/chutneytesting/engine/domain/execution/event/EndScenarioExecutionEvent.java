package com.chutneytesting.engine.domain.execution.event;

import com.chutneytesting.engine.domain.execution.ScenarioExecution;
import com.chutneytesting.engine.domain.execution.engine.step.Step;

public class EndScenarioExecutionEvent implements Event {
    public final ScenarioExecution scenarioExecution;
    public final Step step;

    public EndScenarioExecutionEvent(ScenarioExecution scenarioExecution, Step step) {

        this.scenarioExecution = scenarioExecution;
        this.step = step;
    }

    @Override
    public long executionId() {
        return scenarioExecution.executionId;
    }
}
