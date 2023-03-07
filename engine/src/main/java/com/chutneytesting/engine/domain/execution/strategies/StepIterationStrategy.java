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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StepIterationStrategy implements StepExecutionStrategy {

    private static final Logger LOGGER = LoggerFactory.getLogger(StepIterationStrategy.class);

    @Override
    public String getType() {
        return "for";
    }

    @Override
    public Status execute(ScenarioExecution scenarioExecution,
                          Step step,
                          ScenarioContext scenarioContext,
                          StepExecutionStrategies strategies) {

        StepStrategyDefinition strategyDefinition = step.strategy().orElseThrow(
            () -> new IllegalArgumentException("Strategy definition cannot be empty")
        );

        List<Map<String, Object>> dataset = strategyDefinition.strategyProperties.getProperty("dataset", List.class);

        // build N steps
        AtomicInteger index = new AtomicInteger(0);
        List<Pair<Step, Map<String, Object>>> iterations = dataset.stream()
            .map(data -> buildIteration(index.getAndIncrement(), step, data))
            .peek(e -> step.addStepExecution(e.getKey()))
            .collect(Collectors.toList());

        step.beginExecution(scenarioExecution);
        iterations.forEach(it -> it.getLeft().execute(scenarioExecution, scenarioContext, it.getRight()));
        step.endExecution(scenarioExecution);

        return step.status();

    }

    private Pair<Step, Map<String, Object>> buildIteration(Integer index, Step step, Map<String, Object> data) {
        return Pair.of(
            new Step(step.dataEvaluator(), iterationDefinition(index, step.definition(), step.dataEvaluator(), data), step.executor(), emptyList()),
            data
        );
    }

    private StepDefinition iterationDefinition(Integer index, StepDefinition definition, StepDataEvaluator evaluator, Map<String, Object> data) {
        // remplacement dans le name / inputs / outputs / validations
        return StepDefinitionBuilder.copyFrom(definition)
            .withName(evaluator.evaluate(index(index, definition.name), data))
            .withInputs(index(index, definition.inputs()))
            .withOutputs(index(index, definition.outputs))
            .withValidations(index(index, definition.validations))
            .withStrategy(new StepStrategyDefinition("", new StrategyProperties()))
            .build();
    }

    private String index(Integer index, String string) {
        return  string.replace("<index>", index.toString());
    }

    private Map<String, Object> index(Integer index, Map<String, Object> inputs) {
        return inputs.entrySet().stream()
            .collect(Collectors.toMap(
                e -> index(index, e.getKey()),
                e -> index(index, e.getValue().toString())
            ));
    }
}
