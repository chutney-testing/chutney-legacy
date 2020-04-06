package com.chutneytesting.engine.domain.execution.engine;

import com.chutneytesting.engine.domain.environment.TargetImpl;
import com.chutneytesting.engine.domain.execution.ScenarioExecution;
import com.chutneytesting.engine.domain.execution.engine.step.Step;
import com.chutneytesting.engine.domain.execution.engine.step.StepContext;

public interface StepExecutor {

    void execute(ScenarioExecution scenarioExecution, StepContext stepContext, TargetImpl target, Step step);

}
