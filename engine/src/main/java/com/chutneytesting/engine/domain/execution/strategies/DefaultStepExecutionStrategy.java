/*
 * Copyright 2017-2023 Enedis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.chutneytesting.engine.domain.execution.strategies;

import com.chutneytesting.engine.domain.execution.ScenarioExecution;
import com.chutneytesting.engine.domain.execution.engine.scenario.ScenarioContext;
import com.chutneytesting.engine.domain.execution.engine.step.Step;
import com.chutneytesting.engine.domain.execution.report.Status;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
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
                          Map<String, Object> localContext,
                          StepExecutionStrategies strategies) {
        if (step.isParentStep()) {
            Map<String, Object> context = new HashMap<>(scenarioContext);
            context.putAll(localContext);
            step.resolveName(step.dataEvaluator().evaluateString(step.getName(), context));
            Iterator<Step> subStepsIterator = step.subSteps().iterator();
            step.beginExecution(scenarioExecution);
            Step currentRunningStep = step;
            try {
                Map<String, Object> context = new HashMap<>(scenarioContext);
                context.putAll(localContext);
                step.resolveName(context);
                Status childStatus = Status.RUNNING;
                while (subStepsIterator.hasNext() && childStatus != Status.FAILURE) {
                    currentRunningStep = subStepsIterator.next();
                    StepExecutionStrategy strategy = strategies.buildStrategyFrom(currentRunningStep);
                    childStatus = strategy.execute(scenarioExecution, currentRunningStep, scenarioContext, localContext, strategies);
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

        return step.execute(scenarioExecution, scenarioContext, localContext);
    }
}
