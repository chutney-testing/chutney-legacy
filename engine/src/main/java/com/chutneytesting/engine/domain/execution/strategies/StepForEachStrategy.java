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

import static java.util.Collections.emptyList;

import com.chutneytesting.engine.domain.execution.ScenarioExecution;
import com.chutneytesting.engine.domain.execution.StepDefinition;
import com.chutneytesting.engine.domain.execution.StepDefinitionBuilder;
import com.chutneytesting.engine.domain.execution.engine.StepExecutor;
import com.chutneytesting.engine.domain.execution.engine.evaluation.StepDataEvaluator;
import com.chutneytesting.engine.domain.execution.engine.scenario.ScenarioContext;
import com.chutneytesting.engine.domain.execution.engine.step.Step;
import com.chutneytesting.engine.domain.execution.report.Status;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;

public class StepForEachStrategy implements StepExecutionStrategy {

    @Override
    public String getType() {
        return "for";
    }

    @Override
    public Status execute(ScenarioExecution scenarioExecution,
                          Step step,
                          ScenarioContext scenarioContext,
                          Map<String, Object> localContext,
                          StepExecutionStrategies strategies) {

        StepStrategyDefinition strategyDefinition = step.strategy().orElseThrow(
            () -> new IllegalArgumentException("Strategy definition cannot be empty")
        );

        List<Map<String, Object>> dataset = getDataset(step, scenarioContext, strategyDefinition, step.dataEvaluator());
        final String indexName = (String) Optional.ofNullable(strategyDefinition.strategyProperties.get("index")).orElse("i");
        step.beginExecution(scenarioExecution);
        AtomicInteger index = new AtomicInteger(0);
        Map<String, Object> context = new HashMap<>(scenarioContext);
        context.putAll(localContext);
        step.resolveName(context, true);
        if (step.isParentStep()) {
            List<Step> subSteps = List.copyOf(step.subSteps());
            step.removeStepExecution();

            List<Pair<Step, Map<String, Object>>> iterations = dataset.stream()
                .map(iterationContext -> buildParentIteration(indexName, index.getAndIncrement(), step, subSteps, iterationContext))
                .peek(p -> step.addStepExecution(p.getLeft()))
                .toList();

            iterations.forEach(it ->
                DefaultStepExecutionStrategy.instance.execute(scenarioExecution, it.getLeft()/*step*/, scenarioContext, it.getRight()/*localContext*/, strategies));

        } else {
            List<Pair<Step, Map<String, Object>>> iterations = dataset.stream()
                .map(iterationContext -> buildIteration(indexName, index.getAndIncrement(), step, iterationContext))
                .peek(e -> step.addStepExecution(e.getKey()))
                .toList();

            iterations.forEach(it -> it.getLeft().execute(scenarioExecution, scenarioContext, it.getRight()));
        }
        step.endExecution(scenarioExecution);
        return step.status();
    }

    private static List<Map<String, Object>> getDataset(Step step, ScenarioContext scenarioContext, StepStrategyDefinition strategyDefinition, StepDataEvaluator evaluator) {
        List<Map<String, Object>> dataset = (List<Map<String, Object>>) step.dataEvaluator().evaluate(strategyDefinition.strategyProperties.get("dataset"), scenarioContext);
        if (dataset.isEmpty()) {
            throw new IllegalArgumentException("Step iteration cannot have empty dataset");
        }
        List<Map<String, Object>> evaluatedDataset = dataset.stream()
            .map(iterationData -> iterationData.entrySet().stream()
                .map(e -> Map.entry(e.getKey(), evaluator.evaluate(e.getValue(), scenarioContext)))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)))
            .toList();
        return evaluatedDataset;
    }

    private Pair<Step, Map<String, Object>> buildParentIteration(String indexName, Integer index, Step step, List<Step> subSteps, Map<String, Object> iterationContext) {

        StepDefinition newDef = iterationDefinition(indexName, index, step.definition(), new StepStrategyDefinition("", new StrategyProperties()));
        List<Step> newSubSteps = subSteps.stream().map(
            subStep -> buildIterationDefinition(indexName, index, subStep.dataEvaluator(), subStep.definition(), subStep.executor(), subStep.subSteps(), subStep.strategy().orElse(new StepStrategyDefinition("", new StrategyProperties())))
        ).collect(Collectors.toList());

        return Pair.of(
            new Step(step.dataEvaluator(), newDef, step.executor(), newSubSteps),
            iterationContext
        );
    }

    private Pair<Step, Map<String, Object>> buildIteration(String indexName, Integer index, Step step, Map<String, Object> iterationContext) {
        return Pair.of(
            new Step(step.dataEvaluator(), iterationDefinition(indexName, index, step.definition(), new StepStrategyDefinition("", new StrategyProperties())), step.executor(), emptyList()),
            iterationContext
        );
    }

    private Step buildIterationDefinition(String indexName, Integer index, StepDataEvaluator dataEvaluator, StepDefinition definition, StepExecutor executor, List<Step> subStep, StepStrategyDefinition strategy) {
        definition = iterationDefinition(indexName, index, definition, Optional.ofNullable(strategy).orElse(new StepStrategyDefinition("", new StrategyProperties())));
        return new Step(dataEvaluator, definition, executor, subStep.stream().map(step -> buildIterationDefinition(indexName, index, step.dataEvaluator(), step.definition(), step.executor(), step.subSteps(), step.strategy().orElse(null))).toList());
    }

    private StepDefinition iterationDefinition(String indexName, Integer index, StepDefinition definition, StepStrategyDefinition strategyDefinition) {
        return StepDefinitionBuilder.copyFrom(definition)
            .withName(index(indexName, index, definition.name))
            .withInputs(index(indexName, index, definition.inputs()))
            .withOutputs(index(indexName, index, definition.outputs))
            .withValidations(index(indexName, index, definition.validations))
            .withStrategy(strategyDefinition)
            .withSteps(definition
                .steps
                .stream()
                .map(subStep -> iterationDefinition(indexName, index, subStep, subStep.getStrategy().orElse(new StepStrategyDefinition("", new StrategyProperties()))))
                .toList())
            .build();
    }

    private String index(String indexName, Integer index, String string) {
        return string.replace("<" + indexName + ">", index.toString());
    }

    private Map<String, Object> index(String indexName, Integer index, Map<String, Object> inputs) {
        return inputs.entrySet().stream()
            .collect(Collectors.toMap(
                e -> index(indexName, index, e.getKey()),
                e -> index(indexName, index, e.getValue())
            ));
    }

    private List<Object> index(String indexName, Integer index, List<Object> inputs) {
        return inputs.stream()
            .map(e -> index(indexName, index, e))
            .toList();
    }

    private Object index(String indexName, Integer index, Object value) {
        if (value instanceof Map) {
            return index(indexName, index, (Map) value);
        }

        if (value instanceof List) {
            return index(indexName, index, (List) value);
        }

        return index(indexName, index, (String) value);

    }
}
