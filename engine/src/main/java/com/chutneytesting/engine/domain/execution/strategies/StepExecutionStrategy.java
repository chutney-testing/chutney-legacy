package com.chutneytesting.engine.domain.execution.strategies;

import com.chutneytesting.engine.domain.execution.ScenarioExecution;
import com.chutneytesting.engine.domain.execution.engine.scenario.ScenarioContext;
import com.chutneytesting.engine.domain.execution.engine.step.Step;
import com.chutneytesting.engine.domain.execution.report.Status;

/**
 * Strategy of step execution.
 * <p>From "execution strategy point of view" a step is an action. When executed, that action produces a status.
 * StepExecutionStrategy interface defines step execution behaviour (e.g: sequential or parallel tasks
 * execution, retry on error, etc).</p>
 */
public interface StepExecutionStrategy {

    String getType();

    Status execute(ScenarioExecution scenarioExecution,
                   Step step,
                   ScenarioContext scenarioContext,
                   StepExecutionStrategies strategies);

}
