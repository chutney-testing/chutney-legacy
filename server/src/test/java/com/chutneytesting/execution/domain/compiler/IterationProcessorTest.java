package com.chutneytesting.execution.domain.compiler;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static java.util.Optional.ofNullable;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.chutneytesting.design.domain.dataset.DataSet;
import com.chutneytesting.design.domain.dataset.DataSetRepository;
import com.chutneytesting.design.domain.scenario.TestCaseMetadataImpl;
import com.chutneytesting.design.domain.scenario.compose.Strategy;
import com.chutneytesting.engine.domain.execution.strategies.DataSetIterationsStrategy;
import com.chutneytesting.execution.domain.scenario.composed.ExecutableComposedScenario;
import com.chutneytesting.execution.domain.scenario.composed.ExecutableComposedStep;
import com.chutneytesting.execution.domain.scenario.composed.ExecutableComposedTestCase;
import com.chutneytesting.execution.domain.scenario.composed.StepImplementation;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.groovy.util.Maps;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class IterationProcessorTest {

    ComposedTestCaseDataSetPreProcessor sut;

    private DataSetRepository mockDatasetRepository;

    @Before
    public void setUp() {
        mockDatasetRepository = mock(DataSetRepository.class);
    }

    private void stubDataSetrepository(Map<String, String> uniqueValues, List<Map<String, String>> multipleValues) {
        when(mockDatasetRepository.findById(any())).thenReturn(
            DataSet.builder()
                .withUniqueValues(ofNullable(uniqueValues).orElse(emptyMap()))
                .withMultipleValues(ofNullable(multipleValues).orElse(emptyList()))
                .build()
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
        stubDataSetrepository(null, multipleValues);

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

        sut = new ComposedTestCaseDataSetPreProcessor(mockDatasetRepository);

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

    // TODO //
    // a test with a step using 2 previous indexed values and which should create 2 other indexed outputs values //
    // a test with a step using 2 previous indexed values and 3 multivalues in input, how many iteration should we have ? 2, 3, 6 combine ? Fuck ! //


    @Test
    public void should_index_iterations_outputs_when_inside_a_parent_step() {

        // Given
        List<Map<String, String>> multipleValues = asList(
            Maps.of("letter", "a"),
            Maps.of("letter", "b"),
            Maps.of("letter", "c")
        );
        stubDataSetrepository(null, multipleValues);

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

        sut = new ComposedTestCaseDataSetPreProcessor(mockDatasetRepository);

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
                            .build(),
                        ExecutableComposedStep.builder()
                            .withName("Success - should uppercase & assert each char - dataset iteration 3")
                            .withSteps(asList(
                                ExecutableComposedStep.builder()
                                    .withName("Success - should uppercase a char")
                                    .withImplementation(Optional.of(new StepImplementation("success", null, emptyMap(), singletonMap("result_3", "${new String(\"**letter**\").toUpperCase()"))))
                                    .withDataset(singletonMap("letter", ""))
                                    .build(),
                                ExecutableComposedStep.builder()
                                    .withName("Should assert an uppercased char")
                                    .withImplementation(Optional.of(
                                        new StepImplementation("assert", null, singletonMap("${#result_3}", "${new String(\"**letter**\").toUpperCase()"), emptyMap())
                                    ))
                                    .build()
                            ))
                            .withDataset(singletonMap("letter", "c"))
                            .build()
                    ))
                    .build()
                )
            )
            .build();

        assertThat(actual.composedScenario).isEqualTo(expected);

    }

    @Test
    @Ignore
    public void should_index_iteration_dataset_using_previous_indexed_outputs() {

        // Given
        List<Map<String, String>> multipleValues = asList(
            Maps.of("letter", "a"),
            Maps.of("letter", "b"),
            Maps.of("letter", "c")
        );
        stubDataSetrepository(null, multipleValues);

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
                            new StepImplementation("context-put", null, singletonMap("**myParam**", "fake"), emptyMap())
                        ))
                        .withDataset(singletonMap("myParam", "${#result}"))
                        .build()
                    )
                )
                .build(),
            singletonMap("letter", "")
        );

        sut = new ComposedTestCaseDataSetPreProcessor(mockDatasetRepository);

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
                                new StepImplementation("context-put", null, singletonMap("entries", singletonMap("**myParam**", "fake")), emptyMap())
                            ))
                            .withDataset(singletonMap("**myParam**", "${#result_1}"))
                            .build(),
                        ExecutableComposedStep.builder()
                            .withName("Context put - should create one context entry for each uppercased char - dataset iteration 2")
                            .withImplementation(Optional.of(
                                new StepImplementation("context-put", null, singletonMap("entries", singletonMap("**myParam**", "fake")), emptyMap())
                            ))
                            .withDataset(singletonMap("**myParam**", "${#result_2}"))
                            .build(),
                        ExecutableComposedStep.builder()
                            .withName("Context put - should create one context entry for each uppercased char - dataset iteration 3")
                            .withImplementation(Optional.of(
                                new StepImplementation("context-put", null, singletonMap("entries", singletonMap("**myParam**", "fake")), emptyMap())
                            ))
                            .withDataset(singletonMap("**myParam**", "${#result_3}"))
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

