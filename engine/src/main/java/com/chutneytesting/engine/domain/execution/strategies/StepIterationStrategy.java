package com.chutneytesting.engine.domain.execution.strategies;

import static java.util.Collections.emptyList;

import com.chutneytesting.engine.domain.execution.ScenarioExecution;
import com.chutneytesting.engine.domain.execution.StepDefinition;
import com.chutneytesting.engine.domain.execution.StepDefinitionBuilder;
import com.chutneytesting.engine.domain.execution.engine.evaluation.StepDataEvaluator;
import com.chutneytesting.engine.domain.execution.engine.scenario.ScenarioContext;
import com.chutneytesting.engine.domain.execution.engine.step.Step;
import com.chutneytesting.engine.domain.execution.report.Status;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;

public class StepIterationStrategy implements StepExecutionStrategy {

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

        List<Map<String, Object>> dataset = (List<Map<String, Object>>) step.dataEvaluator().evaluate(strategyDefinition.strategyProperties.get("dataset"), scenarioContext);
        AtomicInteger index = new AtomicInteger(0);

        if (step.isParentStep()) {

            List<Step> subSteps = List.copyOf(step.subSteps());
            step.removeStepExecution();
            List<Pair<Step, Map<String, Object>>> iterations = dataset.stream()
                .map(data -> buildParentIteration(index.getAndIncrement(), step, subSteps, data))
                .peek(p -> step.addStepExecution(p.getLeft()))
                .collect(Collectors.toList());

            step.beginExecution(scenarioExecution);
            iterations.forEach(it -> DefaultStepExecutionStrategy.instance.execute(scenarioExecution, it.getLeft()/*step*/, scenarioContext, it.getRight()/*localContext*/, strategies));
            step.endExecution(scenarioExecution);
            return step.status();
        } else {
            List<Pair<Step, Map<String, Object>>> iterations = dataset.stream()
                .map(data -> buildIteration(index.getAndIncrement(), step, data))
                .peek(e -> step.addStepExecution(e.getKey()))
                .collect(Collectors.toList());

            step.beginExecution(scenarioExecution);
            iterations.forEach(it -> it.getLeft().execute(scenarioExecution, scenarioContext, it.getRight()));
            step.endExecution(scenarioExecution);

            return step.status();
        }
    }

    private Pair<Step, Map<String, Object>> buildParentIteration(Integer index, Step step, List<Step> subSteps, Map<String, Object> data) {

        StepDefinition newDef = iterationDefinition(index, step.definition(), step.dataEvaluator(), new StepStrategyDefinition("", new StrategyProperties()), data);
        List<Step> newSubSteps = subSteps.stream().map(
            ss -> {
                StepDefinition ssDef = iterationDefinition(index, ss.definition(), ss.dataEvaluator(), ss.strategy().orElse(new StepStrategyDefinition("", new StrategyProperties())), data);
                return new Step(ss.dataEvaluator(), ssDef, ss.executor(), ss.subSteps());
            }
        ).collect(Collectors.toList());

        return Pair.of(
            new Step(step.dataEvaluator(), newDef, step.executor(), newSubSteps),
            data
        );
    }

    private Pair<Step, Map<String, Object>> buildIteration(Integer index, Step step, Map<String, Object> data) {
        return Pair.of(
            new Step(step.dataEvaluator(), iterationDefinition(index, step.definition(), step.dataEvaluator(), new StepStrategyDefinition("", new StrategyProperties()), data), step.executor(), emptyList()),
            data
        );
    }

    private StepDefinition iterationDefinition(Integer index, StepDefinition definition, StepDataEvaluator evaluator, StepStrategyDefinition strategyDefinition, Map<String, Object> data) {
        return StepDefinitionBuilder.copyFrom(definition)
            .withName(evaluator.evaluate(index(index, definition.name), data))
            .withInputs(index(index, definition.inputs()))
            .withOutputs(index(index, definition.outputs))
            .withValidations(index(index, definition.validations))
            .withStrategy(strategyDefinition)
            .build();
    }

    private String index(Integer index, String string) {
        return string.replace("<index>", index.toString());
    }

    private Map<String, Object> index(Integer index, Map<String, Object> inputs) {
        return inputs.entrySet().stream()
            .collect(Collectors.toMap(
                e -> index(index, e.getKey()),
                e -> index(index, e.getValue().toString())
            ));
    }
}
