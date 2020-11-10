package com.chutneytesting.execution.domain.compiler;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

import com.chutneytesting.design.domain.dataset.DataSet;
import com.chutneytesting.design.domain.dataset.DataSetRepository;
import com.chutneytesting.design.domain.scenario.compose.Strategy;
import com.chutneytesting.engine.domain.execution.strategies.DataSetIterationsStrategy;
import com.chutneytesting.execution.domain.ExecutionRequest;
import com.chutneytesting.execution.domain.scenario.ExecutableComposedFunctionalStep;
import com.chutneytesting.execution.domain.scenario.ExecutableComposedScenario;
import com.chutneytesting.execution.domain.scenario.ExecutableComposedTestCase;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class ComposableTestCaseDataSetPreProcessor implements TestCasePreProcessor<ExecutableComposedTestCase> {

    private final DataSetRepository dataSetRepository;

    public ComposableTestCaseDataSetPreProcessor(DataSetRepository dataSetRepository) {
        this.dataSetRepository = dataSetRepository;
    }

    @Override
    public ExecutableComposedTestCase apply(ExecutionRequest executionRequest) {
        ExecutableComposedTestCase testCase = (ExecutableComposedTestCase) executionRequest.testCase;
        return apply(testCase);
    }

    private ExecutableComposedTestCase apply(ExecutableComposedTestCase testCase) {
        Optional<DataSet> oDataSet = testCase.metadata.datasetId().map(dataSetRepository::findById);
        if (!oDataSet.isPresent()) {
            return testCase;
        }

        DataSet dataSet = oDataSet.get();
        List<Map<String, String>> multipleValues = dataSet.multipleValues;

        Map<Boolean, List<String>> matchedHeaders = new HashMap<>();
        if (!multipleValues.isEmpty()) {
            Set<String> valuesHeaders = multipleValues.get(0).keySet();
            matchedHeaders = testCase.computedParameters.keySet().stream()
                .collect(groupingBy(valuesHeaders::contains));
        }
        matchedHeaders.putIfAbsent(Boolean.TRUE, emptyList());
        matchedHeaders.putIfAbsent(Boolean.FALSE, emptyList());

        return new ExecutableComposedTestCase(
            testCase.id,
            testCase.metadata,
            applyToScenario(testCase.composableScenario, matchedHeaders, dataSet),
            applyToComputedParameters(testCase.computedParameters, matchedHeaders.get(Boolean.TRUE), dataSet));
    }

    private Map<String, String> applyToComputedParameters(Map<String, String> computedParameters, List<String> matchedHeaders, DataSet dataSet) {
        HashMap<String, String> parameters = new HashMap<>(computedParameters);

        Map<String, String> uniqueValues = dataSet.uniqueValues;
        computedParameters.keySet().stream()
            .filter(uniqueValues::containsKey)
            .forEach(key -> parameters.put(key, uniqueValues.get(key)));

        computedParameters.keySet().stream()
            .filter(matchedHeaders::contains)
            .forEach(parameters::remove);

        return parameters;
    }

    private ExecutableComposedScenario applyToScenario(ExecutableComposedScenario composableScenario, Map<Boolean, List<String>> matchedHeaders, DataSet dataSet) {
        if (matchedHeaders.isEmpty()) {
            return composableScenario;
        }

        return ExecutableComposedScenario.builder()
            .withFunctionalSteps(
                composableScenario.functionalSteps.stream()
                    .map(fs -> applyToScenarioSteps(fs, matchedHeaders, dataSet))
                    .collect(toList())
            )
            .withParameters(composableScenario.parameters)
            .build();
    }

    private ExecutableComposedFunctionalStep applyToScenarioSteps(ExecutableComposedFunctionalStep functionalStep, Map<Boolean, List<String>> matchedHeaders, DataSet dataSet) {
        Set<String> fsNovaluedEntries = findFunctionalStepNoValuedMatchedEntries(functionalStep.dataSet, matchedHeaders.get(Boolean.TRUE));
        Map<String, Set<String>> fsValuedEntriesWithRef = findFunctionalStepValuedEntriesWithRef(functionalStep.dataSet, matchedHeaders.get(Boolean.TRUE));

        if (fsNovaluedEntries.isEmpty() && fsValuedEntriesWithRef.isEmpty()) {
            return functionalStep;
        }

        Map<String, String> fsLeftEntries = functionalStep.dataSet.entrySet().stream()
            .filter(e -> e.getValue().isEmpty())
            .filter(e -> !fsNovaluedEntries.contains(e.getKey()))
            .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));

        matchedHeaders.get(Boolean.FALSE).forEach(s -> fsLeftEntries.put(s, ""));

        return ExecutableComposedFunctionalStep.builder()
            .from(functionalStep)
            .withStrategy(new Strategy(DataSetIterationsStrategy.TYPE, emptyMap()))
            .withSteps(buildStepIterations(functionalStep, fsNovaluedEntries, fsValuedEntriesWithRef, dataSet.multipleValues))
            .overrideDataSetWith(buildDatasetWithAliases(fsLeftEntries))
            .build();
    }

    private Map<String, Set<String>> findFunctionalStepValuedEntriesWithRef(Map<String, String> fsDataSet, List<String> matchedHeaders) {
        HashMap<String, Set<String>> valuedEntriesWithRef = new HashMap<>();
        for (Map.Entry<String, String> fsData : fsDataSet.entrySet()) {
            String value = fsData.getValue();
            for (String matchedHeader : matchedHeaders) {
                if (value.contains("**" + matchedHeader + "**")) {
                    valuedEntriesWithRef.putIfAbsent(fsData.getKey(), new HashSet<>());
                    valuedEntriesWithRef.get(fsData.getKey()).add(matchedHeader);
                }
            }
        }
        return valuedEntriesWithRef;
    }

    private Set<String> findFunctionalStepNoValuedMatchedEntries(Map<String, String> fsDataSet, List<String> matchedHeaders) {
        return fsDataSet.entrySet().stream()
            .filter(e -> e.getValue().isEmpty() && matchedHeaders.contains(e.getKey()))
            .map(Map.Entry::getKey)
            .collect(toSet());
    }

    private List<ExecutableComposedFunctionalStep> buildStepIterations(ExecutableComposedFunctionalStep functionalStep, Set<String> fsNovaluedEntries, Map<String, Set<String>> fsValuedEntriesWithRef, List<Map<String, String>> multipleValues) {
        Set<String> dataSetEntriesReferenced = fsValuedEntriesWithRef.values().stream().flatMap(Collection::stream).collect(toSet());
        List<Map<String, String>> iterationData = multipleValues.stream()
            .map(m ->
                m.entrySet().stream()
                    .filter(e -> fsNovaluedEntries.contains(e.getKey()) || dataSetEntriesReferenced.contains(e.getKey()))
                    .collect(toMap(Map.Entry::getKey, Map.Entry::getValue))
            )
            .distinct()
            .collect(toList());

        AtomicInteger index = new AtomicInteger(0);
        return iterationData.stream()
            .map(mv -> {
                index.getAndIncrement();

                Map<String, String> newDataSet = new HashMap<>(functionalStep.dataSet);
                functionalStep.dataSet.forEach((k, v) -> {
                    if (fsNovaluedEntries.contains(k)) {
                        newDataSet.put(k, mv.get(k));
                    } else if (fsValuedEntriesWithRef.containsKey(k)) {
                        newDataSet.put(k, replaceParams(v, emptyMap(), mv));
                    }
                });

                return ExecutableComposedFunctionalStep.builder()
                    .from(functionalStep)
                    .withName(functionalStep.name + " - dataset iteration " + index)
                    .overrideDataSetWith(newDataSet)
                    .build();
            })
            .collect(toList());
    }
}
