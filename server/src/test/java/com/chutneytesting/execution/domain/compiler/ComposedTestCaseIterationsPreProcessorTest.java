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
import com.chutneytesting.design.domain.scenario.TestCaseMetadataImpl;
import com.chutneytesting.design.domain.scenario.compose.Strategy;
import com.chutneytesting.engine.domain.execution.strategies.DataSetIterationsStrategy;
import com.chutneytesting.engine.domain.execution.strategies.DefaultStepExecutionStrategy;
import com.chutneytesting.engine.domain.execution.strategies.RetryWithTimeOutStrategy;
import com.chutneytesting.engine.domain.execution.strategies.SoftAssertStrategy;
import com.chutneytesting.execution.domain.ExecutionRequest;
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
    public void should_add_dataset_unique_values_to_testcase_dataset_when_matched() {
        // Given
        Map<String, String> uniqueValues = Maps.of(
            "testcase param key", "dataset param value",
            "dataset key", "dataset value"
        );
        stubDatasetRepository(uniqueValues, null);

        Map<String, String> computedParameters = Maps.of(
            "testcase param key", "testcase param value",
            "testcase key", "testcase value"
        );

        ExecutableComposedTestCase testCase = new ExecutableComposedTestCase(
            "1",
            TestCaseMetadataImpl.builder().withDatasetId("fakeId").build(),
            ExecutableComposedScenario.builder().build(),
            computedParameters
        );

        // When
        ComposedTestCaseIterationsPreProcessor sut = new ComposedTestCaseIterationsPreProcessor(mockDatasetRepository);
        ExecutableComposedTestCase processedTestCase = sut.apply(
            new ExecutionRequest(testCase, "env", "user")
        );

        // Then
        assertThat(processedTestCase.computedParameters()).containsOnly(
            entry("testcase key", "testcase value"),
            entry("testcase param key", "dataset param value")
        );
    }

    @Test
    public void should_create_step_iterations_when_step_has_multiple_values_dataset_matching_a_parameter() {
        // Given
        List<Map<String, String>> multipleValues = asList(
            Maps.of("testcase key for dataset", "dataset first value"),
            Maps.of("testcase key for dataset", "dataset second value"),
            Maps.of("testcase key for dataset", "dataset third value")
        );
        stubDatasetRepository(null, multipleValues);

        Map<String, String> computedParameters = Maps.of(
            "testcase key for dataset", "testcase default value for dataset",
            "testcase key no dataset", "testcase value",
            "another param key", "another testcase value"
        );

        ExecutableComposedTestCase testCase = new ExecutableComposedTestCase(
            "1",
            TestCaseMetadataImpl.builder().withDatasetId("fakeId").build(),
            ExecutableComposedScenario.builder()
                .withComposedSteps(
                    asList(
                        ExecutableComposedStep.builder()
                            .withName("a step with no valued parameter")
                            .withDataset(
                                Maps.of(
                                    "testcase key for dataset", "",
                                    "step 1 param", "value 1"
                                )
                            )
                            .build(),
                        ExecutableComposedStep.builder()
                            .withName("a step with no dataset matched parameter")
                            .withDataset(
                                Maps.of("step param", "value 2")
                            )
                            .build(),
                        ExecutableComposedStep.builder()
                            .withName("a step with ref parameter")
                            .withDataset(
                                Maps.of(
                                    "ref step param", "value with ref **testcase key for dataset**",
                                    "step 3 alias", "**another param key**"
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
        ExecutableComposedTestCase processedTestCase = sut.apply(
            new ExecutionRequest(testCase, "env", "user")
        );

        // Then
        assertThat(processedTestCase.computedParameters).containsOnly(
            entry("testcase key no dataset", "testcase value"),
            entry("another param key", "another testcase value")
        );
        assertThat(processedTestCase.composedScenario.composedSteps).hasSize(3);

        ExecutableComposedStep fs = processedTestCase.composedScenario.composedSteps.get(0);
        assertThat(fs.name).isEqualTo("a step with no valued parameter");
        assertThat(fs.dataset).containsOnly(
            entry("testcase key no dataset", ""),
            entry("another param key", "")
        );
        assertThat(fs.steps).hasSize(3);
        assertThat(fs.steps.get(0).name).isEqualTo("a step with no valued parameter - dataset iteration 1");
        assertThat(fs.steps.get(0).dataset).containsOnly(
            entry("testcase key for dataset", "dataset first value"),
            entry("step 1 param", "value 1")
        );
        assertThat(fs.steps.get(1).name).isEqualTo("a step with no valued parameter - dataset iteration 2");
        assertThat(fs.steps.get(1).dataset).containsOnly(
            entry("testcase key for dataset", "dataset second value"),
            entry("step 1 param", "value 1")
        );
        assertThat(fs.steps.get(2).name).isEqualTo("a step with no valued parameter - dataset iteration 3");
        assertThat(fs.steps.get(2).dataset).containsOnly(
            entry("testcase key for dataset", "dataset third value"),
            entry("step 1 param", "value 1")
        );

        fs = processedTestCase.composedScenario.composedSteps.get(1);
        assertThat(fs.name).isEqualTo("a step with no dataset matched parameter");
        assertThat(fs.dataset).containsOnly(
            entry("step param", "value 2")
        );
        assertThat(fs.steps).hasSize(0);

        fs = processedTestCase.composedScenario.composedSteps.get(2);
        assertThat(fs.name).isEqualTo("a step with ref parameter");
        assertThat(fs.dataset).containsOnly(
            entry("another param key", ""),
            entry("testcase key no dataset", "")
        );
        assertThat(fs.steps).hasSize(3);
        assertThat(fs.steps.get(0).name).isEqualTo("a step with ref parameter - dataset iteration 1");
        assertThat(fs.steps.get(0).dataset).containsOnly(
            entry("ref step param", "value with ref dataset first value"),
            entry("step 3 alias", "**another param key**")
        );
        assertThat(fs.steps.get(1).name).isEqualTo("a step with ref parameter - dataset iteration 2");
        assertThat(fs.steps.get(1).dataset).containsOnly(
            entry("ref step param", "value with ref dataset second value"),
            entry("step 3 alias", "**another param key**")
        );
        assertThat(fs.steps.get(2).name).isEqualTo("a step with ref parameter - dataset iteration 3");
        assertThat(fs.steps.get(2).dataset).containsOnly(
            entry("ref step param", "value with ref dataset third value"),
            entry("step 3 alias", "**another param key**")
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
            "1",
            TestCaseMetadataImpl.builder().withDatasetId("fakeId").build(),
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
        ExecutableComposedTestCase processedTestCase = sut.apply(
            new ExecutionRequest(testCase, "env", "user")
        );

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
            "1",
            TestCaseMetadataImpl.builder().withDatasetId("fakeId").build(),
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
        ExecutableComposedTestCase processedTestCase = sut.apply(
            new ExecutionRequest(testCase, "env", "user")
        );

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
            "1",
            TestCaseMetadataImpl.builder().withDatasetId("fakeId").build(),
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
        ExecutableComposedTestCase processedTestCase = sut.apply(
            new ExecutionRequest(testCase, "env", "user")
        );

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
            "fakeId",
            TestCaseMetadataImpl.builder().withDatasetId("fakeId").build(),
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
    public void should_generate_iterations_having_both_previous_indexed_outputs_and_indexing_new_outputs() {

        // Given
        List<Map<String, String>> multipleValues = asList(
            Maps.of("letter", "a"),
            Maps.of("letter", "b")
        );
        stubDatasetRepository(null, multipleValues);

        ExecutableComposedTestCase testCase = new ExecutableComposedTestCase(
            "fakeId",
            TestCaseMetadataImpl.builder().withDatasetId("fakeId").build(),
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
            "fakeId",
            TestCaseMetadataImpl.builder().withDatasetId("fakeId").build(),
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
            "fakeId",
            TestCaseMetadataImpl.builder().withDatasetId("fakeId").build(),
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

    // A test with 2 multivalues dataset
    // a test with a step using 2 previous indexed values and 3 multivalues in input, how many iteration should we have ? 2, 3, 6 combine ? Fuck ! //


    @Test
    public void should_index_iterations_outputs_when_inside_a_parent_step() {

        // Given
        List<Map<String, String>> multipleValues = asList(
            Maps.of("letter", "a"),
            Maps.of("letter", "b")
        );
        stubDatasetRepository(null, multipleValues);

        ExecutableComposedTestCase testCase = new ExecutableComposedTestCase(
            "fakeId",
            TestCaseMetadataImpl.builder().withDatasetId("fakeId").build(),
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
            "fakeId",
            TestCaseMetadataImpl.builder().withDatasetId("fakeId").build(),
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
