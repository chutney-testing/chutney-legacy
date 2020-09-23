package com.chutneytesting.engine.domain.execution.strategies;

import com.chutneytesting.engine.domain.execution.ScenarioExecution;
import com.chutneytesting.engine.domain.execution.engine.scenario.ScenarioContext;
import com.chutneytesting.engine.domain.execution.engine.step.Step;
import com.chutneytesting.engine.domain.execution.report.Status;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DataSetIterationsStrategy implements StepExecutionStrategy {

    public static final String TYPE = "dataset-iterations-strategy";
    private static final Logger LOGGER = LoggerFactory.getLogger(DataSetIterationsStrategy.class);

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public Status execute(ScenarioExecution scenarioExecution,
                          Step step,
                          ScenarioContext scenarioContext,
                          StepExecutionStrategies strategies) {
        if (step.isParentStep()) {
            List<Status> childrenStatus = new ArrayList<>();
            Iterator<Step> subStepsIterator = step.subSteps().iterator();
            step.beginExecution(scenarioExecution);
            Step currentRunningStep = step;
            try {
                while (subStepsIterator.hasNext()) {
                    currentRunningStep = subStepsIterator.next();
                    StepExecutionStrategy strategy = strategies.buildStrategyFrom(currentRunningStep);
                    childrenStatus.add(strategy.execute(scenarioExecution, currentRunningStep, scenarioContext, strategies));
                }
            } catch (RuntimeException e) {
                currentRunningStep.failure(e);
                LOGGER.warn("Intercepted exception!", e);
            } finally {
                step.endExecution(scenarioExecution);
            }
            return Status.worst(childrenStatus);
        }

        return step.execute(scenarioExecution, scenarioContext);
    }
}
