package com.chutneytesting.engine.domain.execution.engine;

import com.chutneytesting.action.spi.injectable.Target;
import com.chutneytesting.engine.domain.execution.ScenarioExecution;
import com.chutneytesting.engine.domain.execution.engine.step.Step;

public interface StepExecutor {

    void execute(ScenarioExecution scenarioExecution, Target target, Step step);

}
