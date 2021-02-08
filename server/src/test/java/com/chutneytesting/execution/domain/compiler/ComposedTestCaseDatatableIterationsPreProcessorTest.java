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
import org.apache.groovy.util.Maps;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class ComposedTestCaseDatatableIterationsPreProcessorTest {

    ComposedTestCaseDatatableIterationsPreProcessor sut;
    private DataSetRepository mockDatasetRepository;

    private final TestCaseMetadata metadata = TestCaseMetadataImpl.builder().withDatasetId("fakeId").build();
    private final ExecutableComposedStep stepGenerating2Iterations = ExecutableComposedStep.builder()
        .withName("Should generate 2 iterations for letter A and B")
        .withImplementation(Optional.of(
            new StepImplementation("task", null, emptyMap(), singletonMap("output", "**letter**"))))
        .withExecutionParameters(singletonMap("letter", ""))
        .build();

    @BeforeEach
    public void setUp() {
        mockDatasetRepository = mock(DataSetRepository.class);
    }

    private void stubDatasetRepository(Map<String, String> constants, List<Map<String, String>> datatable) {
        when(mockDatasetRepository.findById(any())).thenReturn(
            DataSet.builder()
                .withConstants(ofNullable(constants).orElse(emptyMap()))
                .withDatatable(ofNullable(datatable).orElse(emptyList()))
                .build()
        );
    }

    @Test
    public void should_override_keys_matching_external_dataset_constants_to_testcase_execution_parameters() {
        // Given
        Map<String, String> constants = Maps.of(
            "aKey", "usedInTestCase",
            "anotherKey", "notUsedInTestCase"
        );
        stubDatasetRepository(constants, null);

        Map<String, String> executionParameters = Maps.of(
            "aKey", "value will be override",
            "localKey", "will be kept as is"
        );

        ExecutableComposedTestCase testCase = new ExecutableComposedTestCase(
            metadata,
            ExecutableComposedScenario.builder().build(),
            executionParameters
        );

        // When
        ComposedTestCaseDatatableIterationsPreProcessor sut = new ComposedTestCaseDatatableIterationsPreProcessor(mockDatasetRepository);
        ExecutableComposedTestCase processedTestCase = sut.apply(testCase);

        // Then
        assertThat(processedTestCase.executionParameters()).containsOnly(
            entry("aKey", "usedInTestCase"),
            entry("localKey", "will be kept as is")
        );
    }

    @Test
    public void should_remove_keys_matching_external_datatable_from_testcase_execution_parameters() {
        // Given
        List<Map<String, String>> datatable = asList(
            Maps.of("aKey", "aValue"),
            Maps.of("aKey", "anotherValue")
        );
        stubDatasetRepository(null, datatable);

        Map<String, String> executionParameters = Maps.of(
            "aKey", "testcase default value",
            "anotherKey", "testcase value"
        );

        ExecutableComposedTestCase testCase = new ExecutableComposedTestCase(
            metadata,
            ExecutableComposedScenario.builder()
                .withComposedSteps(singletonList(ExecutableComposedStep.builder().build()))
                .build(),
            executionParameters
        );

        // When
        sut = new ComposedTestCaseDatatableIterationsPreProcessor(mockDatasetRepository);
        ExecutableComposedTestCase processedTestCase = sut.apply(testCase);

        // Then
        assertThat(processedTestCase.executionParameters).containsOnly(
            entry("anotherKey", "testcase value")
        );
    }

    @Test
    public void should_create_iterations_when_step_uses_external_dataset_multivalues() {
        // Given
        List<Map<String, String>> datatable = asList(
            Maps.of("aKey", "aValue"),
            Maps.of("aKey", "anotherValue")
        );
        stubDatasetRepository(null, datatable);

        ExecutableComposedTestCase testCase = new ExecutableComposedTestCase(
            metadata,
            ExecutableComposedScenario.builder()
                .withComposedSteps(
                    singletonList(
                        ExecutableComposedStep.builder()
                            .withName("Should create 2 iterations for aKey")
                            .withExecutionParameters(singletonMap("aKey", ""))
                            .build()
                    )
                )
                .build(),
            singletonMap("aKey", "")
        );

        // When
        sut = new ComposedTestCaseDatatableIterationsPreProcessor(mockDatasetRepository);
        ExecutableComposedTestCase processedTestCase = sut.apply(testCase);

        // Then
        ExecutableComposedStep iterableStep = processedTestCase.composedScenario.composedSteps.get(0);
        assertThat(iterableStep.steps).hasSize(2);

        ExecutableComposedStep firstIteration = iterableStep.steps.get(0);
        assertThat(firstIteration.name).isEqualTo("Should create 2 iterations for aKey - datatable iteration 1");
        assertThat(firstIteration.executionParameters).containsOnly(
            entry("aKey", "aValue")
        );

        ExecutableComposedStep secondIteration = iterableStep.steps.get(1);
        assertThat(secondIteration.name).isEqualTo("Should create 2 iterations for aKey - datatable iteration 2");
        assertThat(secondIteration.executionParameters).containsOnly(
            entry("aKey", "anotherValue")
        );

    }

    @Test
    // TODO - what is this rule ? why is that ? TestCase dataset takes precedence over local step values ? But why is it done here ?
    public void should_override_iterations_parent_step_execution_parameters() {
        // Given
        List<Map<String, String>> datatable = asList(
            Maps.of("aKey", "aValue"),
            Maps.of("aKey", "anotherValue")
        );
        stubDatasetRepository(null, datatable);

        Map<String, String> executionParameters = Maps.of(
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
                            .withExecutionParameters(
                                Maps.of(
                                    "aKey", "", // needed for generating iterations
                                    "anotherKey", "step value" // will be override
                                )
                            )
                            .build()
                    )
                )
                .build(),
            executionParameters
        );

        // When
        sut = new ComposedTestCaseDatatableIterationsPreProcessor(mockDatasetRepository);
        ExecutableComposedTestCase processedTestCase = sut.apply(testCase);

        // Then
        ExecutableComposedStep parentIterableStep = processedTestCase.composedScenario.composedSteps.get(0);
        assertThat(parentIterableStep.executionParameters).containsOnly(entry("anotherKey", "")); // TODO - moreover, why is it empty and not using the testcase dataset value ?

    }

    @Test
    public void should_keep_local_dataset_values_when_creating_iterations() {
        // Given
        List<Map<String, String>> datatable = asList(
            Maps.of("aKey", "aValue"),
            Maps.of("aKey", "anotherValue")
        );
        stubDatasetRepository(null, datatable);

        ExecutableComposedTestCase testCase = new ExecutableComposedTestCase(
            metadata,
            ExecutableComposedScenario.builder()
                .withComposedSteps(
                    singletonList(
                        ExecutableComposedStep.builder()
                            .withName("Should create 2 iterations for aKey")
                            .withExecutionParameters(
                                Maps.of(
                                    "aKey", "",
                                    "aLocalParam", "isKeptForEachIteration"
                                )
                            )
                            .build()
                    )
                )
                .build(),
            singletonMap("aKey", "")
        );

        // When
        sut = new ComposedTestCaseDatatableIterationsPreProcessor(mockDatasetRepository);
        ExecutableComposedTestCase processedTestCase = sut.apply(testCase);

        // Then
        ExecutableComposedStep iterableStep = processedTestCase.composedScenario.composedSteps.get(0);
        assertThat(iterableStep.steps).hasSize(2);

        ExecutableComposedStep firstIteration = iterableStep.steps.get(0);
        assertThat(firstIteration.executionParameters).containsOnly(
            entry("aKey", "aValue"),
            entry("aLocalParam", "isKeptForEachIteration")
        );

        ExecutableComposedStep secondIteration = iterableStep.steps.get(1);
        assertThat(secondIteration.executionParameters).containsOnly(
            entry("aKey", "anotherValue"),
            entry("aLocalParam", "isKeptForEachIteration")
        );

    }

    @Test
    public void should_replace_iteration_dataset_value_when_referring_to_external_dataset_multivalue() {
        // Given
        List<Map<String, String>> datatable = asList(
            Maps.of("aKey", "aValue"),
            Maps.of("aKey", "anotherValue")
        );

        stubDatasetRepository(null, datatable);

        ExecutableComposedTestCase testCase = new ExecutableComposedTestCase(
            metadata,
            ExecutableComposedScenario.builder()
                .withComposedSteps(
                    singletonList(
                        ExecutableComposedStep.builder()
                            .withName("a step with ref parameter")
                            .withExecutionParameters(
                                singletonMap("aLocalParam", "refers to the external dataset key **aKey**")
                            )
                            .build()
                    )
                )
                .build(),
            singletonMap("aKey", "")
        );

        // When
        sut = new ComposedTestCaseDatatableIterationsPreProcessor(mockDatasetRepository);
        ExecutableComposedTestCase processedTestCase = sut.apply(testCase);

        // Then
        ExecutableComposedStep parentIterableStep = processedTestCase.composedScenario.composedSteps.get(0);

        assertThat(parentIterableStep.steps.get(0).executionParameters).containsOnly(
            entry("aLocalParam", "refers to the external dataset key aValue")
        );

        assertThat(parentIterableStep.steps.get(1).executionParameters).containsOnly(
            entry("aLocalParam", "refers to the external dataset key anotherValue")
        );
    }

    @Test
    public void iterations_parent_step_should_have_specific_strategy() {
        // Given
        List<Map<String, String>> datatable = asList(
            Maps.of("aKey", "first value"),
            Maps.of("aKey", "second value")
        );
        stubDatasetRepository(null, datatable);

        ExecutableComposedTestCase testCase = new ExecutableComposedTestCase(
            metadata,
            ExecutableComposedScenario.builder()
                .withComposedSteps(
                    singletonList(
                        ExecutableComposedStep.builder()
                            .withName("a step with no valued parameter")
                            .withExecutionParameters(singletonMap("aKey", ""))
                            .build()
                    )
                )
                .build(),
            singletonMap("aKey", "")
        );

        // When
        sut = new ComposedTestCaseDatatableIterationsPreProcessor(mockDatasetRepository);
        ExecutableComposedTestCase processedTestCase = sut.apply(testCase);

        // Then
        assertThat(processedTestCase.composedScenario.composedSteps.get(0).strategy).isEqualTo(
            new Strategy(DataSetIterationsStrategy.TYPE, emptyMap())
        );
    }

    @ParameterizedTest
    @MethodSource("strategyDefinitions")
    public void iterations_should_inherit_their_strategy_from_parent_strategy_definition(Strategy strategyDefinition) {
        // Given
        List<Map<String, String>> datatable = asList(
            Maps.of("aKey", "first value"),
            Maps.of("aKey", "second value")
        );
        stubDatasetRepository(null, datatable);

        ExecutableComposedTestCase testCase = new ExecutableComposedTestCase(
            metadata,
            ExecutableComposedScenario.builder()
                .withComposedSteps(
                    singletonList(
                        ExecutableComposedStep.builder()
                            .withStrategy(strategyDefinition)
                            .withName("a step with no valued parameter")
                            .withExecutionParameters(singletonMap("aKey", ""))
                            .build()
                    )
                )
                .build(),
            singletonMap("aKey", "")
        );

        // When
        sut = new ComposedTestCaseDatatableIterationsPreProcessor(mockDatasetRepository);
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
    public void should_create_iterations_with_distinct_datatable_values() {
        // Given
        List<Map<String, String>> datatable = asList(
            Maps.of("aKey", "value A"),
            Maps.of("aKey", "value B"),
            Maps.of("aKey", "value A")
        );
        stubDatasetRepository(null, datatable);

        ExecutableComposedTestCase testCase = new ExecutableComposedTestCase(
            metadata,
            ExecutableComposedScenario.builder()
                .withComposedSteps(
                    singletonList(
                        ExecutableComposedStep.builder()
                            .withName("Should generate 2 iterations for values A and B only")
                            .withExecutionParameters(singletonMap("aKey", ""))
                            .build()
                    )
                )
                .build(),
            singletonMap("aKey", "")
        );

        // When
        sut = new ComposedTestCaseDatatableIterationsPreProcessor(mockDatasetRepository);
        ExecutableComposedTestCase processedTestCase = sut.apply(testCase);

        // Then

        ExecutableComposedStep parentIterableStep = processedTestCase.composedScenario.composedSteps.get(0);
        assertThat(parentIterableStep.steps).hasSize(2);
        assertThat(parentIterableStep.executionParameters).isEmpty();

        ExecutableComposedStep firstIteration = parentIterableStep.steps.get(0);
        assertThat(firstIteration.executionParameters).containsOnly(
            entry("aKey", "value A")
        );
        ExecutableComposedStep secondIteration = parentIterableStep.steps.get(1);
        assertThat(secondIteration.executionParameters).containsOnly(
            entry("aKey", "value B")
        );
    }

    @Test
    public void should_index_iterations_outputs_when_using_external_multivalues_dataset() {

        // Given
        List<Map<String, String>> datatable = asList(
            Maps.of("letter", "A"),
            Maps.of("letter", "B")
        );
        stubDatasetRepository(null, datatable);

        ExecutableComposedTestCase testCase = new ExecutableComposedTestCase(
            metadata,
            ExecutableComposedScenario.builder()
                .withComposedSteps(singletonList(
                    ExecutableComposedStep.builder()
                        .withName("Should generate 2 iterations using external multivalues dataset")
                        .withImplementation(Optional.of(
                            new StepImplementation("task", null, emptyMap(), singletonMap("output", "**letter**"))))
                        .withExecutionParameters(singletonMap("letter", ""))
                        .build()
                    )
                )
                .build(),
            singletonMap("letter", "")
        );

        // When
        sut = new ComposedTestCaseDatatableIterationsPreProcessor(mockDatasetRepository);
        ExecutableComposedTestCase actual = sut.apply(testCase);

        // Then
        ExecutableComposedStep expected = ExecutableComposedStep.builder()
            .withName("Should generate 2 iterations using external multivalues dataset")
            .withStrategy(new Strategy(DataSetIterationsStrategy.TYPE, emptyMap()))
            .withSteps(asList(
                ExecutableComposedStep.builder()
                    .withName("Should generate 2 iterations using external multivalues dataset - datatable iteration 1")
                    .withImplementation(Optional.of(
                        new StepImplementation("task", null, emptyMap(), singletonMap("output_1", "**letter**"))
                    ))
                    .withExecutionParameters(singletonMap("letter", "A"))
                    .build(),
                ExecutableComposedStep.builder()
                    .withName("Should generate 2 iterations using external multivalues dataset - datatable iteration 2")
                    .withImplementation(Optional.of(
                        new StepImplementation("task", null, emptyMap(), singletonMap("output_2", "**letter**"))
                    ))
                    .withExecutionParameters(singletonMap("letter", "B"))
                    .build()
            ))
            .build();

        assertThat(actual.composedScenario.composedSteps.get(0)).isEqualTo(expected);

    }

    @Test
    public void should_still_work_when_there_are_null_values() {

        // Given
        List<Map<String, String>> datatable = asList(
            Maps.of("letter", "A"),
            Maps.of("letter", "B")
        );
        stubDatasetRepository(null, datatable);

        ExecutableComposedTestCase testCase = new ExecutableComposedTestCase(
            metadata,
            ExecutableComposedScenario.builder()
                .withComposedSteps(asList(
                    stepGenerating2Iterations,
                    ExecutableComposedStep.builder() // the step under test
                        .withName("Should generate 2 iterations using output_1 and output_2 previous outputs")
                        .withImplementation(Optional.of(
                            new StepImplementation("task", null, Maps.of("keyWithNullValue", null, "taskInput", "#output"), emptyMap())))
                        .build(),
                    ExecutableComposedStep.builder()
                        .withName("Should generate 2 iterations using previous outputs in their output")
                        .withImplementation(Optional.of(
                            new StepImplementation("task", null, emptyMap(), Maps.of("keyWithNullValue", null, "key_to_index", "#output"))))
                        .build()
                    )
                )
                .build(),
            singletonMap("letter", "")
        );

        sut = new ComposedTestCaseDatatableIterationsPreProcessor(mockDatasetRepository);

        // When
        ExecutableComposedTestCase actual = sut.apply(testCase);

        // Then
        ExecutableComposedStep expected_1 = ExecutableComposedStep.builder()
            .withName("Should generate 2 iterations using output_1 and output_2 previous outputs")
            .withStrategy(new Strategy(DataSetIterationsStrategy.TYPE, emptyMap()))
            .withSteps(Arrays.asList(
                ExecutableComposedStep.builder()
                    .withName("Should generate 2 iterations using output_1 and output_2 previous outputs - datatable iteration 1")
                    .withImplementation(Optional.of(
                        new StepImplementation("task", null, Maps.of("keyWithNullValue", null, "taskInput", "#output_1"), emptyMap())
                    ))
                    .build(), ExecutableComposedStep.builder()
                    .withName("Should generate 2 iterations using output_1 and output_2 previous outputs - datatable iteration 2")
                    .withImplementation(Optional.of(
                        new StepImplementation("task", null, Maps.of("keyWithNullValue", null, "taskInput", "#output_2"), emptyMap())
                    ))
                    .build()
            ))
            .build();

        assertThat(actual.composedScenario.composedSteps.get(1)).isEqualTo(expected_1);

        ExecutableComposedStep expected_2 = ExecutableComposedStep.builder()
            .withName("Should generate 2 iterations using previous outputs in their output")
            .withStrategy(new Strategy(DataSetIterationsStrategy.TYPE, emptyMap()))
            .withSteps(Arrays.asList(
                ExecutableComposedStep.builder()
                    .withName("Should generate 2 iterations using previous outputs in their output - datatable iteration 1")
                    .withImplementation(Optional.of(
                        new StepImplementation("task", null, emptyMap(), Maps.of("keyWithNullValue_1", null, "key_to_index_1", "#output_1"))
                    ))
                    .build(), ExecutableComposedStep.builder()
                    .withName("Should generate 2 iterations using previous outputs in their output - datatable iteration 2")
                    .withImplementation(Optional.of(
                        new StepImplementation("task", null, emptyMap(), Maps.of("keyWithNullValue_2", null, "key_to_index_2", "#output_2"))
                    ))
                    .build()
            ))
            .build();

        assertThat(actual.composedScenario.composedSteps.get(2)).isEqualTo(expected_2);

    }

    @Test
    public void should_iterate_step_when_using_previous_indexed_outputs_in_simple_input() {

        // Given
        List<Map<String, String>> datatable = asList(
            Maps.of("letter", "A"),
            Maps.of("letter", "B")
        );
        stubDatasetRepository(null, datatable);

        ExecutableComposedTestCase testCase = new ExecutableComposedTestCase(
            metadata,
            ExecutableComposedScenario.builder()
                .withComposedSteps(asList(
                    stepGenerating2Iterations,
                    ExecutableComposedStep.builder() // the step under test
                        .withName("Should generate 2 iterations using output_1 and output_2 previous outputs")
                        .withImplementation(Optional.of(
                            new StepImplementation("task", null, singletonMap("taskInput", "X + ${#output + Y}"), emptyMap())))
                        .build()
                    )
                )
                .build(),
            singletonMap("letter", "")
        );

        // When
        sut = new ComposedTestCaseDatatableIterationsPreProcessor(mockDatasetRepository);
        ExecutableComposedTestCase actual = sut.apply(testCase);

        // Then
        ExecutableComposedStep expected = ExecutableComposedStep.builder()
            .withName("Should generate 2 iterations using output_1 and output_2 previous outputs")
            .withStrategy(new Strategy(DataSetIterationsStrategy.TYPE, emptyMap()))
            .withSteps(Arrays.asList(
                ExecutableComposedStep.builder()
                    .withName("Should generate 2 iterations using output_1 and output_2 previous outputs - datatable iteration 1")
                    .withImplementation(Optional.of(
                        new StepImplementation("task", null, singletonMap("taskInput", "X + ${#output_1 + Y}"), emptyMap())
                    ))
                    .build(), ExecutableComposedStep.builder()
                    .withName("Should generate 2 iterations using output_1 and output_2 previous outputs - datatable iteration 2")
                    .withImplementation(Optional.of(
                        new StepImplementation("task", null, singletonMap("taskInput", "X + ${#output_2 + Y}"), emptyMap())
                    ))
                    .build()
            ))
            .build();

        assertThat(actual.composedScenario.composedSteps.get(1)).isEqualTo(expected);

    }

    @Test
    public void should_iterate_step_when_using_previous_indexed_outputs_combined_to_external_multivalues_dataset() {

        // Given
        List<Map<String, String>> datatable = asList(
            Maps.of("letter", "A"),
            Maps.of("letter", "B")
        );
        stubDatasetRepository(null, datatable);


        ExecutableComposedTestCase testCase = new ExecutableComposedTestCase(
            metadata,
            ExecutableComposedScenario.builder()
                .withComposedSteps(asList(
                    stepGenerating2Iterations,
                    ExecutableComposedStep.builder() // the step under test
                        .withName("Should generate 2 iterations using output_1 and output_2 previous outputs")
                        .withImplementation(Optional.of(
                            new StepImplementation("task", null, Maps.of("taskInput", "X + **var** + Y}"), emptyMap())))
                        .withExecutionParameters(singletonMap("var", "${#output.equals('**letter**')"))
                        .build()
                    )
                )
                .build(),
            singletonMap("letter", "")
        );

        // When
        sut = new ComposedTestCaseDatatableIterationsPreProcessor(mockDatasetRepository);
        ExecutableComposedTestCase actual = sut.apply(testCase);

        // Then
        ExecutableComposedStep expected = ExecutableComposedStep.builder()
            .withName("Should generate 2 iterations using output_1 and output_2 previous outputs")
            .withStrategy(new Strategy(DataSetIterationsStrategy.TYPE, emptyMap()))
            .withSteps(Arrays.asList(
                ExecutableComposedStep.builder()
                    .withName("Should generate 2 iterations using output_1 and output_2 previous outputs - datatable iteration 1")
                    .withImplementation(Optional.of(
                        new StepImplementation("task", null, singletonMap("taskInput", "X + **var** + Y}"), emptyMap())
                    ))
                    .withExecutionParameters(singletonMap("var", "${#output_1.equals('A')"))
                    .build(), ExecutableComposedStep.builder()
                    .withName("Should generate 2 iterations using output_1 and output_2 previous outputs - datatable iteration 2")
                    .withImplementation(Optional.of(
                        new StepImplementation("task", null, singletonMap("taskInput", "X + **var** + Y}"), emptyMap())
                    ))
                    .withExecutionParameters(singletonMap("var", "${#output_2.equals('B')"))
                    .build()
            ))
            .build();

        assertThat(actual.composedScenario.composedSteps.get(1)).isEqualTo(expected);

    }

    @Test
    public void should_iterate_step_when_using_two_previous_indexed_outputs_on_dataset() {

        // Given
        List<Map<String, String>> datatable = asList(
            Maps.of("letter", "A"),
            Maps.of("letter", "B")
        );
        stubDatasetRepository(null, datatable);
        ExecutableComposedStep stepGenerating2IterationsWithOutputs = ExecutableComposedStep.builder()
            .withName("Should generate 2 iterations for letter A and B")
            .withImplementation(Optional.of(
                new StepImplementation("task", null, emptyMap(), Maps.of("output", "**letter**", "otherOutput", "**letter**"))))
            .withExecutionParameters(singletonMap("letter", ""))
            .build();

        ExecutableComposedTestCase testCase = new ExecutableComposedTestCase(
            metadata,
            ExecutableComposedScenario.builder()
                .withComposedSteps(asList(
                    stepGenerating2IterationsWithOutputs,
                    ExecutableComposedStep.builder() // the step under test
                        .withName("Should generate 2 iterations using output_1 and output_2 previous outputs")
                        .withImplementation(Optional.of(
                            new StepImplementation("task", null, emptyMap(), emptyMap())))
                        .withExecutionParameters(Maps.of("var", "${#output}", "var2", "${#otherOutput}"))
                        .build()
                    )
                )
                .build(),
            singletonMap("letter", "")
        );

        // When
        sut = new ComposedTestCaseDatatableIterationsPreProcessor(mockDatasetRepository);
        ExecutableComposedTestCase actual = sut.apply(testCase);

        // Then
        ExecutableComposedStep expected = ExecutableComposedStep.builder()
            .withName("Should generate 2 iterations using output_1 and output_2 previous outputs")
            .withStrategy(new Strategy(DataSetIterationsStrategy.TYPE, emptyMap()))
            .withSteps(Arrays.asList(
                ExecutableComposedStep.builder()
                    .withName("Should generate 2 iterations using output_1 and output_2 previous outputs - datatable iteration 1")
                    .withImplementation(Optional.of(
                        new StepImplementation("task", null, emptyMap(), emptyMap())
                    ))
                    .withExecutionParameters(Maps.of("var", "${#output_1}", "var2", "${#otherOutput_1}"))
                    .build(), ExecutableComposedStep.builder()
                    .withName("Should generate 2 iterations using output_1 and output_2 previous outputs - datatable iteration 2")
                    .withImplementation(Optional.of(
                        new StepImplementation("task", null, emptyMap(), emptyMap())
                    ))
                    .withExecutionParameters(Maps.of("var", "${#output_2}", "var2", "${#otherOutput_2}"))
                    .build()
            ))
            .build();

        assertThat(actual.composedScenario.composedSteps.get(1)).isEqualTo(expected);

    }

    @Test
    public void should_not_match_partial_naming() {

        // Given
        List<Map<String, String>> datatable = asList(
            Maps.of("letter", "A"),
            Maps.of("letter", "B")
        );
        stubDatasetRepository(null, datatable);
        ExecutableComposedStep stepGenerating2IterationsWithOutputs = ExecutableComposedStep.builder()
            .withName("Should generate 2 iterations for letter A and B")
            .withImplementation(Optional.of(
                new StepImplementation("task", null, emptyMap(), Maps.of("SAME", "**letter**", "SAMEagain", "**letter**"))))
            .withExecutionParameters(singletonMap("letter", ""))
            .build();

        ExecutableComposedTestCase testCase = new ExecutableComposedTestCase(
            metadata,
            ExecutableComposedScenario.builder()
                .withComposedSteps(asList(
                    stepGenerating2IterationsWithOutputs,
                    ExecutableComposedStep.builder() // the step under test
                        .withName("Should generate 2 iterations using SAME and SAMEagain without confusion")
                        .withImplementation(Optional.empty())
                        .withExecutionParameters(Maps.of("var", "X+#SAME+}", "var2", "X+#SAMEagain"))
                        .build()
                    )
                )
                .build(),
            singletonMap("letter", "")
        );

        // When
        sut = new ComposedTestCaseDatatableIterationsPreProcessor(mockDatasetRepository);
        ExecutableComposedTestCase actual = sut.apply(testCase);

        // Then
        ExecutableComposedStep expected = ExecutableComposedStep.builder()
            .withName("Should generate 2 iterations using SAME and SAMEagain without confusion")
            .withStrategy(new Strategy(DataSetIterationsStrategy.TYPE, emptyMap()))
            .withSteps(Arrays.asList(
                ExecutableComposedStep.builder()
                    .withName("Should generate 2 iterations using SAME and SAMEagain without confusion - datatable iteration 1")
                    .withImplementation(Optional.empty())
                    .withExecutionParameters(Maps.of("var", "X+#SAME_1+}", "var2", "X+#SAMEagain_1"))
                    .build(),
                ExecutableComposedStep.builder()
                    .withName("Should generate 2 iterations using SAME and SAMEagain without confusion - datatable iteration 2")
                    .withImplementation(Optional.empty())
                    .withExecutionParameters(Maps.of("var", "X+#SAME_2+}", "var2", "X+#SAMEagain_2"))
                    .build()
            ))
            .build();

        assertThat(actual.composedScenario.composedSteps.get(1)).isEqualTo(expected);

    }

    @Test
    public void should_surcharge_indexed_variable() {
        // Given
        List<Map<String, String>> multipleValues = asList(
            Maps.of("letter", "A"),
            Maps.of("letter", "B")
        );
        stubDatasetRepository(null, multipleValues);
        ExecutableComposedStep stepGenerating2IterationsWithOutputs = ExecutableComposedStep.builder()
            .withName("Should generate 2 iterations for letter A and B")
            .withImplementation(Optional.of(
                new StepImplementation("task", null, emptyMap(), Maps.of("output", "**letter**"))))
            .withExecutionParameters(singletonMap("letter", ""))
            .build();
        ExecutableComposedStep stepSurchargeContextVariable = ExecutableComposedStep.builder()
            .withName("Should surcharge context variable 'output'")
            .withImplementation(Optional.of(
                new StepImplementation("task", null, emptyMap(), Maps.of("output", "final value"))))
            .build();

        ExecutableComposedTestCase testCase = new ExecutableComposedTestCase(
            metadata,
            ExecutableComposedScenario.builder()
                .withComposedSteps(asList(
                    stepGenerating2IterationsWithOutputs,
                    ExecutableComposedStep.builder()
                        .withName("Should generate 2 iterations using output")
                        .withImplementation(Optional.empty())
                        .withExecutionParameters(Maps.of("var", "${#output}"))
                        .build(),
                    stepSurchargeContextVariable,
                    ExecutableComposedStep.builder() // the step under test
                        .withName("Should not generate iterations")
                        .withImplementation(Optional.empty())
                        .withExecutionParameters(Maps.of("var", "${#output}"))
                        .build()
                    )
                )
                .build(),
            singletonMap("letter", "")
        );

        // When
        sut = new ComposedTestCaseDatatableIterationsPreProcessor(mockDatasetRepository);
        ExecutableComposedTestCase actual = sut.apply(testCase);

        // Then
        ExecutableComposedStep expected1 = ExecutableComposedStep.builder()
            .withName("Should generate 2 iterations using output")
            .withStrategy(new Strategy(DataSetIterationsStrategy.TYPE, emptyMap()))
            .withSteps(Arrays.asList(
                ExecutableComposedStep.builder()
                    .withName("Should generate 2 iterations using output - datatable iteration 1")
                    .withImplementation(Optional.empty())
                    .withExecutionParameters(Maps.of("var", "${#output_1}"))
                    .build(),
                ExecutableComposedStep.builder()
                    .withName("Should generate 2 iterations using output - datatable iteration 2")
                    .withImplementation(Optional.empty())
                    .withExecutionParameters(Maps.of("var", "${#output_2}"))
                    .build()
            ))
            .build();

        ExecutableComposedStep expected2 = ExecutableComposedStep.builder()
            .withName("Should not generate iterations")
            .withImplementation(Optional.empty())
            .withExecutionParameters(Maps.of("var", "${#output}"))
            .build();

        assertThat(actual.composedScenario.composedSteps.get(1)).isEqualTo(expected1);
        assertThat(actual.composedScenario.composedSteps.get(3)).isEqualTo(expected2);
    }

    @Test
    public void should_surcharge_indexed_variable_in_substep() {
        // Given
        List<Map<String, String>> multipleValues = asList(
            Maps.of("letter", "A"),
            Maps.of("letter", "B")
        );
        stubDatasetRepository(null, multipleValues);
        ExecutableComposedStep stepGenerating2IterationsWithOutputs = ExecutableComposedStep.builder()
            .withName("Should generate 2 iterations for letter A and B")
            .withImplementation(Optional.of(
                new StepImplementation("task", null, emptyMap(), Maps.of("output", "**letter**"))))
            .withExecutionParameters(singletonMap("letter", ""))
            .build();
        ExecutableComposedStep substepSurchargeContextVariable = ExecutableComposedStep.builder()
            .withName("Should surcharge context variable 'output'")
            .withImplementation(Optional.of(
                new StepImplementation("task", null, emptyMap(), Maps.of("output", "final value"))))
            .build();

        ExecutableComposedStep stepSurchargeContextVariable = ExecutableComposedStep.builder()
            .withName("Should surcharge context variable 'output'")
            .withSteps(asList(substepSurchargeContextVariable))
            .build();

        ExecutableComposedTestCase testCase = new ExecutableComposedTestCase(
            metadata,
            ExecutableComposedScenario.builder()
                .withComposedSteps(asList(
                    stepGenerating2IterationsWithOutputs,
                    ExecutableComposedStep.builder()
                        .withName("Should generate 2 iterations using output")
                        .withImplementation(Optional.empty())
                        .withExecutionParameters(Maps.of("var", "${#output}"))
                        .build(),
                    stepSurchargeContextVariable,
                    ExecutableComposedStep.builder() // the step under test
                        .withName("Should not generate iterations")
                        .withImplementation(Optional.empty())
                        .withExecutionParameters(Maps.of("var", "${#output}"))
                        .build()
                    )
                )
                .build(),
            singletonMap("letter", "")
        );

        // When
        sut = new ComposedTestCaseDatatableIterationsPreProcessor(mockDatasetRepository);
        ExecutableComposedTestCase actual = sut.apply(testCase);

        // Then
        ExecutableComposedStep expected1 = ExecutableComposedStep.builder()
            .withName("Should generate 2 iterations using output")
            .withStrategy(new Strategy(DataSetIterationsStrategy.TYPE, emptyMap()))
            .withSteps(Arrays.asList(
                ExecutableComposedStep.builder()
                    .withName("Should generate 2 iterations using output - datatable iteration 1")
                    .withImplementation(Optional.empty())
                    .withExecutionParameters(Maps.of("var", "${#output_1}"))
                    .build(),
                ExecutableComposedStep.builder()
                    .withName("Should generate 2 iterations using output - datatable iteration 2")
                    .withImplementation(Optional.empty())
                    .withExecutionParameters(Maps.of("var", "${#output_2}"))
                    .build()
            ))
            .build();

        ExecutableComposedStep expected2 = ExecutableComposedStep.builder()
            .withName("Should not generate iterations")
            .withImplementation(Optional.empty())
            .withExecutionParameters(Maps.of("var", "${#output}"))
            .build();

        assertThat(actual.composedScenario.composedSteps.get(1)).isEqualTo(expected1);
        assertThat(actual.composedScenario.composedSteps.get(3)).isEqualTo(expected2);
    }

    @Test
    public void should_iterate_step_when_using_two_previous_indexed_outputs() {

        // Given
        List<Map<String, String>> datatable = asList(
            Maps.of("letter", "A"),
            Maps.of("letter", "B")
        );
        stubDatasetRepository(null, datatable);
        ExecutableComposedStep stepGenerating2IterationsWithOutputs = ExecutableComposedStep.builder()
            .withName("Should generate 2 iterations for letter A and B")
            .withImplementation(Optional.of(
                new StepImplementation("task", null, emptyMap(), Maps.of("output", "**letter**", "otherOutput", "**letter**"))))
            .withExecutionParameters(singletonMap("letter", ""))
            .build();

        ExecutableComposedTestCase testCase = new ExecutableComposedTestCase(
            metadata,
            ExecutableComposedScenario.builder()
                .withComposedSteps(asList(
                    stepGenerating2IterationsWithOutputs,
                    ExecutableComposedStep.builder() // the step under test
                        .withName("Should generate 2 iterations using output_1 and output_2 previous outputs")
                        .withImplementation(Optional.of(
                            new StepImplementation("task", null, Maps.of("taskInput", "X + ${#output} + Y}", "taskOtherInput", "${#otherOutput}"), emptyMap())))
                        .build()
                    )
                )
                .build(),
            singletonMap("letter", "")
        );

        // When
        sut = new ComposedTestCaseDatatableIterationsPreProcessor(mockDatasetRepository);
        ExecutableComposedTestCase actual = sut.apply(testCase);

        // Then
        ExecutableComposedStep expected = ExecutableComposedStep.builder()
            .withName("Should generate 2 iterations using output_1 and output_2 previous outputs")
            .withStrategy(new Strategy(DataSetIterationsStrategy.TYPE, emptyMap()))
            .withSteps(Arrays.asList(
                ExecutableComposedStep.builder()
                    .withName("Should generate 2 iterations using output_1 and output_2 previous outputs - datatable iteration 1")
                    .withImplementation(Optional.of(
                        new StepImplementation("task", null, Maps.of("taskInput", "X + ${#output_1} + Y}", "taskOtherInput", "${#otherOutput_1}"), emptyMap())
                    ))
                    .build(), ExecutableComposedStep.builder()
                    .withName("Should generate 2 iterations using output_1 and output_2 previous outputs - datatable iteration 2")
                    .withImplementation(Optional.of(
                        new StepImplementation("task", null, Maps.of("taskInput", "X + ${#output_2} + Y}", "taskOtherInput", "${#otherOutput_2}"), emptyMap())
                    ))
                    .build()
            ))
            .build();

        assertThat(actual.composedScenario.composedSteps.get(1)).isEqualTo(expected);

    }

    @Test
    public void should_iterate_step_when_using_outputKey_id_defined_as_dataset() {

        // Given
        List<Map<String, String>> datatable = asList(
            Maps.of("letter", "A"),
            Maps.of("letter", "B")
        );
        stubDatasetRepository(null, datatable);
        ExecutableComposedStep stepGenerating2IterationsWithOutputs = ExecutableComposedStep.builder()
            .withName("Should generate 2 iterations for letter A and B")
            .withImplementation(Optional.of(
                new StepImplementation("task", null, emptyMap(), Maps.of("**outputVar**", "**letter**"))))
            .withExecutionParameters(Maps.of("outputVar", "output", "letter", ""))
            .build();

        ExecutableComposedTestCase testCase = new ExecutableComposedTestCase(
            metadata,
            ExecutableComposedScenario.builder()
                .withComposedSteps(asList(
                    stepGenerating2IterationsWithOutputs,
                    ExecutableComposedStep.builder() // the step under test
                        .withName("Should generate 2 iterations using output_1 and output_2 previous outputs")
                        .withImplementation(Optional.of(
                            new StepImplementation("task", null, Maps.of("taskInput", "X + ${#output} + Y}"), emptyMap())))
                        .build()
                    )
                )
                .build(),
            singletonMap("letter", "")
        );

        // When
        sut = new ComposedTestCaseDatatableIterationsPreProcessor(mockDatasetRepository);
        ExecutableComposedTestCase actual = sut.apply(testCase);

        // Then
        ExecutableComposedStep expected = ExecutableComposedStep.builder()
            .withName("Should generate 2 iterations using output_1 and output_2 previous outputs")
            .withStrategy(new Strategy(DataSetIterationsStrategy.TYPE, emptyMap()))
            .withSteps(Arrays.asList(
                ExecutableComposedStep.builder()
                    .withName("Should generate 2 iterations using output_1 and output_2 previous outputs - datatable iteration 1")
                    .withImplementation(Optional.of(
                        new StepImplementation("task", null, Maps.of("taskInput", "X + ${#output_1} + Y}"), emptyMap())
                    ))
                    .build(), ExecutableComposedStep.builder()
                    .withName("Should generate 2 iterations using output_1 and output_2 previous outputs - datatable iteration 2")
                    .withImplementation(Optional.of(
                        new StepImplementation("task", null, Maps.of("taskInput", "X + ${#output_2} + Y}"), emptyMap())
                    ))
                    .build()
            ))
            .build();

        assertThat(actual.composedScenario.composedSteps.get(1)).isEqualTo(expected);

    }

    @Test
    public void should_iterate_step_when_using_output_defined_in_substep_with_outputkey_as_dataset() {

        // Given
        List<Map<String, String>> multipleValues = asList(
            Maps.of("letter", "A"),
            Maps.of("letter", "B")
        );
        stubDatasetRepository(null, multipleValues);
        ExecutableComposedStep firstSubstepGenerating2IterationsWithOutputs = ExecutableComposedStep.builder()
            .withName("Should generate 2 iterations for letter A and B")
            .withImplementation(Optional.of(
                new StepImplementation("task", null, emptyMap(), Maps.of("**outputKey**", "**outputValue**"))))
            .withExecutionParameters(Maps.of("outputKey", "**letter**", "outputValue", "**letter**"))
            .build();
        ExecutableComposedStep secondSubstepGenerating2IterationsWithOutputs = ExecutableComposedStep.builder()
            .withName("Should generate 2 iterations for letter A and B")
            .withImplementation(Optional.of(
                new StepImplementation("task", null, emptyMap(), Maps.of("**outputKey**", "**outputValue**"))))
            .withExecutionParameters(Maps.of("outputKey", "**outputVar**", "outputValue", "**letter**"))
            .build();

        ExecutableComposedStep stepGenerating2IterationsWithOutputs = ExecutableComposedStep.builder()
            .withSteps(asList(firstSubstepGenerating2IterationsWithOutputs, secondSubstepGenerating2IterationsWithOutputs))
            .withExecutionParameters(Maps.of("outputVar", "output", "letter", ""))
            .build();

        ExecutableComposedTestCase testCase = new ExecutableComposedTestCase(
            metadata,
            ExecutableComposedScenario.builder()
                .withComposedSteps(asList(
                    stepGenerating2IterationsWithOutputs,
                    ExecutableComposedStep.builder() // the step under test
                        .withName("Should generate 2 iterations using output_1 and output_2 previous outputs")
                        .withImplementation(Optional.of(
                            new StepImplementation("task", null, Maps.of("taskInput", "**var**"), emptyMap())))
                        .withExecutionParameters(Maps.of("var", "${#output}"))
                        .build()
                    )
                )
                .build(),
            singletonMap("letter", "")
        );

        // When
        sut = new ComposedTestCaseDatatableIterationsPreProcessor(mockDatasetRepository);
        ExecutableComposedTestCase actual = sut.apply(testCase);

        // Then
        ExecutableComposedStep expected = ExecutableComposedStep.builder()
            .withName("Should generate 2 iterations using output_1 and output_2 previous outputs")
            .withStrategy(new Strategy(DataSetIterationsStrategy.TYPE, emptyMap()))
            .withSteps(Arrays.asList(
                ExecutableComposedStep.builder()
                    .withName("Should generate 2 iterations using output_1 and output_2 previous outputs - datatable iteration 1")
                    .withImplementation(Optional.of(
                        new StepImplementation("task", null, Maps.of("taskInput", "**var**"), emptyMap())
                    ))
                    .withExecutionParameters(Maps.of("var", "${#output_1}"))
                    .build(), ExecutableComposedStep.builder()
                    .withName("Should generate 2 iterations using output_1 and output_2 previous outputs - datatable iteration 2")
                    .withImplementation(Optional.of(
                        new StepImplementation("task", null, Maps.of("taskInput", "**var**"), emptyMap())
                    ))
                    .withExecutionParameters(Maps.of("var", "${#output_2}"))
                    .build()
            ))
            .build();
        assertThat(actual.composedScenario.composedSteps.get(1)).isEqualTo(expected);
    }

    @Test
    public void should_iterate_step_when_using_previous_indexed_outputs_in_map_input() {

        // Given
        List<Map<String, String>> datatable = asList(
            Maps.of("letter", "A"),
            Maps.of("letter", "B")
        );
        stubDatasetRepository(null, datatable);

        ExecutableComposedTestCase testCase = new ExecutableComposedTestCase(
            metadata,
            ExecutableComposedScenario.builder()
                .withComposedSteps(asList(
                    stepGenerating2Iterations,
                    ExecutableComposedStep.builder() // step under test
                        .withName("Should generate 2 iterations using output_1 and output_2 previous outputs")
                        .withImplementation(Optional.of(
                            new StepImplementation("task", null, singletonMap("taskMapInput", singletonMap("${#output} + X", "${#output} + Y")), emptyMap())))
                        .build()
                    )
                )
                .build(),
            singletonMap("letter", "")
        );

        sut = new ComposedTestCaseDatatableIterationsPreProcessor(mockDatasetRepository);

        // When
        ExecutableComposedTestCase actual = sut.apply(testCase);

        // Then
        ExecutableComposedStep expected = ExecutableComposedStep.builder()
            .withName("Should generate 2 iterations using output_1 and output_2 previous outputs")
            .withStrategy(new Strategy(DataSetIterationsStrategy.TYPE, emptyMap()))
            .withSteps(Arrays.asList(
                ExecutableComposedStep.builder()
                    .withName("Should generate 2 iterations using output_1 and output_2 previous outputs - datatable iteration 1")
                    .withImplementation(Optional.of(
                        new StepImplementation("task", null, singletonMap("taskMapInput", singletonMap("${#output_1} + X", "${#output_1} + Y")), emptyMap())
                    ))
                    .build(), ExecutableComposedStep.builder()
                    .withName("Should generate 2 iterations using output_1 and output_2 previous outputs - datatable iteration 2")
                    .withImplementation(Optional.of(
                        new StepImplementation("task", null, singletonMap("taskMapInput", singletonMap("${#output_2} + X", "${#output_2} + Y")), emptyMap())
                    ))
                    .build()
            ))
            .build();

        assertThat(actual.composedScenario.composedSteps.get(1)).isEqualTo(expected);

    }

    @Test
    public void should_iterate_step_when_using_previous_indexed_outputs_in_task_output() {

        // Given
        List<Map<String, String>> datatable = asList(
            Maps.of("letter", "A"),
            Maps.of("letter", "B")
        );
        stubDatasetRepository(null, datatable);

        ExecutableComposedTestCase testCase = new ExecutableComposedTestCase(
            metadata,
            ExecutableComposedScenario.builder()
                .withComposedSteps(asList(
                    stepGenerating2Iterations,
                    ExecutableComposedStep.builder()
                        .withName("Should generate 2 iterations using previous outputs in their output")
                        .withImplementation(Optional.of(
                            new StepImplementation("task", null, emptyMap(), singletonMap("key_to_index", "${#output} + X"))))
                        .build()
                    )
                )
                .build(),
            singletonMap("letter", "")
        );

        sut = new ComposedTestCaseDatatableIterationsPreProcessor(mockDatasetRepository);

        // When
        ExecutableComposedTestCase actual = sut.apply(testCase);

        // Then
        ExecutableComposedStep expected = ExecutableComposedStep.builder()
            .withName("Should generate 2 iterations using previous outputs in their output")
            .withStrategy(new Strategy(DataSetIterationsStrategy.TYPE, emptyMap()))
            .withSteps(Arrays.asList(
                ExecutableComposedStep.builder()
                    .withName("Should generate 2 iterations using previous outputs in their output - datatable iteration 1")
                    .withImplementation(Optional.of(
                        new StepImplementation("task", null, emptyMap(), singletonMap("key_to_index_1", "${#output_1} + X"))
                    ))
                    .build(), ExecutableComposedStep.builder()
                    .withName("Should generate 2 iterations using previous outputs in their output - datatable iteration 2")
                    .withImplementation(Optional.of(
                        new StepImplementation("task", null, emptyMap(), singletonMap("key_to_index_2", "${#output_2} + X"))
                    ))
                    .build()
            ))
            .build();

        assertThat(actual.composedScenario.composedSteps.get(1)).isEqualTo(expected);

    }

    @Test
    public void should_iterate_step_when_using_previous_indexed_outputs_in_dataset() {

        // Given
        List<Map<String, String>> datatable = asList(
            Maps.of("letter", "A"),
            Maps.of("letter", "B")
        );
        stubDatasetRepository(null, datatable);

        ExecutableComposedTestCase testCase = new ExecutableComposedTestCase(
            metadata,
            ExecutableComposedScenario.builder()
                .withComposedSteps(asList(
                    stepGenerating2Iterations,
                    ExecutableComposedStep.builder()
                        .withName("Should generate 2 iterations using previous outputs in their dataset")
                        .withImplementation(Optional.of(
                            new StepImplementation("task", null, singletonMap("taskInput", "**myParam**"), emptyMap())
                        ))
                        .withExecutionParameters(singletonMap("myParam", "X + ${#output} + Y"))
                        .build()
                    )
                )
                .build(),
            singletonMap("letter", "")
        );

        sut = new ComposedTestCaseDatatableIterationsPreProcessor(mockDatasetRepository);

        // When
        ExecutableComposedTestCase actual = sut.apply(testCase);

        // Then
        ExecutableComposedStep expected = ExecutableComposedStep.builder()
            .withName("Should generate 2 iterations using previous outputs in their dataset")
            .withStrategy(new Strategy(DataSetIterationsStrategy.TYPE, emptyMap()))
            .withSteps(asList(
                ExecutableComposedStep.builder()
                    .withName("Should generate 2 iterations using previous outputs in their dataset - datatable iteration 1")
                    .withImplementation(Optional.of(
                        new StepImplementation("task", null, singletonMap("taskInput", "**myParam**"), emptyMap())
                    ))
                    .withExecutionParameters(singletonMap("myParam", "X + ${#output_1} + Y"))
                    .build(),
                ExecutableComposedStep.builder()
                    .withName("Should generate 2 iterations using previous outputs in their dataset - datatable iteration 2")
                    .withImplementation(Optional.of(
                        new StepImplementation("task", null, singletonMap("taskInput", "**myParam**"), emptyMap())
                    ))
                    .withExecutionParameters(singletonMap("myParam", "X + ${#output_2} + Y"))
                    .build()
            ))
            .build();

        assertThat(actual.composedScenario.composedSteps.get(1)).isEqualTo(expected);

    }

    @Test
    public void should_iterate_step_having_both_previous_indexed_outputs_and_indexing_new_outputs() {

        // Given
        List<Map<String, String>> datatable = asList(
            Maps.of("letter", "A"),
            Maps.of("letter", "B")
        );
        stubDatasetRepository(null, datatable);

        ExecutableComposedTestCase testCase = new ExecutableComposedTestCase(
            metadata,
            ExecutableComposedScenario.builder()
                .withComposedSteps(asList(
                    stepGenerating2Iterations,
                    ExecutableComposedStep.builder()
                        .withName("Should generate 2 iterations having previous indexed output and indexing 2 other outputs")
                        .withImplementation(Optional.of(
                            new StepImplementation("task", null, singletonMap("taskInput", "${#output} + X"), singletonMap("otherOutput", "**letter**"))
                        ))
                        .withExecutionParameters(singletonMap("letter", ""))
                        .build()
                    )
                )
                .build(),
            singletonMap("letter", "")
        );

        sut = new ComposedTestCaseDatatableIterationsPreProcessor(mockDatasetRepository);

        // When
        ExecutableComposedTestCase actual = sut.apply(testCase);

        // Then
        ExecutableComposedStep expected = ExecutableComposedStep.builder()
            .withName("Should generate 2 iterations having previous indexed output and indexing 2 other outputs")
            .withStrategy(new Strategy(DataSetIterationsStrategy.TYPE, emptyMap()))
            .withSteps(asList(
                ExecutableComposedStep.builder()
                    .withName("Should generate 2 iterations having previous indexed output and indexing 2 other outputs - datatable iteration 1")
                    .withImplementation(Optional.of(
                        new StepImplementation("task", null, singletonMap("taskInput", "${#output_1} + X"), singletonMap("otherOutput_1", "**letter**"))
                    ))
                    .withExecutionParameters(singletonMap("letter", "A"))
                    .build(),
                ExecutableComposedStep.builder()
                    .withName("Should generate 2 iterations having previous indexed output and indexing 2 other outputs - datatable iteration 2")
                    .withImplementation(Optional.of(
                        new StepImplementation("task", null, singletonMap("taskInput", "${#output_2} + X"), singletonMap("otherOutput_2", "**letter**"))
                    ))
                    .withExecutionParameters(singletonMap("letter", "B"))
                    .build()
            ))
            .build();

        assertThat(actual.composedScenario.composedSteps.get(1)).isEqualTo(expected);

    }

    @Test
    public void should_generate_iterations_indexing_only_new_outputs() {

        // Given
        List<Map<String, String>> datatable = asList(
            Maps.of("letter", "A"),
            Maps.of("letter", "B")
        );
        stubDatasetRepository(null, datatable);

        ExecutableComposedTestCase testCase = new ExecutableComposedTestCase(
            metadata,
            ExecutableComposedScenario.builder()
                .withComposedSteps(asList(
                    stepGenerating2Iterations,
                    ExecutableComposedStep.builder()
                        .withName("Should generate 2 iterations indexing otherOutputs only")
                        .withImplementation(Optional.of(
                            new StepImplementation("task", null, singletonMap("taskInput", "${#notFromIteration}"), singletonMap("otherOutput", "**letter**"))
                        ))
                        .withExecutionParameters(singletonMap("letter", ""))
                        .build()
                    )
                )
                .build(),
            singletonMap("letter", "")
        );

        sut = new ComposedTestCaseDatatableIterationsPreProcessor(mockDatasetRepository);

        // When
        ExecutableComposedTestCase actual = sut.apply(testCase);

        // Then
        ExecutableComposedStep expected = ExecutableComposedStep.builder()
            .withName("Should generate 2 iterations indexing otherOutputs only")
            .withStrategy(new Strategy(DataSetIterationsStrategy.TYPE, emptyMap()))
            .withSteps(asList(
                ExecutableComposedStep.builder()
                    .withName("Should generate 2 iterations indexing otherOutputs only - datatable iteration 1")
                    .withImplementation(Optional.of(
                        new StepImplementation("task", null, singletonMap("taskInput", "${#notFromIteration}"), singletonMap("otherOutput_1", "**letter**"))
                    ))
                    .withExecutionParameters(singletonMap("letter", "A"))
                    .build(),
                ExecutableComposedStep.builder()
                    .withName("Should generate 2 iterations indexing otherOutputs only - datatable iteration 2")
                    .withImplementation(Optional.of(
                        new StepImplementation("task", null, singletonMap("taskInput", "${#notFromIteration}"), singletonMap("otherOutput_2", "**letter**"))
                    ))
                    .withExecutionParameters(singletonMap("letter", "B"))
                    .build()
            ))
            .build();

        assertThat(actual.composedScenario.composedSteps.get(1)).isEqualTo(expected);

    }

    @Test
    public void should_index_iterations_outputs_when_inside_a_parent_step() {

        // Given
        List<Map<String, String>> datatable = asList(
            Maps.of("letter", "A"),
            Maps.of("letter", "B")
        );
        stubDatasetRepository(null, datatable);

        ExecutableComposedTestCase testCase = new ExecutableComposedTestCase(
            metadata,
            ExecutableComposedScenario.builder()
                .withComposedSteps(singletonList(
                    ExecutableComposedStep.builder()
                        .withName("Parent step with 2 substeps")
                        .withSteps(asList(
                            ExecutableComposedStep.builder()
                                .withName("First sub step")
                                .withImplementation(Optional.of(
                                    new StepImplementation("task", null, emptyMap(), singletonMap("output", "**letter**"))
                                ))
                                .withExecutionParameters(singletonMap("letter", ""))
                                .build(),
                            ExecutableComposedStep.builder()
                                .withName("Second sub step")
                                .withImplementation(Optional.of(
                                    new StepImplementation("task", null, singletonMap("${#output}", "**letter**"), emptyMap())
                                ))
                                .build()
                        ))
                        .withExecutionParameters(singletonMap("letter", ""))
                        .build()
                    )
                )
                .build(),
            singletonMap("letter", "")
        );

        sut = new ComposedTestCaseDatatableIterationsPreProcessor(mockDatasetRepository);

        // When
        ExecutableComposedTestCase actual = sut.apply(testCase);

        // Then

        ExecutableComposedScenario expected = ExecutableComposedScenario.builder()
            .withComposedSteps(singletonList(
                ExecutableComposedStep.builder()
                    .withName("Parent step with 2 substeps")
                    .withStrategy(new Strategy(DataSetIterationsStrategy.TYPE, emptyMap()))
                    .withSteps(asList(
                        ExecutableComposedStep.builder()
                            .withName("Parent step with 2 substeps - datatable iteration 1")
                            .withSteps(asList(
                                ExecutableComposedStep.builder()
                                    .withName("First sub step")
                                    .withImplementation(Optional.of(
                                        new StepImplementation("task", null, emptyMap(), singletonMap("output_1", "**letter**"))
                                    ))
                                    .withExecutionParameters(singletonMap("letter", ""))
                                    .build(),
                                ExecutableComposedStep.builder()
                                    .withName("Second sub step")
                                    .withImplementation(Optional.of(
                                        new StepImplementation("task", null, singletonMap("${#output_1}", "**letter**"), emptyMap())
                                    ))
                                    .build()
                            ))
                            .withExecutionParameters(singletonMap("letter", "A"))
                            .build(),
                        ExecutableComposedStep.builder()
                            .withName("Parent step with 2 substeps - datatable iteration 2")
                            .withSteps(asList(
                                ExecutableComposedStep.builder()
                                    .withName("First sub step")
                                    .withImplementation(Optional.of(
                                        new StepImplementation("task", null, emptyMap(), singletonMap("output_2", "**letter**"))
                                    ))
                                    .withExecutionParameters(singletonMap("letter", ""))
                                    .build(),
                                ExecutableComposedStep.builder()
                                    .withName("Second sub step")
                                    .withImplementation(Optional.of(
                                        new StepImplementation("task", null, singletonMap("${#output_2}", "**letter**"), emptyMap())
                                    ))
                                    .build()
                            ))
                            .withExecutionParameters(singletonMap("letter", "B"))
                            .build()
                    ))
                    .build()
                )
            )
            .build();

        assertThat(actual.composedScenario).isEqualTo(expected);

    }

    @Test
    public void should_index_iterations_inputs_when_generated_in_same_parent_step() {

        // Given
        List<Map<String, String>> datatable = asList(
            Maps.of("letter", "A"),
            Maps.of("letter", "B")
        );
        stubDatasetRepository(null, datatable);

        ExecutableComposedTestCase testCase = new ExecutableComposedTestCase(
            metadata,
            ExecutableComposedScenario.builder()
                .withComposedSteps(singletonList(
                    ExecutableComposedStep.builder()
                        .withName("Parent step with 2 substeps")
                        .withSteps(asList(
                            ExecutableComposedStep.builder()
                                .withName("First sub step")
                                .withImplementation(Optional.of(
                                    new StepImplementation("task", null, emptyMap(), singletonMap("output", "**letter**"))
                                ))
                                .withExecutionParameters(singletonMap("letter", ""))
                                .build(),
                            ExecutableComposedStep.builder()
                                .withName("Second sub step")
                                .withImplementation(Optional.of(
                                    new StepImplementation("task", null, singletonMap("input", "**data**"), emptyMap())
                                ))
                                .withExecutionParameters(singletonMap("data", "${#output}"))
                                .build()
                        ))
                        .withExecutionParameters(singletonMap("letter", ""))
                        .build()
                    )
                )
                .build(),
            singletonMap("letter", "")
        );

        sut = new ComposedTestCaseDatatableIterationsPreProcessor(mockDatasetRepository);

        // When
        ExecutableComposedTestCase actual = sut.apply(testCase);

        // Then

        ExecutableComposedScenario expected = ExecutableComposedScenario.builder()
            .withComposedSteps(singletonList(
                ExecutableComposedStep.builder()
                    .withName("Parent step with 2 substeps")
                    .withStrategy(new Strategy(DataSetIterationsStrategy.TYPE, emptyMap()))
                    .withSteps(asList(
                        ExecutableComposedStep.builder()
                            .withName("Parent step with 2 substeps - datatable iteration 1")
                            .withSteps(asList(
                                ExecutableComposedStep.builder()
                                    .withName("First sub step")
                                    .withImplementation(Optional.of(
                                        new StepImplementation("task", null, emptyMap(), singletonMap("output_1", "**letter**"))
                                    ))
                                    .withExecutionParameters(singletonMap("letter", ""))
                                    .build(),
                                ExecutableComposedStep.builder()
                                    .withName("Second sub step")
                                    .withImplementation(Optional.of(
                                        new StepImplementation("task", null, singletonMap("input", "**data**"), emptyMap())
                                    ))
                                    .withExecutionParameters(singletonMap("data", "${#output_1}"))
                                    .build()
                            ))
                            .withExecutionParameters(singletonMap("letter", "A"))
                            .build(),
                        ExecutableComposedStep.builder()
                            .withName("Parent step with 2 substeps - datatable iteration 2")
                            .withSteps(asList(
                                ExecutableComposedStep.builder()
                                    .withName("First sub step")
                                    .withImplementation(Optional.of(
                                        new StepImplementation("task", null, emptyMap(), singletonMap("output_2", "**letter**"))
                                    ))
                                    .withExecutionParameters(singletonMap("letter", ""))
                                    .build(),
                                ExecutableComposedStep.builder()
                                    .withName("Second sub step")
                                    .withImplementation(Optional.of(
                                        new StepImplementation("task", null, singletonMap("input", "**data**"), emptyMap())
                                    ))
                                    .withExecutionParameters(singletonMap("data", "${#output_2}"))
                                    .build()
                            ))
                            .withExecutionParameters(singletonMap("letter", "B"))
                            .build()
                    ))
                    .build()
                )
            )
            .build();

        assertThat(actual.composedScenario).isEqualTo(expected);

    }

}
