package com.chutneytesting.execution.domain.compiler;

import com.chutneytesting.design.domain.scenario.compose.Strategy;
import com.chutneytesting.execution.domain.ExecutionRequest;
import com.chutneytesting.execution.domain.scenario.ExecutableComposedFunctionalStep;
import com.chutneytesting.execution.domain.scenario.ExecutableComposedScenario;
import com.chutneytesting.execution.domain.scenario.ExecutableComposedTestCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ComposableTestCaseLoopPreProcessor implements TestCasePreProcessor<ExecutableComposedTestCase> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ComposableTestCaseLoopPreProcessor.class);

    private final ObjectMapper objectMapper;

    ComposableTestCaseLoopPreProcessor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public ExecutableComposedTestCase apply(ExecutionRequest executionRequest) {
        ExecutableComposedTestCase testCase = (ExecutableComposedTestCase) executionRequest.testCase;
        return new ExecutableComposedTestCase(
            testCase.id,
            testCase.metadata,
            apply(testCase.composableScenario),
            testCase.computedParameters);
    }

    private ExecutableComposedScenario apply(ExecutableComposedScenario composableScenario) {
        return ExecutableComposedScenario.builder()
            .withFunctionalSteps(
                composableScenario.functionalSteps.stream()
                    .map(this::apply)
                    .collect(Collectors.toList())
            )
            .withParameters(composableScenario.parameters)
            .build();
    }

    private ExecutableComposedFunctionalStep apply(ExecutableComposedFunctionalStep functionalStep) {
        ExecutableComposedFunctionalStep step = applyOnChildren(functionalStep);

        if ("Loop".equals(functionalStep.strategy.type)) {
            return createStepWithIterations(step);
        }

        return step;
    }

    private ExecutableComposedFunctionalStep applyOnChildren(ExecutableComposedFunctionalStep functionalStep) {
        List<ExecutableComposedFunctionalStep> subSteps = functionalStep.steps.stream().map(this::apply).collect(Collectors.toList());
        return ExecutableComposedFunctionalStep.builder()
            .from(functionalStep)
            .withSteps(subSteps)
            .overrideDataSetWith(functionalStep.dataSet)
            .build();
    }

    private ExecutableComposedFunctionalStep createStepWithIterations(ExecutableComposedFunctionalStep functionalStep) {
        try {
            String data = (String) Optional.ofNullable(functionalStep.strategy.parameters.get("data")).orElse("[{}]");
            List<Map<String, String>> iterationData = objectMapper.readValue(StringEscapeUtils.unescapeJson(data), List.class);

            return ExecutableComposedFunctionalStep.builder()
                .from(functionalStep)
                .withStrategy(Strategy.DEFAULT)
                .withSteps(createStepIterations(functionalStep, iterationData))
                .overrideDataSetWith(buildDatasetWithAliases(functionalStep.dataSet))
                .build();

        } catch (IOException e) {
            LOGGER.error("Error reading json loop data", e);
            return functionalStep;
        }
    }

    private List<ExecutableComposedFunctionalStep> createStepIterations(ExecutableComposedFunctionalStep functionalStep, List<Map<String, String>> iterationData) {
        AtomicInteger index = new AtomicInteger(0);
        return iterationData.stream()
            .map(i -> {
                index.getAndIncrement();
                return createIteration(i, functionalStep, index);
            })
            .collect(Collectors.toList());
    }

    private ExecutableComposedFunctionalStep createIteration(Map<String, String> iterationData, ExecutableComposedFunctionalStep step, AtomicInteger index) {
        Stream<Map<String, String>> combined = Stream.of(iterationData, step.dataSet);

        Map<String, String> params = combined
            .flatMap(map -> map.entrySet().stream())
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (data, param) -> data));

        return ExecutableComposedFunctionalStep.builder()
            .from(step)
            .withName(step.name + " - iteration " + index)
            .overrideDataSetWith(params)
            .withStrategy(Strategy.DEFAULT)
            .build();

    }
}
