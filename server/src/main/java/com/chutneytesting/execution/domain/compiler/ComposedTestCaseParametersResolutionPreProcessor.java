package com.chutneytesting.execution.domain.compiler;

import static java.util.Collections.emptyMap;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.isBlank;

import com.chutneytesting.design.domain.globalvar.GlobalvarRepository;
import com.chutneytesting.design.domain.scenario.TestCaseMetadata;
import com.chutneytesting.design.domain.scenario.TestCaseMetadataImpl;
import com.chutneytesting.design.domain.scenario.compose.Strategy;
import com.chutneytesting.execution.domain.ExecutionRequest;
import com.chutneytesting.execution.domain.scenario.composed.ExecutableComposedScenario;
import com.chutneytesting.execution.domain.scenario.composed.ExecutableComposedStep;
import com.chutneytesting.execution.domain.scenario.composed.ExecutableComposedTestCase;
import com.chutneytesting.execution.domain.scenario.composed.StepImplementation;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ComposedTestCaseParametersResolutionPreProcessor implements TestCasePreProcessor<ExecutableComposedTestCase> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ComposedTestCaseParametersResolutionPreProcessor.class);

    private final GlobalvarRepository globalvarRepository;
    private final ObjectMapper objectMapper;

    ComposedTestCaseParametersResolutionPreProcessor(GlobalvarRepository globalvarRepository, ObjectMapper objectMapper) {
        this.globalvarRepository = globalvarRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public ExecutableComposedTestCase apply(ExecutionRequest executionRequest) {
        return this.apply((ExecutableComposedTestCase) executionRequest.testCase, executionRequest.environment);
    }

    private ExecutableComposedTestCase apply(ExecutableComposedTestCase testCase, String environment) {
        Map<String, String> globalVariable = globalvarRepository.getFlatMap();

        if (!StringUtils.isBlank(environment)) {
            globalVariable.put("environment", environment);
        }

        testCase = this.applyOnStrategy(testCase, globalVariable);

        return new ExecutableComposedTestCase(
            applyToMetadata(testCase.metadata, testCase.executionParameters, globalVariable),
            applyToScenario(testCase.composedScenario, testCase.executionParameters, globalVariable),
            testCase.executionParameters);
    }

    public ExecutableComposedTestCase applyOnStrategy(ExecutableComposedTestCase testCase, Map<String, String> globalVariable) {
        Map<String, String> testCaseDataSet = applyOnCurrentStepDataSet(testCase.executionParameters, emptyMap(), globalVariable);
        return new ExecutableComposedTestCase(
            testCase.metadata,
            applyOnStrategy(testCase.composedScenario, testCaseDataSet, globalVariable),
            testCaseDataSet
        );
    }

    private ExecutableComposedScenario applyOnStrategy(ExecutableComposedScenario composedScenario, Map<String, String> testCaseDataSet, Map<String, String> globalVariable) {
        return ExecutableComposedScenario.builder()
            .withComposedSteps(
                composedScenario.composedSteps.stream()
                    .map(step -> applyOnStepStrategy(step, testCaseDataSet, globalVariable))
                    .collect(Collectors.toList())
            )
            .withParameters(composedScenario.parameters)
            .build();
    }

    private ExecutableComposedStep applyOnStepStrategy(ExecutableComposedStep composedStep, Map<String, String> parentDataset, Map<String, String> globalVariable) {
        Map<String, String> scopedDataset = applyOnCurrentStepDataSet(composedStep.executionParameters, parentDataset, globalVariable);

        return ExecutableComposedStep.builder()
            .withName(composedStep.name)
            .withSteps(
                composedStep.steps.stream()
                    .map(f -> applyOnStepStrategy(f, scopedDataset, globalVariable))
                    .collect(Collectors.toList())
            )
            .withImplementation(composedStep.stepImplementation)
            .withStrategy(applyToStrategy(composedStep.strategy, scopedDataset, globalVariable))
            .withExecutionParameters(composedStep.executionParameters)
            .build();
    }

    private Strategy applyToStrategy(Strategy strategy, Map<String, String> scopedDataset, Map<String, String> globalVariable) {
        Map<String, Object> parameters = new HashMap<>();
        strategy.parameters.forEach((key, value) -> parameters.put(key, replaceParams(value.toString(), scopedDataset, globalVariable)));
        return new Strategy(strategy.type, parameters);
    }

    private TestCaseMetadata applyToMetadata(TestCaseMetadata metadata, Map<String, String> dataSet, Map<String, String> globalVariable) {
        return TestCaseMetadataImpl.TestCaseMetadataBuilder
            .from(metadata)
            .withTitle(replaceParams(metadata.title(), globalVariable, dataSet))
            .withDescription(replaceParams(metadata.description(), globalVariable, dataSet))
            .build();
    }

    private ExecutableComposedScenario applyToScenario(ExecutableComposedScenario composedScenario, Map<String, String> testCaseDataSet, Map<String, String> globalVariable) {
        return ExecutableComposedScenario.builder()
            .withComposedSteps(
                composedScenario.composedSteps.stream()
                    .map(step -> applyToComposedStep(step, testCaseDataSet, globalVariable))
                    .collect(Collectors.toList())
            )
            .withParameters(composedScenario.parameters)
            .build();
    }

    private ExecutableComposedStep applyToComposedStep(ExecutableComposedStep composedStep, Map<String, String> parentDataset, Map<String, String> globalVariable) {
        Map<String, String> scopedDataset = applyOnCurrentStepDataSet(composedStep.executionParameters, parentDataset, globalVariable);
        List<ExecutableComposedStep> subSteps = composedStep.steps;

        // Preprocess substeps - Recurse
        return ExecutableComposedStep.builder()
            .withName(replaceParams(composedStep.name, globalVariable, scopedDataset))
            .withSteps(
                subSteps.stream()
                    .map(f -> applyToComposedStep(f, scopedDataset, globalVariable))
                    .collect(Collectors.toList())
            )
            .withImplementation(composedStep.stepImplementation.flatMap(si -> applyToImplementation(si, scopedDataset, globalVariable)))
            .withStrategy(composedStep.strategy)
            .withExecutionParameters(scopedDataset)
            .build();
    }

    private Optional<StepImplementation> applyToImplementation(StepImplementation stepImplementation, Map<String, String> scopedDataset, Map<String, String> globalVariable) {
        try {
            String blob = replaceParams(objectMapper.writeValueAsString(stepImplementation), globalVariable, scopedDataset, StringEscapeUtils::escapeJson);
            StepImplementation impl = objectMapper.readValue(blob, StepImplementation.class);
            return ofNullable(impl);
        } catch (IOException e) {
            LOGGER.error("Error reading step implementation", e);
            return ofNullable(stepImplementation);
        }
    }

    private Map<String, String> applyOnCurrentStepDataSet(Map<String, String> currentStepDataset, Map<String, String> parentDataset, Map<String, String> globalVariables) {
        Map<String, String> scopedDataset = new HashMap<>();
        Map<Boolean, List<Map.Entry<String, String>>> splitDataSet = currentStepDataset.entrySet().stream().collect(Collectors.groupingBy(o -> isBlank(o.getValue())));

        ofNullable(splitDataSet.get(true))
            .ifPresent(l -> l.forEach(e ->
                scopedDataset.put(e.getKey(), ofNullable(parentDataset.get(e.getKey())).orElse(""))
            ));

        ofNullable(splitDataSet.get(false))
            .ifPresent(l -> l.forEach(e ->
                scopedDataset.put(e.getKey(), replaceParams(e.getValue(), globalVariables, parentDataset))
            ));

        return scopedDataset;
    }

}
