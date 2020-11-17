package com.chutneytesting.execution.domain.compiler;

import com.chutneytesting.design.domain.scenario.compose.Strategy;
import com.chutneytesting.execution.domain.ExecutionRequest;
import com.chutneytesting.execution.domain.scenario.ExecutableComposedScenario;
import com.chutneytesting.execution.domain.scenario.ExecutableComposedStep;
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

class ComposedTestCaseLoopPreProcessor implements TestCasePreProcessor<ExecutableComposedTestCase> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ComposedTestCaseLoopPreProcessor.class);

    private final ObjectMapper objectMapper;

    ComposedTestCaseLoopPreProcessor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public ExecutableComposedTestCase apply(ExecutionRequest executionRequest) {
        ExecutableComposedTestCase testCase = (ExecutableComposedTestCase) executionRequest.testCase;
        return new ExecutableComposedTestCase(
            testCase.id,
            testCase.metadata,
            apply(testCase.composedScenario),
            testCase.computedParameters);
    }

    private ExecutableComposedScenario apply(ExecutableComposedScenario composedScenario) {
        return ExecutableComposedScenario.builder()
            .withComposedSteps(
                composedScenario.composedSteps.stream()
                    .map(this::apply)
                    .collect(Collectors.toList())
            )
            .withParameters(composedScenario.parameters)
            .build();
    }

    private ExecutableComposedStep apply(ExecutableComposedStep composedStep) {
        ExecutableComposedStep step = applyOnChildren(composedStep);

        if ("Loop".equals(composedStep.strategy.type)) {
            return createStepWithIterations(step);
        }

        return step;
    }

    private ExecutableComposedStep applyOnChildren(ExecutableComposedStep composedStep) {
        List<ExecutableComposedStep> subSteps = composedStep.steps.stream().map(this::apply).collect(Collectors.toList());
        return ExecutableComposedStep.builder()
            .from(composedStep)
            .withSteps(subSteps)
            .overrideDataSetWith(composedStep.dataset)
            .build();
    }

    private ExecutableComposedStep createStepWithIterations(ExecutableComposedStep composedStep) {
        try {
            String data = (String) Optional.ofNullable(composedStep.strategy.parameters.get("data")).orElse("[{}]");
            List<Map<String, String>> iterationData = objectMapper.readValue(StringEscapeUtils.unescapeJson(data), List.class);

            return ExecutableComposedStep.builder()
                .from(composedStep)
                .withStrategy(Strategy.DEFAULT)
                .withSteps(createStepIterations(composedStep, iterationData))
                .overrideDataSetWith(buildDatasetWithAliases(composedStep.dataset))
                .build();

        } catch (IOException e) {
            LOGGER.error("Error reading json loop data", e);
            return composedStep;
        }
    }

    private List<ExecutableComposedStep> createStepIterations(ExecutableComposedStep composedStep, List<Map<String, String>> iterationData) {
        AtomicInteger index = new AtomicInteger(0);
        return iterationData.stream()
            .map(i -> {
                index.getAndIncrement();
                return createIteration(i, composedStep, index);
            })
            .collect(Collectors.toList());
    }

    private ExecutableComposedStep createIteration(Map<String, String> iterationData, ExecutableComposedStep step, AtomicInteger index) {
        Stream<Map<String, String>> combined = Stream.of(iterationData, step.dataset);

        Map<String, String> params = combined
            .flatMap(map -> map.entrySet().stream())
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (data, param) -> data));

        return ExecutableComposedStep.builder()
            .from(step)
            .withName(step.name + " - iteration " + index)
            .overrideDataSetWith(params)
            .withStrategy(Strategy.DEFAULT)
            .build();

    }
}
