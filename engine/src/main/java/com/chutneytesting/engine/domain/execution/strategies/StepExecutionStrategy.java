package com.chutneytesting.engine.domain.execution.strategies;

import com.chutneytesting.engine.domain.execution.ScenarioExecution;
import com.chutneytesting.engine.domain.execution.engine.scenario.ScenarioContext;
import com.chutneytesting.engine.domain.execution.engine.step.Step;
import com.chutneytesting.engine.domain.execution.report.Status;
import java.util.Collections;
import java.util.Map;

/**
 * Strategy of step execution.
 * <p>From "execution strategy point of view" a step is an action. When executed, that action produces a status.
 * StepExecutionStrategy interface defines step execution behaviour (e.g: sequential or parallel actions
 * execution, retry on error, etc).</p>
 */
public interface StepExecutionStrategy {

    String getType();

    default Status execute(ScenarioExecution scenarioExecution,
                   Step step,
                   ScenarioContext scenarioContext,
                   StepExecutionStrategies strategies) {
        return execute(scenarioExecution, step, scenarioContext, Collections.emptyMap(), strategies);
    }

    Status execute(ScenarioExecution scenarioExecution,
                   Step step,
                   ScenarioContext scenarioContext,
                   Map<String, Object> localContext,
                   StepExecutionStrategies strategies);

}
