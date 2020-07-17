package com.chutneytesting.execution.domain.compiler;

import com.chutneytesting.design.domain.compose.ComposableScenario;
import com.chutneytesting.design.domain.compose.ComposableTestCase;
import com.chutneytesting.design.domain.compose.FunctionalStep;
import com.chutneytesting.design.domain.compose.Strategy;
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

class ComposableTestCaseLoopPreProcessor implements TestCasePreProcessor<ComposableTestCase> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ComposableTestCaseLoopPreProcessor.class);

    private final ObjectMapper objectMapper;

    ComposableTestCaseLoopPreProcessor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public ComposableTestCase apply(ComposableTestCase testCase, String environment) {
        return new ComposableTestCase(
            testCase.id,
            testCase.metadata,
            apply(testCase.composableScenario),
            testCase.computedParameters);
    }

    private ComposableScenario apply(ComposableScenario composableScenario) {
        return ComposableScenario.builder()
            .withFunctionalSteps(
                composableScenario.functionalSteps.stream()
                    .map(this::apply)
                    .collect(Collectors.toList())
            )
            .withParameters(composableScenario.parameters)
            .build();
    }

    private FunctionalStep apply(FunctionalStep functionalStep) {
        FunctionalStep step = applyOnChildren(functionalStep);

        if ("Loop".equals(functionalStep.strategy.type)) {
            return createStepWithIterations(step);
        }

        return step;
    }

    private FunctionalStep applyOnChildren(FunctionalStep functionalStep) {
        List<FunctionalStep> subSteps = functionalStep.steps.stream().map(this::apply).collect(Collectors.toList());
        return FunctionalStep.builder()
            .from(functionalStep)
            .withSteps(subSteps)
            .overrideDataSetWith(functionalStep.dataSet)
            .build();
    }

    private FunctionalStep createStepWithIterations(FunctionalStep functionalStep) {
        try {
            String data = (String) Optional.ofNullable(functionalStep.strategy.parameters.get("data")).orElse("[{}]");
            List<Map<String, String>> iterationData = objectMapper.readValue(StringEscapeUtils.unescapeJson(data), List.class);

            return FunctionalStep.builder()
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

    private List<FunctionalStep> createStepIterations(FunctionalStep functionalStep, List<Map<String, String>> iterationData) {
        AtomicInteger index = new AtomicInteger(0);
        return iterationData.stream()
            .map(i -> {
                index.getAndIncrement();
                return createIteration(i, functionalStep, index);
            })
            .collect(Collectors.toList());
    }

    private FunctionalStep createIteration(Map<String, String> iterationData, FunctionalStep step, AtomicInteger index) {
        Stream<Map<String, String>> combined = Stream.of(iterationData, step.dataSet);

        Map<String, String> params = combined
            .flatMap(map -> map.entrySet().stream())
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (data, param) -> data));

        return FunctionalStep.builder()
            .from(step)
            .withName(step.name + " - iteration " + index)
            .overrideDataSetWith(params)
            .withStrategy(Strategy.DEFAULT)
            .build();

    }
}
