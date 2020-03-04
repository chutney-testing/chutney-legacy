package com.chutneytesting.execution.domain.compiler;

import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.isBlank;

import com.chutneytesting.design.domain.compose.ComposableScenario;
import com.chutneytesting.design.domain.compose.ComposableTestCase;
import com.chutneytesting.design.domain.compose.FunctionalStep;
import com.chutneytesting.design.domain.compose.Strategy;
import com.chutneytesting.design.domain.globalvar.GlobalvarRepository;
import com.chutneytesting.design.domain.scenario.TestCaseMetadata;
import com.chutneytesting.design.domain.scenario.TestCaseMetadataImpl;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ComposableTestCaseDataSetPreProcessor implements TestCasePreProcessor<ComposableTestCase> {

    private GlobalvarRepository globalvarRepository;

    ComposableTestCaseDataSetPreProcessor(GlobalvarRepository globalvarRepository) {
        this.globalvarRepository = globalvarRepository;
    }

    @Override
    public ComposableTestCase apply(ComposableTestCase testCase) {
        Map<String, String> globalVariable = globalvarRepository.getFlatMap();
        return new ComposableTestCase(
            testCase.id,
            applyToMetadata(testCase.metadata, testCase.dataSet, globalVariable),
            applyToScenario(testCase.composableScenario, testCase.dataSet, globalVariable),
            testCase.dataSet);
    }

    private TestCaseMetadata applyToMetadata(TestCaseMetadata metadata, Map<String, String> dataSet, Map<String, String> globalVariable) {
        return TestCaseMetadataImpl.TestCaseMetadataBuilder
            .from(metadata)
            .withTitle(replaceParams(metadata.title(), globalVariable, dataSet))
            .withDescription(replaceParams(metadata.description(), globalVariable, dataSet))
            .build();
    }

    private ComposableScenario applyToScenario(ComposableScenario composableScenario, Map<String, String> testCaseDataSet, Map<String, String> globalVariable) {
        return ComposableScenario.builder()
            .withFunctionalSteps(
                composableScenario.functionalSteps.stream()
                    .map(step -> applyToFunctionalStep(step, testCaseDataSet, globalVariable))
                    .collect(Collectors.toList())
            )
            .withParameters(composableScenario.parameters)
            .build();
    }

    private FunctionalStep applyToFunctionalStep(FunctionalStep functionalStep, Map<String, String> parentDataset, Map<String, String> globalVariable) {
        Map<String, String> scopedDataset = applyOnCurrentStepDataSet(functionalStep.dataSet, parentDataset, globalVariable);
        List<FunctionalStep> subSteps = functionalStep.steps;

        // Preprocess substeps - Recurse
        FunctionalStep.FunctionalStepBuilder parentStepBuilder = FunctionalStep.builder()
            .withName(replaceParams(functionalStep.name, globalvarRepository.getFlatMap(), scopedDataset))
            .withSteps(
                subSteps.stream()
                    .map(f -> applyToFunctionalStep(f, scopedDataset, globalVariable))
                    .collect(Collectors.toList())
            )
            .withImplementation(functionalStep.implementation.map(v -> replaceParams(v, globalvarRepository.getFlatMap(), scopedDataset)));

        parentStepBuilder
            .withStrategy(applyToStrategy(functionalStep.strategy, scopedDataset, globalVariable))
            .overrideDataSetWith(scopedDataset);

        return parentStepBuilder.build();
    }

    private Strategy applyToStrategy(Strategy strategy, Map<String, String> scopedDataset, Map<String, String> globalVariable) {
        Map<String, Object> parameters = new HashMap<>();
        strategy.parameters.forEach((key, value) -> parameters.put(key, replaceParams(value.toString(), scopedDataset, globalVariable)));
        return new Strategy(strategy.type, parameters);
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

}
