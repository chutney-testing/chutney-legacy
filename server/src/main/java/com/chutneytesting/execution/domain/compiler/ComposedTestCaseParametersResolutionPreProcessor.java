package com.chutneytesting.execution.domain.compiler;

import static java.util.Collections.emptyMap;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.isBlank;

import com.chutneytesting.design.domain.globalvar.GlobalvarRepository;
import com.chutneytesting.design.domain.scenario.TestCaseMetadata;
import com.chutneytesting.design.domain.scenario.TestCaseMetadataImpl;
import com.chutneytesting.design.domain.scenario.compose.Strategy;
import com.chutneytesting.execution.domain.ExecutionRequest;
import com.chutneytesting.execution.domain.scenario.ExecutableComposedScenario;
import com.chutneytesting.execution.domain.scenario.ExecutableComposedStep;
import com.chutneytesting.execution.domain.scenario.ExecutableComposedTestCase;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.text.StringEscapeUtils;

public class ComposedTestCaseParametersResolutionPreProcessor implements TestCasePreProcessor<ExecutableComposedTestCase> {

    private final GlobalvarRepository globalvarRepository;

    ComposedTestCaseParametersResolutionPreProcessor(GlobalvarRepository globalvarRepository) {
        this.globalvarRepository = globalvarRepository;
    }

    @Override
    public ExecutableComposedTestCase apply(ExecutionRequest executionRequest) {
        ExecutableComposedTestCase testCase = (ExecutableComposedTestCase) executionRequest.testCase;
        Map<String, String> globalVariable = globalvarRepository.getFlatMap();
        makeEnvironmentNameAsGlobalVariable(globalVariable, executionRequest.environment);
        return new ExecutableComposedTestCase(
            testCase.id,
            applyToMetadata(testCase.metadata, testCase.computedParameters, globalVariable),
            applyToScenario(testCase.composedScenario, testCase.computedParameters, globalVariable),
            testCase.computedParameters);
    }

    public ExecutableComposedTestCase applyOnStrategy(ExecutableComposedTestCase testCase, String environment) {
        Map<String, String> globalVariable = globalvarRepository.getFlatMap();
        makeEnvironmentNameAsGlobalVariable(globalVariable, environment);
        Map<String, String> testCaseDataSet = applyOnCurrentStepDataSet(testCase.computedParameters, emptyMap(), globalVariable);
        return new ExecutableComposedTestCase(
            testCase.id,
            testCase.metadata,
            applyOnStrategy(testCase.composedScenario, testCaseDataSet, globalVariable),
            testCaseDataSet);
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
        Map<String, String> scopedDataset = applyOnCurrentStepDataSet(composedStep.dataset, parentDataset, globalVariable);
        List<ExecutableComposedStep> subSteps = composedStep.steps;

        // Preprocess substeps - Recurse
        return ExecutableComposedStep.builder()
            .withName(replaceParams(composedStep.name, globalvarRepository.getFlatMap(), scopedDataset))
            .withSteps(
                subSteps.stream()
                    .map(f -> applyToComposedStep(f, scopedDataset, globalVariable))
                    .collect(Collectors.toList())
            )
            .withImplementation(composedStep.implementation.map(v -> replaceParams(v, globalvarRepository.getFlatMap(), scopedDataset, StringEscapeUtils::escapeJson)))
            .withStrategy(composedStep.strategy)
            .overrideDataSetWith(scopedDataset)
            .build();
    }

    private Map<String, String> applyOnCurrentStepDataSet(Map<String, String> currentStepDataset, Map<String, String> parentDataset, Map<String, String> globalVariables) {
        Map<String, String> scopedDataset = new HashMap<>();
        Map<Boolean, List<Map.Entry<String, String>>> splitDataSet = currentStepDataset.entrySet().stream().collect(Collectors.groupingBy(o -> isBlank(o.getValue())));

        ofNullable(splitDataSet.get(true))
            .ifPresent(l -> l.forEach(e -> scopedDataset.put(e.getKey(), ofNullable(parentDataset.get(e.getKey())).orElse(""))));

        ofNullable(splitDataSet.get(false))
            .ifPresent(l -> l.forEach(e -> {
                scopedDataset.put(e.getKey(), replaceParams(e.getValue(), globalVariables, parentDataset));
            }));

        return scopedDataset;
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
        Map<String, String> scopedDataset = applyOnCurrentStepDataSet(composedStep.dataset, parentDataset, globalVariable);

        return ExecutableComposedStep.builder()
            .withName(composedStep.name)
            .withSteps(
                composedStep.steps.stream()
                    .map(f -> applyOnStepStrategy(f, scopedDataset, globalVariable))
                    .collect(Collectors.toList())
            )
            .withImplementation(composedStep.implementation)
            .withStrategy(applyToStrategy(composedStep.strategy, scopedDataset, globalVariable))
            .overrideDataSetWith(composedStep.dataset)
            .build();
    }

    private Strategy applyToStrategy(Strategy strategy, Map<String, String> scopedDataset, Map<String, String> globalVariable) {
        Map<String, Object> parameters = new HashMap<>();
        strategy.parameters.forEach((key, value) -> parameters.put(key, replaceParams(value.toString(), scopedDataset, globalVariable)));
        return new Strategy(strategy.type, parameters);
    }

}
