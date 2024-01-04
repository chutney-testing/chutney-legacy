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

import static com.chutneytesting.engine.domain.execution.report.Status.SUCCESS;

import com.chutneytesting.engine.domain.execution.ScenarioExecution;
import com.chutneytesting.engine.domain.execution.engine.evaluation.EvaluationException;
import com.chutneytesting.engine.domain.execution.engine.evaluation.StepDataEvaluator;
import com.chutneytesting.engine.domain.execution.engine.scenario.ScenarioContext;
import com.chutneytesting.engine.domain.execution.engine.step.Step;
import com.chutneytesting.engine.domain.execution.report.Status;
import java.util.HashMap;
import java.util.Map;

public class IfStrategy implements StepExecutionStrategy {

    private static final String TYPE = "if";

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public Status execute(ScenarioExecution scenarioExecution, Step step, ScenarioContext scenarioContext, Map<String, Object> localContext, StepExecutionStrategies strategies) {
        final StepStrategyDefinition strategyDefinition = step.strategy().orElseThrow(
            () -> new IllegalArgumentException("Strategy definition cannot be empty")
        );

        final Object conditionObject = strategyDefinition.strategyProperties.getProperty("condition", Object.class);
        if (conditionObject == null) {
            throw new IllegalArgumentException("Property [condition] mandatory for if strategy");
        }
        final Boolean condition = getCondition(scenarioContext, conditionObject, step.dataEvaluator());

        final String conditionStatus = condition ? "step executed" : "step skipped";
        step.addInformation("Execution condition [" + conditionObject + "] = " + conditionStatus);

        if (condition) {
            return DefaultStepExecutionStrategy.instance.execute(scenarioExecution, step, scenarioContext, localContext, strategies);
        } else {
            Map<String, Object> context = new HashMap<>(scenarioContext);
            context.putAll(localContext);
            step.resolveName(step.dataEvaluator().evaluateString(step.getName(), context));
            step.success();
            skipAllSubSteps(step);
        }
        return SUCCESS;
    }

    private void skipAllSubSteps(Step step) {
        if (step.isParentStep()) {
            step.subSteps().forEach(subStep -> {
                subStep.addInformation("Step skipped");
                subStep.success();
                skipAllSubSteps(subStep);
            });
        }
    }

    private static Boolean getCondition(ScenarioContext scenarioContext, Object conditionObject, StepDataEvaluator evaluator) {
        if (conditionObject instanceof Boolean booleanCondition) {
            return booleanCondition;
        } else if (conditionObject instanceof String stringCondition) {
            try {
                return (Boolean) evaluator.evaluate(stringCondition, scenarioContext);
            } catch (EvaluationException | ClassCastException e) {
                throw new RuntimeException("Cannot evaluate execution condition: [" + conditionObject + "]. Error message: " + e.getMessage());
            }
        } else {
            throw new RuntimeException("Cannot evaluate execution condition: [" + conditionObject + "]. should be a boolean or a Spring Expression Language which return a boolean");
        }
    }
}
