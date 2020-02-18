package com.chutneytesting.engine.domain.execution.strategies;

import com.chutneytesting.engine.domain.execution.ScenarioExecution;
import com.chutneytesting.engine.domain.execution.engine.scenario.ScenarioContext;
import com.chutneytesting.engine.domain.execution.engine.step.Step;
import com.chutneytesting.engine.domain.execution.report.Status;
import java.util.Iterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DefaultStepExecutionStrategy implements StepExecutionStrategy {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultStepExecutionStrategy.class);

    public static final DefaultStepExecutionStrategy instance = new DefaultStepExecutionStrategy();

    private DefaultStepExecutionStrategy() {
    }

    @Override
    public String getType() {
        return "";
    }

    @Override
    public Status execute(ScenarioExecution scenarioExecution,
                          Step step,
                          ScenarioContext scenarioContext,
                          StepExecutionStrategies strategies) {
        if (step.isParentStep()) {
            Iterator<Step> subStepsIterator = step.subSteps().iterator();
            step.beginExecution(scenarioExecution);
            Step currentRunningStep = step;
            try {
                Status childStatus = Status.RUNNING;
                while (subStepsIterator.hasNext() && childStatus != Status.FAILURE) {
                    currentRunningStep = subStepsIterator.next();
                    StepExecutionStrategy strategy = strategies.buildStrategyFrom(currentRunningStep);
                    childStatus = strategy.execute(scenarioExecution, currentRunningStep, scenarioContext, strategies);
                }
                return childStatus;
            } catch (RuntimeException e) {
                currentRunningStep.failure(e);
                LOGGER.warn("Intercepted exception!", e);
            } finally {
                step.endExecution(scenarioExecution);
            }
            return step.status();
        }

        return step.execute(scenarioExecution, scenarioContext);
    }
}
