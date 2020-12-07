package com.chutneytesting.execution.domain.compiler;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static java.util.Optional.ofNullable;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.chutneytesting.design.domain.dataset.DataSet;
import com.chutneytesting.design.domain.dataset.DataSetRepository;
import com.chutneytesting.design.domain.scenario.TestCaseMetadata;
import com.chutneytesting.design.domain.scenario.TestCaseMetadataImpl;
import com.chutneytesting.design.domain.scenario.compose.Strategy;
import com.chutneytesting.engine.domain.execution.strategies.DataSetIterationsStrategy;
import com.chutneytesting.engine.domain.execution.strategies.DefaultStepExecutionStrategy;
import com.chutneytesting.engine.domain.execution.strategies.RetryWithTimeOutStrategy;
import com.chutneytesting.engine.domain.execution.strategies.SoftAssertStrategy;
import com.chutneytesting.execution.domain.scenario.composed.ExecutableComposedScenario;
import com.chutneytesting.execution.domain.scenario.composed.ExecutableComposedStep;
import com.chutneytesting.execution.domain.scenario.composed.ExecutableComposedTestCase;
import com.chutneytesting.execution.domain.scenario.composed.StepImplementation;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.apache.groovy.util.Maps;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JUnitParamsRunner.class)
public class ComposedTestCaseIterationsPreProcessorTest {

    ComposedTestCaseIterationsPreProcessor sut;
    private DataSetRepository mockDatasetRepository;
    private TestCaseMetadata metadata = TestCaseMetadataImpl.builder().withDatasetId("fakeId").build();

    @Before
    public void setUp() {
        mockDatasetRepository = mock(DataSetRepository.class);
    }

    private void stubDatasetRepository(Map<String, String> uniqueValues, List<Map<String, String>> multipleValues) {
        when(mockDatasetRepository.findById(any())).thenReturn(
            DataSet.builder()
                .withUniqueValues(ofNullable(uniqueValues).orElse(emptyMap()))
                .withMultipleValues(ofNullable(multipleValues).orElse(emptyList()))
                .build()
        );
    }

    @Test
    public void should_override_keys_matching_external_dataset_unique_values_to_testcase_local_dataset() {
        // Given
        Map<String, String> uniqueValues = Maps.of(
            "aKey", "usedInTestCase",
            "anotherKey", "notUsedInTestCase"
        );
        stubDatasetRepository(uniqueValues, null);

        Map<String, String> computedParameters = Maps.of(
            "aKey", "value will be override",
            "localKey", "will be kept as is"
        );

        ExecutableComposedTestCase testCase = new ExecutableComposedTestCase(
            metadata,
            ExecutableComposedScenario.builder().build(),
            computedParameters
        );

        // When
        ComposedTestCaseIterationsPreProcessor sut = new ComposedTestCaseIterationsPreProcessor(mockDatasetRepository);
        ExecutableComposedTestCase processedTestCase = sut.apply(testCase);

        // Then
        assertThat(processedTestCase.computedParameters()).containsOnly(
            entry("aKey", "usedInTestCase"),
            entry("localKey", "will be kept as is")
        );
    }

    @Test
    public void should_remove_keys_matching_external_dataset_multivalues_from_testcase_local_dataset() {
        // Given
        List<Map<String, String>> multipleValues = asList(
            Maps.of("aKey", "aValue"),
            Maps.of("aKey", "anotherValue")
        );
        stubDatasetRepository(null, multipleValues);

        Map<String, String> computedParameters = Maps.of(
            "aKey", "testcase default value",
            "anotherKey", "testcase value"
        );

        ExecutableComposedTestCase testCase = new ExecutableComposedTestCase(
            metadata,
            ExecutableComposedScenario.builder()
                .withComposedSteps(singletonList(ExecutableComposedStep.builder().build()))
                .build(),
            computedParameters
        );

        // When
        sut = new ComposedTestCaseIterationsPreProcessor(mockDatasetRepository);
        ExecutableComposedTestCase processedTestCase = sut.apply(testCase);

        // Then
        assertThat(processedTestCase.computedParameters).containsOnly(
            entry("anotherKey", "testcase value")
        );
    }

    @Test
    public void should_create_iterations_when_step_uses_external_dataset_multivalues() {
        // Given
        List<Map<String, String>> multipleValues = asList(
            Maps.of("aKey", "aValue"),
            Maps.of("aKey", "anotherValue")
        );
        stubDatasetRepository(null, multipleValues);

        ExecutableComposedTestCase testCase = new ExecutableComposedTestCase(
            metadata,
            ExecutableComposedScenario.builder()
                .withComposedSteps(
                    singletonList(
                        ExecutableComposedStep.builder()
                            .withName("Should create 2 iterations for aKey")
                            .withDataset(singletonMap("aKey", ""))
                            .build()
                    )
                )
                .build(),
            singletonMap("aKey", "")
        );

        // When
        sut = new ComposedTestCaseIterationsPreProcessor(mockDatasetRepository);
        ExecutableComposedTestCase processedTestCase = sut.apply(testCase);

        // Then
        ExecutableComposedStep iterableStep = processedTestCase.composedScenario.composedSteps.get(0);
        assertThat(iterableStep.steps).hasSize(2);

        ExecutableComposedStep firstIteration = iterableStep.steps.get(0);
        assertThat(firstIteration.name).isEqualTo("Should create 2 iterations for aKey - dataset iteration 1");
        assertThat(firstIteration.dataset).containsOnly(
            entry("aKey", "aValue")
        );

        ExecutableComposedStep secondIteration = iterableStep.steps.get(1);
        assertThat(secondIteration.name).isEqualTo("Should create 2 iterations for aKey - dataset iteration 2");
        assertThat(secondIteration.dataset).containsOnly(
            entry("aKey", "anotherValue")
        );

    }

    @Test
    // TODO - what is this rule ? why is that ? TestCase dataset takes precedence over local step values ? But why is it done here ?
    public void should_override_iterations_parent_step_dataset() {
        // Given
        List<Map<String, String>> multipleValues = asList(
            Maps.of("aKey", "aValue"),
            Maps.of("aKey", "anotherValue")
        );
        stubDatasetRepository(null, multipleValues);

        Map<String, String> computedParameters = Maps.of(
            "aKey", "",
            "anotherKey", "testcase value"
        );

        ExecutableComposedTestCase testCase = new ExecutableComposedTestCase(
            metadata,
            ExecutableComposedScenario.builder()
                .withComposedSteps(
                    singletonList(
                        ExecutableComposedStep.builder()
                            .withName("Parent local dataset is override by testcase local dataset ")
                            .withDataset(
                                Maps.of(
                                    "aKey", "", // needed for generating iterations
                                    "anotherKey", "step value" // will be override
                                )
                            )
                            .build()
                    )
                )
                .build(),
            computedParameters
        );

        // When
        sut = new ComposedTestCaseIterationsPreProcessor(mockDatasetRepository);
        ExecutableComposedTestCase processedTestCase = sut.apply(testCase);

        // Then
        ExecutableComposedStep parentIterableStep = processedTestCase.composedScenario.composedSteps.get(0);
        assertThat(parentIterableStep.dataset).containsOnly(entry("anotherKey", "")); // TODO - moreover, why is it empty and not using the testcase dataset value ?

    }

    @Test
    public void should_keep_local_dataset_values_when_creating_iterations() {
        // Given
        List<Map<String, String>> multipleValues = asList(
            Maps.of("aKey", "aValue"),
            Maps.of("aKey", "anotherValue")
        );
        stubDatasetRepository(null, multipleValues);

        ExecutableComposedTestCase testCase = new ExecutableComposedTestCase(
            metadata,
            ExecutableComposedScenario.builder()
                .withComposedSteps(
                    singletonList(
                        ExecutableComposedStep.builder()
                            .withName("Should create 2 iterations for aKey")
                            .withDataset(
                                Maps.of(
                                    "aKey", "",
                                    "aLocalParam", "isKeptForEachIteration"
                                )
                            )
                            .build()
                    )
                )
                .build(),
            Maps.of("aKey", "")
        );

        // When
        sut = new ComposedTestCaseIterationsPreProcessor(mockDatasetRepository);
        ExecutableComposedTestCase processedTestCase = sut.apply(testCase);

        // Then
        ExecutableComposedStep iterableStep = processedTestCase.composedScenario.composedSteps.get(0);
        assertThat(iterableStep.steps).hasSize(2);

        ExecutableComposedStep firstIteration = iterableStep.steps.get(0);
        assertThat(firstIteration.name).isEqualTo("Should create 2 iterations for aKey - dataset iteration 1");
        assertThat(firstIteration.dataset).containsOnly(
            entry("aKey", "aValue"),
            entry("aLocalParam", "isKeptForEachIteration")
        );

        ExecutableComposedStep secondIteration = iterableStep.steps.get(1);
        assertThat(secondIteration.name).isEqualTo("Should create 2 iterations for aKey - dataset iteration 2");
        assertThat(secondIteration.dataset).containsOnly(
            entry("aKey", "anotherValue"),
            entry("aLocalParam", "isKeptForEachIteration")
        );

    }

    @Test
    public void should_replace_iteration_dataset_value_when_referring_to_external_dataset_multivalue() {
        // Given
        List<Map<String, String>> multipleValues = asList(
            Maps.of("aKey", "aValue"),
            Maps.of("aKey", "anotherValue")
        );

        stubDatasetRepository(null, multipleValues);

        ExecutableComposedTestCase testCase = new ExecutableComposedTestCase(
            metadata,
            ExecutableComposedScenario.builder()
                .withComposedSteps(
                    singletonList(
                        ExecutableComposedStep.builder()
                            .withName("a step with ref parameter")
                            .withDataset(
                                singletonMap("aLocalParam", "refers to the external dataset key **aKey**")
                            )
                            .build()
                    )
                )
                .build(),
            singletonMap("aKey", "")
        );

        // When
        ComposedTestCaseIterationsPreProcessor sut = new ComposedTestCaseIterationsPreProcessor(mockDatasetRepository);
        ExecutableComposedTestCase processedTestCase = sut.apply(testCase);

        // Then
        ExecutableComposedStep fs = processedTestCase.composedScenario.composedSteps.get(0);

        assertThat(fs.steps.get(0).name).isEqualTo("a step with ref parameter - dataset iteration 1");
        assertThat(fs.steps.get(0).dataset).containsOnly(
            entry("aLocalParam", "refers to the external dataset key aValue")
        );

        assertThat(fs.steps.get(1).name).isEqualTo("a step with ref parameter - dataset iteration 2");
        assertThat(fs.steps.get(1).dataset).containsOnly(
            entry("aLocalParam", "refers to the external dataset key anotherValue")
        );
    }

    @Test
    public void iterations_parent_step_should_have_specific_strategy() {
        // Given
        List<Map<String, String>> multipleValues = asList(
            Maps.of("testcase key for dataset", "dataset first value"),
            Maps.of("testcase key for dataset", "dataset second value")
        );
        stubDatasetRepository(null, multipleValues);

        Map<String, String> computedParameters = Maps.of(
            "testcase key for dataset", "testcase default value for dataset"
        );

        ExecutableComposedTestCase testCase = new ExecutableComposedTestCase(
            metadata,
            ExecutableComposedScenario.builder()
                .withComposedSteps(
                    singletonList(
                        ExecutableComposedStep.builder()
                            .withName("a step with no valued parameter")
                            .withDataset(
                                Maps.of(
                                    "testcase key for dataset", "",
                                    "step 1 param", "value 1"
                                )
                            ).build()
                    )
                )
                .build(),
            computedParameters
        );

        // When
        ComposedTestCaseIterationsPreProcessor sut = new ComposedTestCaseIterationsPreProcessor(mockDatasetRepository);
        ExecutableComposedTestCase processedTestCase = sut.apply(testCase);

        // Then
        assertThat(processedTestCase.composedScenario.composedSteps.get(0).strategy).isEqualTo(
            new Strategy(DataSetIterationsStrategy.TYPE, emptyMap())
        );
    }

    @Test
    @Parameters(method = "strategyDefinitions")
    public void iterations_strategy_inherit_from_parent_strategy_definition(Strategy strategyDefinition) {
        // Given
        List<Map<String, String>> multipleValues = asList(
            Maps.of("testcase key for dataset", "dataset first value"),
            Maps.of("testcase key for dataset", "dataset second value")
        );
        stubDatasetRepository(null, multipleValues);

        Map<String, String> computedParameters = Maps.of(
            "testcase key for dataset", "testcase default value for dataset"
        );

        ExecutableComposedTestCase testCase = new ExecutableComposedTestCase(
            metadata,
            ExecutableComposedScenario.builder()
                .withComposedSteps(
                    singletonList(
                        ExecutableComposedStep.builder()
                            .withStrategy(strategyDefinition)
                            .withName("a step with no valued parameter")
                            .withDataset(
                                Maps.of(
                                    "testcase key for dataset", "",
                                    "step 1 param", "value 1"
                                )
                            ).build()
                    )
                )
                .build(),
            computedParameters
        );

        // When
        ComposedTestCaseIterationsPreProcessor sut = new ComposedTestCaseIterationsPreProcessor(mockDatasetRepository);
        ExecutableComposedTestCase processedTestCase = sut.apply(testCase);

        // Then
        assertThat(processedTestCase.composedScenario.composedSteps.get(0).steps.get(0).strategy).isEqualTo(
            strategyDefinition
        );
    }

    private static Strategy[] strategyDefinitions() {
        return new Strategy[]{
            new Strategy(DefaultStepExecutionStrategy.instance.getType(), emptyMap()),
            new Strategy(new SoftAssertStrategy().getType(), emptyMap()),
            new Strategy(new RetryWithTimeOutStrategy().getType(), emptyMap())
        };
    }

    @Test
    public void should_create_iterations_with_distinct_multiple_values_dataset() {
        // Given
        List<Map<String, String>> multipleValues = asList(
            Maps.of("key 1", "value 1"),
            Maps.of("key 1", "value 12"),
            Maps.of("key 1", "value 1")
        );
        stubDatasetRepository(null, multipleValues);

        Map<String, String> computedParameters = Maps.of(
            "key 1", "testcase value for key 1"
        );

        ExecutableComposedTestCase testCase = new ExecutableComposedTestCase(
            metadata,
            ExecutableComposedScenario.builder()
                .withComposedSteps(
                    singletonList(
                        ExecutableComposedStep.builder()
                            .withName("a step with key 1 dependency")
                            .withDataset(
                                Maps.of(
                                    "key 1", ""
                                )
                            )
                            .build()
                    )
                )
                .build(),
            computedParameters
        );

        // When
        ComposedTestCaseIterationsPreProcessor sut = new ComposedTestCaseIterationsPreProcessor(mockDatasetRepository);
        ExecutableComposedTestCase processedTestCase = sut.apply(testCase);

        // Then
        assertThat(processedTestCase.composedScenario.composedSteps).hasSize(1);

        ExecutableComposedStep fs = processedTestCase.composedScenario.composedSteps.get(0);
        assertThat(fs.name).isEqualTo("a step with key 1 dependency");
        assertThat(fs.dataset).isEmpty();
        assertThat(fs.steps).hasSize(2);
        assertThat(fs.steps.get(0).name).isEqualTo("a step with key 1 dependency - dataset iteration 1");
        assertThat(fs.steps.get(0).dataset).containsOnly(
            entry("key 1", "value 1")
        );
        assertThat(fs.steps.get(1).name).isEqualTo("a step with key 1 dependency - dataset iteration 2");
        assertThat(fs.steps.get(1).dataset).containsOnly(
            entry("key 1", "value 12")
        );
    }

    @Test
    public void should_index_iterations_outputs_and_used_them_for_inputs() {

        // Given
        List<Map<String, String>> multipleValues = asList(
            Maps.of("letter", "a"),
            Maps.of("letter", "b"),
            Maps.of("letter", "c")
        );
        stubDatasetRepository(null, multipleValues);

        ExecutableComposedTestCase testCase = new ExecutableComposedTestCase(
            metadata,
            ExecutableComposedScenario.builder()
                .withComposedSteps(asList(
                    ExecutableComposedStep.builder()
                        .withName("Success - should uppercase each char")
                        .withImplementation(Optional.of(
                            new StepImplementation("success", null, emptyMap(), singletonMap("result", "${new String(\"**letter**\").toUpperCase()"))))
                        .withDataset(singletonMap("letter", ""))
                        .build(),
                    ExecutableComposedStep.builder()
                        .withName("Context put - should create one context entry for each uppercased char")
                        .withImplementation(Optional.of(
                            new StepImplementation("context-put", null, singletonMap("entries", singletonMap("${#result}", "fake_value")), emptyMap())))
                        .build()
                    )
                )
                .build(),
            singletonMap("letter", "")
        );

        sut = new ComposedTestCaseIterationsPreProcessor(mockDatasetRepository);

        // When
        ExecutableComposedTestCase actual = sut.apply(testCase);

        // Then
        ExecutableComposedScenario expected = ExecutableComposedScenario.builder()
            .withComposedSteps(asList(
                ExecutableComposedStep.builder()
                    .withName("Success - should uppercase each char")
                    .withStrategy(new Strategy(DataSetIterationsStrategy.TYPE, emptyMap()))
                    .withSteps(asList(
                        ExecutableComposedStep.builder()
                            .withName("Success - should uppercase each char - dataset iteration 1")
                            .withImplementation(Optional.of(
                                new StepImplementation("success", null, emptyMap(), singletonMap("result_1", "${new String(\"**letter**\").toUpperCase()"))
                            ))
                            .withDataset(singletonMap("letter", "a"))
                            .build(),
                        ExecutableComposedStep.builder()
                            .withName("Success - should uppercase each char - dataset iteration 2")
                            .withImplementation(Optional.of(
                                new StepImplementation("success", null, emptyMap(), singletonMap("result_2", "${new String(\"**letter**\").toUpperCase()"))
                            ))
                            .withDataset(singletonMap("letter", "b"))
                            .build(),
                        ExecutableComposedStep.builder()
                            .withName("Success - should uppercase each char - dataset iteration 3")
                            .withImplementation(Optional.of(
                                new StepImplementation("success", null, emptyMap(), singletonMap("result_3", "${new String(\"**letter**\").toUpperCase()"))
                            ))
                            .withDataset(singletonMap("letter", "c"))
                            .build()
                    ))
                    .build(),
                ExecutableComposedStep.builder()
                    .withName("Context put - should create one context entry for each uppercased char")
                    .withStrategy(new Strategy(DataSetIterationsStrategy.TYPE, emptyMap()))
                    .withSteps(Arrays.asList(
                        ExecutableComposedStep.builder()
                            .withName("Context put - should create one context entry for each uppercased char - dataset iteration 1")
                            .withImplementation(Optional.of(
                                new StepImplementation("context-put", null, singletonMap("entries", singletonMap("${#result_1}", "fake_value")), emptyMap())
                            ))
                            .build(),ExecutableComposedStep.builder()
                            .withName("Context put - should create one context entry for each uppercased char - dataset iteration 2")
                            .withImplementation(Optional.of(
                                new StepImplementation("context-put", null, singletonMap("entries", singletonMap("${#result_2}", "fake_value")), emptyMap())
                            ))
                            .build(),
                        ExecutableComposedStep.builder()
                            .withName("Context put - should create one context entry for each uppercased char - dataset iteration 3")
                            .withImplementation(Optional.of(
                                new StepImplementation("context-put", null, singletonMap("entries", singletonMap("${#result_3}", "fake_value")), emptyMap())
                            ))
                            .build()
                    ))
                    .build()
                )
            )
            .build();

        assertThat(actual.composedScenario.composedSteps.get(0)).isEqualTo(expected.composedSteps.get(0));
        assertThat(actual.composedScenario.composedSteps.get(1)).isEqualTo(expected.composedSteps.get(1));

    }

    @Test
    public void should_create_iterations_when_using_previous_indexed_outputs_in_task_output() {

        // Given
        List<Map<String, String>> multipleValues = asList(
            Maps.of("letter", "a"),
            Maps.of("letter", "b")
        );
        stubDatasetRepository(null, multipleValues);

        ExecutableComposedTestCase testCase = new ExecutableComposedTestCase(
            metadata,
            ExecutableComposedScenario.builder()
                .withComposedSteps(asList(
                    ExecutableComposedStep.builder()
                        .withName("Should generate iterations and index resulting outputs")
                        .withImplementation(Optional.of(
                            new StepImplementation("task", null, emptyMap(), singletonMap("result", "**letter**"))))
                        .withDataset(singletonMap("letter", ""))
                        .build(),
                    ExecutableComposedStep.builder()
                        .withName("Should generate iterations using previous outputs in their output")
                        .withImplementation(Optional.of(
                            new StepImplementation("task", null, emptyMap(), singletonMap("key_to_index", "${#result}"))))
                        .build()
                    )
                )
                .build(),
            singletonMap("letter", "")
        );

        sut = new ComposedTestCaseIterationsPreProcessor(mockDatasetRepository);

        // When
        ExecutableComposedTestCase actual = sut.apply(testCase);

        // Then
        ExecutableComposedScenario expected = ExecutableComposedScenario.builder()
            .withComposedSteps(asList(
                ExecutableComposedStep.builder()
                    .withName("Should generate iterations and index resulting outputs")
                    .withStrategy(new Strategy(DataSetIterationsStrategy.TYPE, emptyMap()))
                    .withSteps(asList(
                        ExecutableComposedStep.builder()
                            .withName("Should generate iterations and index resulting outputs - dataset iteration 1")
                            .withImplementation(Optional.of(
                                new StepImplementation("success", null, emptyMap(), singletonMap("result_1", "**letter**"))
                            ))
                            .withDataset(singletonMap("letter", "a"))
                            .build(),
                        ExecutableComposedStep.builder()
                            .withName("Should generate iterations and index resulting outputs - dataset iteration 2")
                            .withImplementation(Optional.of(
                                new StepImplementation("success", null, emptyMap(), singletonMap("result_2", "**letter**"))
                            ))
                            .withDataset(singletonMap("letter", "b"))
                            .build()
                    ))
                    .build(),
                ExecutableComposedStep.builder()
                    .withName("Should generate iterations using previous outputs in their output")
                    .withStrategy(new Strategy(DataSetIterationsStrategy.TYPE, emptyMap()))
                    .withSteps(Arrays.asList(
                        ExecutableComposedStep.builder()
                            .withName("Should generate iterations using previous outputs in their output - dataset iteration 1")
                            .withImplementation(Optional.of(
                                new StepImplementation("context-put", null, emptyMap(), singletonMap("key_to_index_1", "${#result_1}"))
                            ))
                            .build(),ExecutableComposedStep.builder()
                            .withName("Should generate iterations using previous outputs in their output - dataset iteration 2")
                            .withImplementation(Optional.of(
                                new StepImplementation("context-put", null, emptyMap(), singletonMap("key_to_index_2", "${#result_2}"))
                            ))
                            .build()
                    ))
                    .build()
                )
            )
            .build();

        assertThat(actual.composedScenario.composedSteps.get(0)).isEqualTo(expected.composedSteps.get(0));
        assertThat(actual.composedScenario.composedSteps.get(1)).isEqualTo(expected.composedSteps.get(1));

    }

    @Test
    public void should_generate_iterations_having_both_previous_indexed_outputs_and_indexing_new_outputs() {

        // Given
        List<Map<String, String>> multipleValues = asList(
            Maps.of("letter", "a"),
            Maps.of("letter", "b")
        );
        stubDatasetRepository(null, multipleValues);

        ExecutableComposedTestCase testCase = new ExecutableComposedTestCase(
            metadata,
            ExecutableComposedScenario.builder()
                .withComposedSteps(asList(
                    ExecutableComposedStep.builder()
                        .withName("Should generate 2 iterations for letter a and b")
                        .withImplementation(Optional.of(
                            new StepImplementation("success", null, emptyMap(), singletonMap("output", "**letter**"))
                        ))
                        .withDataset(singletonMap("letter", ""))
                        .build(),
                    ExecutableComposedStep.builder()
                        .withName("Should generate 2 iterations having previous indexed output and indexing 2 other outputs")
                        .withImplementation(Optional.of(
                            new StepImplementation("success", null, singletonMap("taskInput", singletonMap("${#output}", "fake")), singletonMap("otherOutput", "**letter**"))
                        ))
                        .withDataset(singletonMap("letter", ""))
                        .build()
                    )
                )
                .build(),
            singletonMap("letter", "")
        );

        sut = new ComposedTestCaseIterationsPreProcessor(mockDatasetRepository);

        // When
        ExecutableComposedTestCase actual = sut.apply(testCase);

        // Then
        ExecutableComposedScenario expected = ExecutableComposedScenario.builder()
            .withComposedSteps(asList(
                ExecutableComposedStep.builder()
                    .withName("Should generate 2 iterations for letter a and b")
                    .withStrategy(new Strategy(DataSetIterationsStrategy.TYPE, emptyMap()))
                    .withSteps(Arrays.asList(
                        ExecutableComposedStep.builder()
                            .withName("Should generate 2 iterations for letter a and b - dataset iteration 1")
                            .withImplementation(Optional.of(
                                new StepImplementation("success", null, emptyMap(), singletonMap("output_1", "**letter**"))
                            ))
                            .withDataset(singletonMap("letter", "a"))
                            .build(),
                        ExecutableComposedStep.builder()
                            .withName("Should generate 2 iterations for letter a and b - dataset iteration 2")
                            .withImplementation(Optional.of(
                                new StepImplementation("success", null, emptyMap(), singletonMap("output_2", "**letter**"))
                            ))
                            .withDataset(singletonMap("letter", "b"))
                            .build()
                    ))
                    .build(),
                ExecutableComposedStep.builder()
                    .withName("Should generate 2 iterations having previous indexed output and indexing 2 other outputs")
                    .withStrategy(new Strategy(DataSetIterationsStrategy.TYPE, emptyMap()))
                    .withSteps(Arrays.asList(
                        ExecutableComposedStep.builder()
                            .withName("Should generate 2 iterations having previous indexed output and indexing 2 other outputs - dataset iteration 1")
                            .withImplementation(Optional.of(
                                new StepImplementation("success", null, singletonMap("taskInput", singletonMap("${#output_1}", "fake")), singletonMap("otherOutput_1", "**letter**"))
                            ))
                            .withDataset(singletonMap("letter", "a"))
                            .build(),
                        ExecutableComposedStep.builder()
                            .withName("Should generate 2 iterations having previous indexed output and indexing 2 other outputs - dataset iteration 2")
                            .withImplementation(Optional.of(
                                new StepImplementation("success", null, singletonMap("taskInput", singletonMap("${#output_2}", "fake")), singletonMap("otherOutput_2", "**letter**"))
                            ))
                            .withDataset(singletonMap("letter", "b"))
                            .build()
                    ))
                    .build()
                )
            )
            .build();

        assertThat(actual.composedScenario.composedSteps.get(0)).isEqualTo(expected.composedSteps.get(0));
        assertThat(actual.composedScenario.composedSteps.get(1)).isEqualTo(expected.composedSteps.get(1));

    }

    @Test
    public void should_generate_iterations_indexing_only_new_outputs() {

        // Given
        List<Map<String, String>> multipleValues = asList(
            Maps.of("letter", "a"),
            Maps.of("letter", "b")
        );
        stubDatasetRepository(null, multipleValues);

        ExecutableComposedTestCase testCase = new ExecutableComposedTestCase(
            metadata,
            ExecutableComposedScenario.builder()
                .withComposedSteps(asList(
                    ExecutableComposedStep.builder()
                        .withName("Should generate 2 iterations for letter a and b")
                        .withImplementation(Optional.of(
                            new StepImplementation("success", null, emptyMap(), singletonMap("output", "**letter**"))
                        ))
                        .withDataset(singletonMap("letter", ""))
                        .build(),
                    ExecutableComposedStep.builder()
                        .withName("Should generate 2 iterations having previous indexed output and indexing 2 other outputs")
                        .withImplementation(Optional.of(
                            new StepImplementation("success", null, singletonMap("taskInput", singletonMap("${#notFromIteration}", "fake")), singletonMap("otherOutput", "**letter**"))
                        ))
                        .withDataset(singletonMap("letter", ""))
                        .build()
                    )
                )
                .build(),
            singletonMap("letter", "")
        );

        sut = new ComposedTestCaseIterationsPreProcessor(mockDatasetRepository);

        // When
        ExecutableComposedTestCase actual = sut.apply(testCase);

        // Then
        ExecutableComposedScenario expected = ExecutableComposedScenario.builder()
            .withComposedSteps(asList(
                ExecutableComposedStep.builder()
                    .withName("Should generate 2 iterations for letter a and b")
                    .withStrategy(new Strategy(DataSetIterationsStrategy.TYPE, emptyMap()))
                    .withSteps(Arrays.asList(
                        ExecutableComposedStep.builder()
                            .withName("Should generate 2 iterations for letter a and b - dataset iteration 1")
                            .withImplementation(Optional.of(
                                new StepImplementation("success", null, emptyMap(), singletonMap("output_1", "**letter**"))
                            ))
                            .withDataset(singletonMap("letter", "a"))
                            .build(),
                        ExecutableComposedStep.builder()
                            .withName("Should generate 2 iterations for letter a and b - dataset iteration 2")
                            .withImplementation(Optional.of(
                                new StepImplementation("success", null, emptyMap(), singletonMap("output_2", "**letter**"))
                            ))
                            .withDataset(singletonMap("letter", "b"))
                            .build()
                    ))
                    .build(),
                ExecutableComposedStep.builder()
                    .withName("Should generate 2 iterations having previous indexed output and indexing 2 other outputs")
                    .withStrategy(new Strategy(DataSetIterationsStrategy.TYPE, emptyMap()))
                    .withSteps(Arrays.asList(
                        ExecutableComposedStep.builder()
                            .withName("Should generate 2 iterations having previous indexed output and indexing 2 other outputs - dataset iteration 1")
                            .withImplementation(Optional.of(
                                new StepImplementation("success", null, singletonMap("taskInput", singletonMap("${#notFromIteration}", "fake")), singletonMap("otherOutput_1", "**letter**"))
                            ))
                            .withDataset(singletonMap("letter", "a"))
                            .build(),
                        ExecutableComposedStep.builder()
                            .withName("Should generate 2 iterations having previous indexed output and indexing 2 other outputs - dataset iteration 2")
                            .withImplementation(Optional.of(
                                new StepImplementation("success", null, singletonMap("taskInput", singletonMap("${#notFromIteration}", "fake")), singletonMap("otherOutput_2", "**letter**"))
                            ))
                            .withDataset(singletonMap("letter", "b"))
                            .build()
                    ))
                    .build()
                )
            )
            .build();

        assertThat(actual.composedScenario.composedSteps.get(0)).isEqualTo(expected.composedSteps.get(0));
        assertThat(actual.composedScenario.composedSteps.get(1)).isEqualTo(expected.composedSteps.get(1));

    }

    @Test
    public void should_generate_iterations_for_simple_task_input_taking_previous_indexed_outputs() {

        // Given
        List<Map<String, String>> multipleValues = asList(
            Maps.of("letter", "a"),
            Maps.of("letter", "b")
        );
        stubDatasetRepository(null, multipleValues);

        ExecutableComposedTestCase testCase = new ExecutableComposedTestCase(
            metadata,
            ExecutableComposedScenario.builder()
                .withComposedSteps(asList(
                    ExecutableComposedStep.builder()
                        .withName("Should generate 2 iterations for letter a and b")
                        .withImplementation(Optional.of(
                            new StepImplementation("success", null, emptyMap(), singletonMap("output", "**letter**"))
                        ))
                        .withDataset(singletonMap("letter", ""))
                        .build(),
                    ExecutableComposedStep.builder()
                        .withName("Should generate 2 iterations having previous indexed output")
                        .withImplementation(Optional.of(
                            new StepImplementation("success", null, singletonMap("taskInput", "${#output}"), emptyMap())
                        ))
                        .build()
                    )
                )
                .build(),
            singletonMap("letter", "")
        );

        sut = new ComposedTestCaseIterationsPreProcessor(mockDatasetRepository);

        // When
        ExecutableComposedTestCase actual = sut.apply(testCase);

        // Then
        ExecutableComposedScenario expected = ExecutableComposedScenario.builder()
            .withComposedSteps(asList(
                ExecutableComposedStep.builder()
                    .withName("Should generate 2 iterations for letter a and b")
                    .withStrategy(new Strategy(DataSetIterationsStrategy.TYPE, emptyMap()))
                    .withSteps(Arrays.asList(
                        ExecutableComposedStep.builder()
                            .withName("Should generate 2 iterations for letter a and b - dataset iteration 1")
                            .withImplementation(Optional.of(
                                new StepImplementation("success", null, emptyMap(), singletonMap("output_1", "**letter**"))
                            ))
                            .withDataset(singletonMap("letter", "a"))
                            .build(),
                        ExecutableComposedStep.builder()
                            .withName("Should generate 2 iterations for letter a and b - dataset iteration 2")
                            .withImplementation(Optional.of(
                                new StepImplementation("success", null, emptyMap(), singletonMap("output_2", "**letter**"))
                            ))
                            .withDataset(singletonMap("letter", "b"))
                            .build()
                    ))
                    .build(),
                ExecutableComposedStep.builder()
                    .withName("Should generate 2 iterations having previous indexed output")
                    .withStrategy(new Strategy(DataSetIterationsStrategy.TYPE, emptyMap()))
                    .withSteps(Arrays.asList(
                        ExecutableComposedStep.builder()
                            .withName("Should generate 2 iterations having previous indexed output - dataset iteration 1")
                            .withImplementation(Optional.of(
                                new StepImplementation("success", null, singletonMap("taskInput", "${#output_1}"), emptyMap())
                            ))
                            .build(),
                        ExecutableComposedStep.builder()
                            .withName("Should generate 2 iterations having previous indexed output - dataset iteration 2")
                            .withImplementation(Optional.of(
                                new StepImplementation("success", null, singletonMap("taskInput", "${#output_2}"), emptyMap())
                            ))
                            .build()
                    ))
                    .build()
                )
            )
            .build();

        assertThat(actual.composedScenario.composedSteps.get(0)).isEqualTo(expected.composedSteps.get(0));
        assertThat(actual.composedScenario.composedSteps.get(1)).isEqualTo(expected.composedSteps.get(1));

    }

    @Test
    public void should_index_iterations_outputs_when_inside_a_parent_step() {

        // Given
        List<Map<String, String>> multipleValues = asList(
            Maps.of("letter", "a"),
            Maps.of("letter", "b")
        );
        stubDatasetRepository(null, multipleValues);

        ExecutableComposedTestCase testCase = new ExecutableComposedTestCase(
            metadata,
            ExecutableComposedScenario.builder()
                .withComposedSteps(singletonList(
                    ExecutableComposedStep.builder()
                        .withName("Success - should uppercase & assert each char")
                        .withSteps(asList(
                            ExecutableComposedStep.builder()
                                .withName("Success - should uppercase a char")
                                .withImplementation(Optional.of(
                                    new StepImplementation("success", null, emptyMap(), singletonMap("result", "${new String(\"**letter**\").toUpperCase()"))
                                ))
                                .withDataset(singletonMap("letter", ""))
                                .build(),
                            ExecutableComposedStep.builder()
                                .withName("Should assert an uppercased char")
                                .withImplementation(Optional.of(
                                    new StepImplementation("assert", null, Maps.of("${#result}", "${new String(\"**letter**\").toUpperCase()"), emptyMap())
                                ))
                                .build()
                        ))
                        .withDataset(singletonMap("letter", ""))
                        .build()
                    )
                )
                .build(),
            singletonMap("letter", "")
        );

        sut = new ComposedTestCaseIterationsPreProcessor(mockDatasetRepository);

        // When
        ExecutableComposedTestCase actual = sut.apply(testCase);

        // Then

        ExecutableComposedScenario expected = ExecutableComposedScenario.builder()
            .withComposedSteps(singletonList(
                ExecutableComposedStep.builder()
                    .withName("Success - should uppercase & assert each char")
                    .withStrategy(new Strategy(DataSetIterationsStrategy.TYPE, emptyMap()))
                    .withSteps(asList(
                        ExecutableComposedStep.builder()
                            .withName("Success - should uppercase & assert each char - dataset iteration 1")
                            .withSteps(asList(
                                ExecutableComposedStep.builder()
                                    .withName("Success - should uppercase a char")
                                    .withImplementation(Optional.of(new StepImplementation("success", null, emptyMap(), singletonMap("result_1", "${new String(\"**letter**\").toUpperCase()"))))
                                    .withDataset(singletonMap("letter", ""))
                                    .build(),
                                ExecutableComposedStep.builder()
                                    .withName("Should assert an uppercased char")
                                    .withImplementation(Optional.of(
                                        new StepImplementation("assert", null, singletonMap("${#result_1}", "${new String(\"**letter**\").toUpperCase()"), emptyMap())
                                    ))
                                    .build()
                            ))
                            .withDataset(singletonMap("letter", "a"))
                            .build(),
                        ExecutableComposedStep.builder()
                            .withName("Success - should uppercase & assert each char - dataset iteration 2")
                            .withSteps(asList(
                                ExecutableComposedStep.builder()
                                    .withName("Success - should uppercase a char")
                                    .withImplementation(Optional.of(new StepImplementation("success", null, emptyMap(), singletonMap("result_2", "${new String(\"**letter**\").toUpperCase()"))))
                                    .withDataset(singletonMap("letter", ""))
                                    .build(),
                                ExecutableComposedStep.builder()
                                    .withName("Should assert an uppercased char")
                                    .withImplementation(Optional.of(
                                        new StepImplementation("assert", null, singletonMap("${#result_2}", "${new String(\"**letter**\").toUpperCase()"), emptyMap())
                                    ))
                                    .build()
                            ))
                            .withDataset(singletonMap("letter", "b"))
                            .build()
                    ))
                    .build()
                )
            )
            .build();

        assertThat(actual.composedScenario).isEqualTo(expected);

    }

    @Test
    public void should_index_iteration_dataset_using_previous_indexed_outputs() {

        // Given
        List<Map<String, String>> multipleValues = asList(
            Maps.of("letter", "a"),
            Maps.of("letter", "b")
        );
        stubDatasetRepository(null, multipleValues);

        ExecutableComposedTestCase testCase = new ExecutableComposedTestCase(
            metadata,
            ExecutableComposedScenario.builder()
                .withComposedSteps(asList(
                    ExecutableComposedStep.builder()
                        .withName("Success - should uppercase each char")
                        .withImplementation(Optional.of(new StepImplementation("success", null, emptyMap(), singletonMap("result", "${new String(\"**letter**\").toUpperCase()"))))
                        .withDataset(singletonMap("letter", ""))
                        .build(),
                    ExecutableComposedStep.builder()
                        .withName("Context put - should create one context entry for each uppercased char")
                        .withImplementation(Optional.of(
                            new StepImplementation("context-put", null, singletonMap("entries", singletonMap("**myParam**", "fake")), emptyMap())
                        ))
                        .withDataset(singletonMap("myParam", "${#result}"))
                        .build()
                    )
                )
                .build(),
            singletonMap("letter", "")
        );

        sut = new ComposedTestCaseIterationsPreProcessor(mockDatasetRepository);

        // When
        ExecutableComposedTestCase actual = sut.apply(testCase);

        // Then
        ExecutableComposedScenario expected = ExecutableComposedScenario.builder()
            .withComposedSteps(asList(
                ExecutableComposedStep.builder()
                    .withName("Success - should uppercase each char")
                    .withStrategy(new Strategy(DataSetIterationsStrategy.TYPE, emptyMap()))
                    .withSteps(Arrays.asList(
                        ExecutableComposedStep.builder()
                            .withName("Success - should uppercase each char - dataset iteration 1")
                            .withImplementation(Optional.of(
                                new StepImplementation("success", null, emptyMap(), singletonMap("result_1", "${new String(\"**letter**\").toUpperCase()"))
                            ))
                            .withDataset(singletonMap("letter", "a"))
                            .build(),
                        ExecutableComposedStep.builder()
                            .withName("Success - should uppercase each char - dataset iteration 2")
                            .withImplementation(Optional.of(
                                new StepImplementation("success", null, emptyMap(), singletonMap("result_2", "${new String(\"**letter**\").toUpperCase()"))
                            ))
                            .withDataset(singletonMap("letter", "b"))
                            .build()
                    ))
                    .build(),
                ExecutableComposedStep.builder()
                    .withName("Context put - should create one context entry for each uppercased char")
                    .withStrategy(new Strategy(DataSetIterationsStrategy.TYPE, emptyMap()))
                    .withSteps(Arrays.asList(
                        ExecutableComposedStep.builder()
                            .withName("Context put - should create one context entry for each uppercased char - dataset iteration 1")
                            .withImplementation(Optional.of(
                                new StepImplementation("context-put", null, singletonMap("entries", singletonMap("**myParam**", "fake")), emptyMap())
                            ))
                            .withDataset(singletonMap("myParam", "${#result_1}"))
                            .build(),
                        ExecutableComposedStep.builder()
                            .withName("Context put - should create one context entry for each uppercased char - dataset iteration 2")
                            .withImplementation(Optional.of(
                                new StepImplementation("context-put", null, singletonMap("entries", singletonMap("**myParam**", "fake")), emptyMap())
                            ))
                            .withDataset(singletonMap("myParam", "${#result_2}"))
                            .build()
                    ))
                    .build()
                )
            )
            .build();

        assertThat(actual.composedScenario.composedSteps.get(0)).isEqualTo(expected.composedSteps.get(0));
        assertThat(actual.composedScenario.composedSteps.get(1)).isEqualTo(expected.composedSteps.get(1));

    }
}
