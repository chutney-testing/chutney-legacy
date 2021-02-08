package com.chutneytesting.execution.domain.compiler;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.text.StringEscapeUtils;

public class ComposedTestCaseDatatableIterationsPreProcessor implements TestCasePreProcessor<ExecutableComposedTestCase> {

    private final DataSetRepository dataSetRepository;

    ComposedTestCaseDatatableIterationsPreProcessor(DataSetRepository dataSetRepository) {
        this.dataSetRepository = dataSetRepository;
    }

    @Override
    public ExecutableComposedTestCase apply(ExecutionRequest executionRequest) {
        ExecutableComposedTestCase testCase = (ExecutableComposedTestCase) executionRequest.testCase;
        return apply(testCase);
    }

    ExecutableComposedTestCase apply(ExecutableComposedTestCase testCase) {
        Optional<DataSet> oDataset = testCase.metadata.datasetId().map(dataSetRepository::findById);
        if (!oDataset.isPresent()) {
            return testCase;
        }

        DataSet dataset = oDataset.get();
        Map<Boolean, List<String>> matchedHeaders = findDatableHeadersMatchingExecutionParameters(testCase, dataset.datatable);

        return new ExecutableComposedTestCase(
            testCase.metadata,
            applyToScenario(testCase.composedScenario, matchedHeaders, dataset),
            applyToExecutionParameters(testCase.executionParameters, matchedHeaders.get(Boolean.TRUE), dataset));
    }

    private Map<Boolean, List<String>> findDatableHeadersMatchingExecutionParameters(ExecutableComposedTestCase testCase, List<Map<String, String>> datatable) {
        Map<Boolean, List<String>> matchedHeaders = new HashMap<>();
        if (!datatable.isEmpty()) {
            Set<String> headers = datatable.get(0).keySet();
            matchedHeaders = testCase.executionParameters.keySet().stream()
                .collect(groupingBy(headers::contains));
        }
        matchedHeaders.putIfAbsent(Boolean.TRUE, emptyList());
        matchedHeaders.putIfAbsent(Boolean.FALSE, emptyList());
        return matchedHeaders;
    }

    private Map<String, String> applyToExecutionParameters(Map<String, String> executionParameters, List<String> matchedHeaders, DataSet dataset) {
        HashMap<String, String> parameters = new HashMap<>(executionParameters);

        Map<String, String> constants = dataset.constants;
        executionParameters.keySet().stream()
            .filter(constants::containsKey)
            .forEach(key -> parameters.put(key, constants.get(key)));

        executionParameters.keySet().stream()
            .filter(matchedHeaders::contains)
            .forEach(parameters::remove);

        return parameters;
    }

    private ExecutableComposedScenario applyToScenario(ExecutableComposedScenario composedScenario, Map<Boolean, List<String>> matchedHeaders, DataSet dataset) {
        Map<String, Integer> iterationOutputs = new HashMap<>();
        return ExecutableComposedScenario.builder()
            .withComposedSteps(
                composedScenario.composedSteps.stream()
                    .map(cs -> applyToStep(cs, matchedHeaders, dataset, iterationOutputs))
                    .collect(toList())
            )
            .withParameters(composedScenario.parameters)
            .build();
    }

    private ExecutableComposedStep applyToStep(ExecutableComposedStep composedStep, Map<Boolean, List<String>> matchedHeaders, DataSet dataset, Map<String, Integer> iterationOutputs) {
        // ex. parameter : { "**header**" : "" }
        Set<String> executionParametersReferencingDatableHeadersInKey = findHeadersReferencesWithinEmptyParametersKey(composedStep.executionParameters, matchedHeaders.get(Boolean.TRUE));

        // ex: parameter : { "paramName" : "**header1** + **header2**" }
        Map<String, Set<String>> executionParametersReferencingDatableHeadersInValue = findHeadersReferencesWithinParameterValues(composedStep.executionParameters, matchedHeaders.get(Boolean.TRUE));
        Map<String, Integer> usedIndexedOutput = findUsageOfPreviousOutputInImplementation(composedStep, iterationOutputs);
        Map<String, Integer> usedIndexedOutputInDataset = findUsageOfPreviousOutputInExecutionParameters(composedStep, iterationOutputs);

        if (executionParametersReferencingDatableHeadersInKey.isEmpty() && executionParametersReferencingDatableHeadersInValue.isEmpty() && usedIndexedOutput.isEmpty() && usedIndexedOutputInDataset.isEmpty()) {
            removeObsoleteIndexedOutputs(iterationOutputs, composedStep);
            return composedStep;
        }

        // ex. parameter : { "paramName" : "" }
        Map<String, String> emptyExecutionParameters = composedStep.executionParameters.entrySet().stream()
            .filter(e -> e.getValue().isEmpty())
            .filter(e -> !executionParametersReferencingDatableHeadersInKey.contains(e.getKey()))
            .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));

        matchedHeaders.get(Boolean.FALSE).forEach(s -> emptyExecutionParameters.put(s, ""));

        return ExecutableComposedStep.builder()
            .from(composedStep)
            .withImplementation(empty())
            .withStrategy(new Strategy(DataSetIterationsStrategy.TYPE, emptyMap()))
            .withSteps(buildStepIterations(composedStep, dataset.datatable, executionParametersReferencingDatableHeadersInKey, executionParametersReferencingDatableHeadersInValue, iterationOutputs))
            .withExecutionParameters(buildExecutionParametersWithAliases(emptyExecutionParameters))
            .build();
    }

    private Map<String, Integer> findUsageOfPreviousOutputInExecutionParameters(ExecutableComposedStep composedStep, Map<String, Integer> iterationOutputs) {
        return iterationOutputs.entrySet().stream()
            .filter(previousOutput ->
                composedStep.executionParameters.entrySet().stream()
                    .anyMatch(input -> input.getKey().contains("#" + previousOutput.getKey())
                        || usePreviousIterationOutput(previousOutput.getKey(), input.getValue())
                    )
            )
            .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private Map<String, Integer> findUsageOfPreviousOutputInImplementation(ExecutableComposedStep composedStep, Map<String, Integer> iterationOutputs) {
        Map<String, Integer> map = new HashMap<>();
        composedStep.stepImplementation.ifPresent(si ->
            map.putAll(iterationOutputs.entrySet().stream()
                .filter(previousOutput ->
                    contains(previousOutput.getKey(), si.inputs) || contains(previousOutput.getKey(), si.outputs)
                )
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue))
            )
        );

        return map;
    }

    private boolean contains(String previousOutput, Map<String, Object> map) {
        return map.entrySet().stream()
            .anyMatch(entry -> entry.getKey().contains("#" + previousOutput)
                || usePreviousIterationOutput(previousOutput, entry.getValue())
            );
    }

    private boolean usePreviousIterationOutput(String previousOutput, Object value) {
        if (value instanceof String) {
            return usePreviousIterationOutput(previousOutput, (String) value);
        } else if (value instanceof Map) {
            return usePreviousIterationOutput(previousOutput, (Map<String, String>) value);
        }
        return false;
    }

    private boolean usePreviousIterationOutput(String previousOutput, String value) {
        return value.contains("#" + previousOutput);
    }

    private boolean usePreviousIterationOutput(String previousOutput, Map<String, String> value) {
        return value.entrySet().stream().anyMatch(e -> e.getKey().contains("#" + previousOutput) || usePreviousIterationOutput(previousOutput, e.getValue()));
    }

    private Set<String> findHeadersReferencesWithinEmptyParametersKey(Map<String, String> executionParameters, List<String> matchedHeaders) {
        return executionParameters.entrySet().stream()
            .filter(e -> e.getValue().isEmpty() && matchedHeaders.contains(e.getKey()))
            .map(Map.Entry::getKey)
            .collect(toSet());
    }

    private Map<String, Set<String>> findHeadersReferencesWithinParameterValues(Map<String, String> executionParameters, List<String> matchedHeaders) {
        // Key is an execution parameter name, value is a set of headers name
        HashMap<String, Set<String>> executionParametersReferencingDatableHeader = new HashMap<>();
        // TODO - change double for loop + condition by first listing step parameters matching a header, then only proceed matched ones
        for (Map.Entry<String, String> parameter : executionParameters.entrySet()) {
            String value = parameter.getValue();
            for (String matchedHeader : matchedHeaders) {
                if (value.contains("**" + matchedHeader + "**")) {
                    executionParametersReferencingDatableHeader.putIfAbsent(parameter.getKey(), new HashSet<>());
                    executionParametersReferencingDatableHeader.get(parameter.getKey()).add(matchedHeader);
                }
            }
        }
        return executionParametersReferencingDatableHeader;
    }

    private List<ExecutableComposedStep> buildStepIterations(ExecutableComposedStep composedStep,
                                                             List<Map<String, String>> datatable,
                                                             Set<String> csNovaluedEntries,
                                                             Map<String, Set<String>> executionParametersReferencingDatableHeader,
                                                             Map<String, Integer> iterationOutputs) {
        List<ExecutableComposedStep> iterations;
        AtomicInteger index = new AtomicInteger(0);

        iterations = generateIterationsForDatatable(composedStep, datatable, csNovaluedEntries, executionParametersReferencingDatableHeader, iterationOutputs, index);

        if (iterations.isEmpty()) {
            iterations = generateIterationsForPreviousIterationOutputs(composedStep, iterationOutputs, index);
        }

        if (iterations.isEmpty()) {
            iterations = generateIterationsForPreviousIterationOutputsInDataset(composedStep, iterationOutputs, index);
        }

        rememberIterationsCountForEachOutput(iterationOutputs, index);
        updateIndexedOutputsUsingExecutionParameterValues(iterationOutputs, composedStep);
        return iterations;
    }

    private List<ExecutableComposedStep> generateIterationsForDatatable(ExecutableComposedStep composedStep, List<Map<String, String>> datatable, Set<String> csNovaluedEntries, Map<String, Set<String>> executionParametersReferencingDatableHeader, Map<String, Integer> iterationOutputs, AtomicInteger index) {
        List<Map<String, String>> iterationData = findUsageOfDatasetDatatable(csNovaluedEntries, executionParametersReferencingDatableHeader, datatable);
        return iterationData.stream()
            .map(mv -> {
                index.getAndIncrement();

                return ExecutableComposedStep.builder()
                    .from(composedStep)
                    .withImplementation(composedStep.stepImplementation.flatMap(si -> Optional.of(indexIterationIO(si, index, iterationOutputs))))
                    .withName(composedStep.name + " - datatable iteration " + index)
                    .withExecutionParameters(applyIndexedOutputs(updatedExecutionParametersUsingCurrentValue(composedStep.executionParameters, csNovaluedEntries, executionParametersReferencingDatableHeader, mv), index, iterationOutputs))
                    .withSteps(composedStep.steps.stream()
                        .map(s ->
                            ExecutableComposedStep.builder()
                                .from(s)
                                .withImplementation(s.stepImplementation.flatMap(si -> Optional.of(indexIterationIO(si, index, iterationOutputs))))
                                .withExecutionParameters(applyIndexedOutputs(s.executionParameters, index, iterationOutputs))
                                .build())
                        .collect(toList())
                    )
                    .build();
            })
            .collect(toList());
    }

    private List<Map<String, String>> findUsageOfDatasetDatatable(Set<String> csNovaluedEntries, Map<String, Set<String>> csValuedEntriesWithRef, List<Map<String, String>> datatable) {
        Set<String> dataSetEntriesReferenced = csValuedEntriesWithRef.values().stream().flatMap(Collection::stream).collect(toSet());
        return datatable.stream()
            .map(mv ->
                mv.entrySet().stream()
                    .filter(e -> csNovaluedEntries.contains(e.getKey()) || dataSetEntriesReferenced.contains(e.getKey()))
                    .collect(toMap(Map.Entry::getKey, Map.Entry::getValue))
            )
            .distinct()
            .filter(mv -> !mv.isEmpty())
            .collect(toList());
    }

    private List<ExecutableComposedStep> generateIterationsForPreviousIterationOutputs(ExecutableComposedStep composedStep, Map<String, Integer> iterationOutputs, AtomicInteger index) {
        Map<String, Integer> previousOutputs = findUsageOfPreviousOutputInImplementation(composedStep, iterationOutputs);
        int iterationsCount = previousOutputs.values().stream().findFirst().orElse(0);

        List<ExecutableComposedStep> generatedIterations = new ArrayList<>();
        for (int i = 0; i < iterationsCount; i++) {
            index.getAndIncrement();
            generatedIterations.add(
                ExecutableComposedStep.builder()
                    .from(composedStep)
                    .withImplementation(composedStep.stepImplementation.flatMap(si -> Optional.of(indexIterationIO(si, index, iterationOutputs))))
                    .withName(composedStep.name + " - datatable iteration " + index)
                    .withExecutionParameters(composedStep.executionParameters)
                    .build()
            );
        }
        return generatedIterations;
    }

    private List<ExecutableComposedStep> generateIterationsForPreviousIterationOutputsInDataset(ExecutableComposedStep composedStep, Map<String, Integer> iterationOutputs, AtomicInteger index) {
        Map<String, Integer> previousOutputs = findUsageOfPreviousOutputInExecutionParameters(composedStep, iterationOutputs);
        int iterationsCount = previousOutputs.values().stream().findFirst().orElse(0);

        List<ExecutableComposedStep> generatedIterations = new ArrayList<>();
        for (int i = 0; i < iterationsCount; i++) {
            index.getAndIncrement();
            generatedIterations.add(
                ExecutableComposedStep.builder()
                    .from(composedStep)
                    .withImplementation(composedStep.stepImplementation.flatMap(si -> Optional.of(indexIterationIO(si, index, iterationOutputs))))
                    .withName(composedStep.name + " - datatable iteration " + index)
                    .withExecutionParameters(applyIndexedOutputs(composedStep.executionParameters, index, iterationOutputs))
                    .build()
            );
        }
        return generatedIterations;
    }

    private Map<String, String> updatedExecutionParametersUsingCurrentValue(Map<String, String> executionParameters, Set<String> csNovaluedEntries, Map<String, Set<String>> executionParametersReferencingDatableHeader, Map<String, String> mv) {
        Map<String, String> newExecutionParameters = new HashMap<>(executionParameters);
        executionParameters.forEach((k, v) -> {
            if (csNovaluedEntries.contains(k)) {
                newExecutionParameters.put(k, mv.get(k));
            } else if (executionParametersReferencingDatableHeader.containsKey(k)) {
                newExecutionParameters.put(k, replaceParams(v, emptyMap(), mv));
            }
        });
        return newExecutionParameters;
    }

    private StepImplementation indexIterationIO(StepImplementation si, AtomicInteger index, Map<String, Integer> iterationOutputs) {
        rememberIndexedOutput(iterationOutputs, si.outputs);
        return new StepImplementation(
            si.type,
            si.target,
            indexInputs(si.inputs, index, iterationOutputs),
            indexOutputs(si.outputs, index, iterationOutputs)
        );
    }

    private void rememberIndexedOutput(Map<String, Integer> iterationOutputs, Map<String, Object> outputs) {
        Map<String, Integer> collect = outputs.entrySet().stream().collect(toMap(Map.Entry::getKey, e -> 0));
        iterationOutputs.putAll(collect);
    }

    private void rememberIterationsCountForEachOutput(Map<String, Integer> iterationOutputs, AtomicInteger index) {
        iterationOutputs.replaceAll((k, v) -> v = index.get());
    }

    private void updateIndexedOutputsUsingExecutionParameterValues(Map<String, Integer> iterationOutputs, ExecutableComposedStep composedStep) {
        List<ExecutableComposedStep> steps = new ArrayList<>(composedStep.steps);
        Collections.reverse(steps);
        steps.forEach(executableComposedStep ->
            updateIndexedOutputsUsingExecutionParameterValues(iterationOutputs, executableComposedStep)
        );

        composedStep.executionParameters.entrySet().stream()
            .filter(entry -> iterationOutputs.containsKey("**" + entry.getKey() + "**"))
            .forEach(entry -> {
                Integer iterationsCount = iterationOutputs.get("**" + entry.getKey() + "**");
                iterationOutputs.remove("**" + entry.getKey() + "**");
                iterationOutputs.put(composedStep.executionParameters.get(entry.getKey()), iterationsCount);
            });
    }

    private void removeObsoleteIndexedOutputs(Map<String, Integer> iterationOutputs, ExecutableComposedStep composedStep) {
        List<String> list = new ArrayList<>();
        composedStep.stepImplementation.ifPresent(si -> list.addAll(si.outputs.keySet()));
        if (list.isEmpty()) {
            composedStep.steps
                .forEach(executableComposedStep -> removeObsoleteIndexedOutputs(iterationOutputs, executableComposedStep));
        } else {
            composedStep.executionParameters.entrySet().stream()
                .filter(entry -> list.contains("**" + entry.getKey() + "**"))
                .forEach(entry -> {
                    list.remove("**" + entry.getKey() + "**");
                    list.add(entry.getValue());
                });
            list.stream()
                .filter(iterationOutputs::containsKey)
                .forEach(iterationOutputs::remove);
        }
    }

    private Map<String, Object> indexInputs(Map<String, Object> inputs, AtomicInteger index, Map<String, Integer> iterationOutputs) {
        return inputs.entrySet().stream().collect(HashMap::new, (m, e) -> m.put(
            applyIndexedOutputsOnStringValue(e.getKey(), index, iterationOutputs),
            applyIndexedOutputs(e.getValue(), index, iterationOutputs)
            ), HashMap::putAll
        );
    }

    private Map<String, Object> indexOutputs(Map<String, Object> outputs, AtomicInteger index, Map<String, Integer> iterationOutputs) {
        return outputs.entrySet().stream().collect(HashMap::new, (m, e) -> m.put(
            e.getKey() + "_" + index,
            applyIndexedOutputs(e.getValue(), index, iterationOutputs)
            ), HashMap::putAll
        );
    }

    private Object applyIndexedOutputs(Object value, AtomicInteger index, Map<String, Integer> indexedOutput) {
        if (value instanceof String) {
            return applyIndexedOutputsOnStringValue((String) value, index, indexedOutput);
        } else if (value instanceof Map) {
            return applyIndexedOutputs((Map<String, String>) value, index, indexedOutput);
        } else return value;
    }

    private Map<String, String> applyIndexedOutputs(Map<String, String> value, AtomicInteger index, Map<String, Integer> indexedOutput) {
        return value.entrySet().parallelStream().collect(toMap(
            e -> applyIndexedOutputsOnStringValue(e.getKey(), index, indexedOutput),
            e -> applyIndexedOutputsOnStringValue(e.getValue(), index, indexedOutput)
        ));
    }

    private String applyIndexedOutputsOnStringValue(String value, AtomicInteger index, Map<String, Integer> indexedOutput) {
        String tmp = value;
        for (String output : indexedOutput.keySet()) {
            Pattern pattern = Pattern.compile("#" + Pattern.quote(output) + "\\b");
            Matcher matcher = pattern.matcher(tmp);
            if (matcher.find()) {
                tmp = matcher.replaceAll("#" + StringEscapeUtils.escapeJson(output + "_" + index));
            }
        }
        return tmp;
    }

    private Map<String, String> buildExecutionParametersWithAliases(Map<String, String> executionParameters) {
        Map<String, String> aliases = executionParameters.entrySet().stream()
            .filter(e -> isAlias(e.getValue()))
            .collect(Collectors.toMap(a -> a.getValue().substring(2, a.getValue().length() - 2), o -> ""));

        aliases.putAll(executionParameters); // TODO - need to check why we filter and remove aliases and then add them all again ?

        return unmodifiableMap(aliases);
    }

    Pattern aliasPattern = Pattern.compile("^\\*\\*(.+)\\*\\*$");
    private boolean isAlias(String paramValue) {
        return aliasPattern.matcher(paramValue).matches();
    }
}
