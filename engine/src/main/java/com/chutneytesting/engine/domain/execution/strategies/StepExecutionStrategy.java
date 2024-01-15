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

import static java.util.Collections.emptyMap;

import com.chutneytesting.engine.domain.execution.ScenarioExecution;
import com.chutneytesting.engine.domain.execution.engine.scenario.ScenarioContext;
import com.chutneytesting.engine.domain.execution.engine.step.Step;
import com.chutneytesting.engine.domain.execution.report.Status;
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
        return execute(scenarioExecution, step, scenarioContext, emptyMap(), strategies);
    }

    Status execute(ScenarioExecution scenarioExecution,
                   Step step,
                   ScenarioContext scenarioContext,
                   Map<String, Object> localContext,
                   StepExecutionStrategies strategies);

}
