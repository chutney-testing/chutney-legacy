package com.chutneytesting.engine.domain.execution.engine;

import com.chutneytesting.engine.domain.execution.ScenarioExecution;
import com.chutneytesting.engine.domain.execution.engine.step.Step;
import com.chutneytesting.engine.domain.execution.engine.step.StepContext;
import com.chutneytesting.task.spi.injectable.Target;

public interface StepExecutor {

    void execute(ScenarioExecution scenarioExecution, StepContext stepContext, Target target, Step step);

}
