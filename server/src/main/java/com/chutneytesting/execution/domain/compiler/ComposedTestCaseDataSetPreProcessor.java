package com.chutneytesting.execution.domain.compiler;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Optional.empty;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

import com.chutneytesting.design.domain.dataset.DataSet;
import com.chutneytesting.design.domain.dataset.DataSetRepository;
import com.chutneytesting.design.domain.scenario.compose.Strategy;
import com.chutneytesting.engine.domain.execution.strategies.DataSetIterationsStrategy;
import com.chutneytesting.execution.domain.ExecutionRequest;
import com.chutneytesting.execution.domain.scenario.composed.ExecutableComposedScenario;
import com.chutneytesting.execution.domain.scenario.composed.ExecutableComposedStep;
import com.chutneytesting.execution.domain.scenario.composed.ExecutableComposedTestCase;
import com.chutneytesting.execution.domain.scenario.composed.StepImplementation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import org.apache.commons.text.StringEscapeUtils;

public class ComposedTestCaseDataSetPreProcessor implements TestCasePreProcessor<ExecutableComposedTestCase> {

    private final DataSetRepository dataSetRepository;

    ComposedTestCaseDataSetPreProcessor(DataSetRepository dataSetRepository) {
        this.dataSetRepository = dataSetRepository;
    }

    @Override
    public ExecutableComposedTestCase apply(ExecutionRequest executionRequest) {
        ExecutableComposedTestCase testCase = (ExecutableComposedTestCase) executionRequest.testCase;
        return apply(testCase);
    }

    ExecutableComposedTestCase apply(ExecutableComposedTestCase testCase) {
        Optional<DataSet> oDataSet = testCase.metadata.datasetId().map(dataSetRepository::findById);
        if (!oDataSet.isPresent()) {
            return testCase;
        }

        DataSet dataSet = oDataSet.get();
        Map<Boolean, List<String>> matchedHeaders = doSomethingWithHeaders(testCase, dataSet.multipleValues);

        return new ExecutableComposedTestCase(
            testCase.id,
            testCase.metadata,
            applyToScenario(testCase.composedScenario, matchedHeaders, dataSet),
            applyToComputedParameters(testCase.computedParameters, matchedHeaders.get(Boolean.TRUE), dataSet));
    }

    private Map<Boolean, List<String>> doSomethingWithHeaders(ExecutableComposedTestCase testCase, List<Map<String, String>> multipleValues) {
        Map<Boolean, List<String>> matchedHeaders = new HashMap<>();
        if (!multipleValues.isEmpty()) {
            Set<String> valuesHeaders = multipleValues.get(0).keySet();
            matchedHeaders = testCase.computedParameters.keySet().stream()
                .collect(groupingBy(valuesHeaders::contains));
        }
        matchedHeaders.putIfAbsent(Boolean.TRUE, emptyList());
        matchedHeaders.putIfAbsent(Boolean.FALSE, emptyList());
        return matchedHeaders;
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

    private ExecutableComposedScenario applyToScenario(ExecutableComposedScenario composedScenario, Map<Boolean, List<String>> matchedHeaders, DataSet dataSet) {
        Map<String, Integer> iterationOutputs = new HashMap<>();
        return ExecutableComposedScenario.builder()
            .withComposedSteps(
                composedScenario.composedSteps.stream()
                    .map(cs -> applyToStep(cs, matchedHeaders, dataSet, iterationOutputs))
                    .collect(toList())
            )
            .withParameters(composedScenario.parameters)
            .build();
    }

    private ExecutableComposedStep applyToStep(ExecutableComposedStep composedStep, Map<Boolean, List<String>> matchedHeaders, DataSet dataset, Map<String, Integer> iterationOutputs) {
        Set<String> csNovaluedEntries = findComposedStepNoValuedMatchedEntries(composedStep.dataset, matchedHeaders.get(Boolean.TRUE));
        Map<String, Set<String>> csValuedEntriesWithRef = findComposedStepValuedEntriesWithRef(composedStep.dataset, matchedHeaders.get(Boolean.TRUE));
        Map<String, Integer> usedIndexedOutput = findUsageOfPreviousOutput(composedStep, iterationOutputs);

        if (csNovaluedEntries.isEmpty() && csValuedEntriesWithRef.isEmpty() && usedIndexedOutput.isEmpty()) {
            return composedStep;
        }

        Map<String, String> csLeftEntries = composedStep.dataset.entrySet().stream()
            .filter(e -> e.getValue().isEmpty())
            .filter(e -> !csNovaluedEntries.contains(e.getKey()))
            .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));

        matchedHeaders.get(Boolean.FALSE).forEach(s -> csLeftEntries.put(s, ""));

        return ExecutableComposedStep.builder()
            .from(composedStep)
            .withImplementation(empty())
            .withStrategy(new Strategy(DataSetIterationsStrategy.TYPE, emptyMap()))
            .withSteps(buildStepIterations(composedStep, csNovaluedEntries, csValuedEntriesWithRef, dataset.multipleValues, iterationOutputs))
            .overrideDataSetWith(buildDatasetWithAliases(csLeftEntries))
            .build();
    }

    private Map<String, Integer> findUsageOfPreviousOutput(ExecutableComposedStep composedStep, Map<String, Integer> iterationOutputs) {
        Map<String, Integer> m = new HashMap<>();
        composedStep.stepImplementation.ifPresent( si ->
            m.putAll(iterationOutputs.entrySet().stream()
            .filter(previousOutput ->
                si.inputs.entrySet().stream()
                    .anyMatch(input -> input.getKey().equals("${#" + previousOutput.getKey() + "}")
                        || usePreviousIterationOutput(previousOutput.getKey(), input.getValue())
                    ))
            .collect(toMap(Map.Entry::getKey, Map.Entry::getValue))));

        return m;
    }

    private boolean usePreviousIterationOutput(String s, Object value) {
        if (value instanceof String) {
            return usePreviousIterationOutput(s, (String) value);
        } else if (value instanceof Map) {
            return usePreviousIterationOutput(s, (Map<String, String>) value);
        }
        return false;
    }

    private boolean usePreviousIterationOutput(String s, Map<String, String> value) {
        return value.entrySet().stream().anyMatch(e -> e.getKey().equals("${#" + s + "}") || usePreviousIterationOutput(s, e.getValue()));
    }

    private boolean usePreviousIterationOutput(String s, String value) {
        return value.contains("${#" + s + "}");
    }

    private Set<String> findComposedStepNoValuedMatchedEntries(Map<String, String> csDataset, List<String> matchedHeaders) {
        return csDataset.entrySet().stream()
            .filter(e -> e.getValue().isEmpty() && matchedHeaders.contains(e.getKey()))
            .map(Map.Entry::getKey)
            .collect(toSet());
    }

    private Map<String, Set<String>> findComposedStepValuedEntriesWithRef(Map<String, String> csDataSet, List<String> matchedHeaders) {
        HashMap<String, Set<String>> valuedEntriesWithRef = new HashMap<>();
        for (Map.Entry<String, String> csData : csDataSet.entrySet()) {
            String value = csData.getValue();
            for (String matchedHeader : matchedHeaders) {
                if (value.contains("**" + matchedHeader + "**")) {
                    valuedEntriesWithRef.putIfAbsent(csData.getKey(), new HashSet<>());
                    valuedEntriesWithRef.get(csData.getKey()).add(matchedHeader);
                }
            }
        }
        return valuedEntriesWithRef;
    }

    private List<ExecutableComposedStep> buildStepIterations(ExecutableComposedStep composedStep,
                                                             Set<String> csNovaluedEntries,
                                                             Map<String, Set<String>> csValuedEntriesWithRef,
                                                             List<Map<String, String>> multipleValues,
                                                             Map<String, Integer> iterationOutputs) {

        Set<String> dataSetEntriesReferenced = csValuedEntriesWithRef.values().stream().flatMap(Collection::stream).collect(toSet());
        List<Map<String, String>> iterationData = multipleValues.stream()
            .map(mv ->
                mv.entrySet().stream()
                    .filter(e -> csNovaluedEntries.contains(e.getKey()) || dataSetEntriesReferenced.contains(e.getKey()))
                    .collect(toMap(Map.Entry::getKey, Map.Entry::getValue))
            )
            .distinct()
            .filter(mv -> !mv.isEmpty())
            .collect(toList());

        AtomicInteger index = new AtomicInteger(0);
        List<ExecutableComposedStep> iterations = iterationData.stream()
            .map(mv -> {
                index.getAndIncrement();

                return ExecutableComposedStep.builder()
                    .from(composedStep)
                    .withImplementation(composedStep.stepImplementation.flatMap(si -> Optional.of(indexIterationIO(si, index, iterationOutputs))))
                    .withName(composedStep.name + " - dataset iteration " + index)
                    .withSteps(composedStep.steps.stream()
                        .map(s ->
                            ExecutableComposedStep.builder()
                                .from(s)
                                .withImplementation(s.stepImplementation.flatMap(si -> Optional.of(indexIterationIO(si, index, iterationOutputs))))
                                .build())
                        .collect(toList())
                    )
                    .overrideDataSetWith(updatedDatasetUsingCurrentValue(composedStep.dataset, csNovaluedEntries, csValuedEntriesWithRef, mv))
                    .build();
            })
            .collect(toList());

        Map<String, Integer> usageOfPreviousOutput = findUsageOfPreviousOutput(composedStep, iterationOutputs);
        List<ExecutableComposedStep> iterationsFromUsingPreviousOutput = new ArrayList<>();
        for (int i = 0; i < Optional.ofNullable(usageOfPreviousOutput.get("result")).orElse(0); i++) {
            index.getAndIncrement();
            iterationsFromUsingPreviousOutput.add(ExecutableComposedStep.builder()
                .from(composedStep)
                .withImplementation(composedStep.stepImplementation.flatMap(si -> Optional.of(indexIterationIO(si, index, iterationOutputs))))
                .withName(composedStep.name + " - dataset iteration " + index)
                .build());
        }

        iterations.addAll(iterationsFromUsingPreviousOutput);
        iterationOutputs.replaceAll((k, v) -> v = index.get());
        return iterations;
    }

    private Map<String, String> updatedDatasetUsingCurrentValue(Map<String, String> dataset, Set<String> csNovaluedEntries, Map<String, Set<String>> csValuedEntriesWithRef, Map<String, String> mv) {
        Map<String, String> newDataSet = new HashMap<>(dataset);
        dataset.forEach((k, v) -> {
            if (csNovaluedEntries.contains(k)) {
                newDataSet.put(k, mv.get(k));
            } else if (csValuedEntriesWithRef.containsKey(k)) {
                newDataSet.put(k, replaceParams(v, emptyMap(), mv));
            }
        });
        return newDataSet;
    }

    private StepImplementation indexIterationIO(StepImplementation si, AtomicInteger index, Map<String, Integer> iterationOutputs) {
        rememberIndexedOutput(iterationOutputs, si.outputs);
        return new StepImplementation(
            si.type,
            si.target,
            indexInputs(si.inputs, index, iterationOutputs),
            indexOutputs(si.outputs, index)
        );
    }

    private Map<String, Object> indexInputs(Map<String, Object> inputs, AtomicInteger index, Map<String, Integer> iterationOutputs) {
        Map<String, Object> collect = inputs.entrySet().parallelStream().collect(toMap(e -> (String) applyIndexedOutput(e.getKey(), index, iterationOutputs), e -> applyIndexedOutput(e.getValue(), index, iterationOutputs)));
        return collect;
    }

    private Map<String, Object> indexOutputs(Map<String, Object> outputs, AtomicInteger index) {
        return outputs.entrySet().parallelStream().collect(toMap(e -> e.getKey() + "_" + index, Map.Entry::getValue));
    }

    private Object applyIndexedOutput(Object value, AtomicInteger index, Map<String, Integer> iterationOutputs) {
        return applyIndexedOutput(value, index, iterationOutputs, StringEscapeUtils::escapeJson);
    }

    private Object applyIndexedOutput(Object value, AtomicInteger index, Map<String, Integer> indexedOutput, Function<String, String> escapeValueFunction) {
        if (value instanceof String) {
            String tmp = (String) value;
            for (String output : indexedOutput.keySet()) {
                tmp = tmp.replace("#" + output, escapeValueFunction.apply("#" + output + "_" + index));
            }
            return tmp;
        } else if (value instanceof Map) {
            Map<String, String> tmp = (Map<String, String>) value;
            Map<String, String> collect = tmp.entrySet().parallelStream().collect(toMap(
                e -> {
                    String s = e.getKey();
                    for (String output : indexedOutput.keySet()) {
                        s = s.replace("#" + output, escapeValueFunction.apply("#" + output + "_" + index));
                    }
                    return s;
                },
                e -> {
                    String s = e.getValue();
                    for (String output : indexedOutput.keySet()) {
                        s = s.replace("#" + output, escapeValueFunction.apply("#" + output + "_" + index));
                    }
                    return s;
                })
            );
            return collect;
        } else return value;
    }

    private void rememberIndexedOutput(Map<String, Integer> iterationOutputs, Map<String, Object> outputs) {
        Map<String, Integer> collect = outputs.entrySet().stream().collect(toMap(Map.Entry::getKey, e -> 0));
        iterationOutputs.putAll(collect);
    }
}
