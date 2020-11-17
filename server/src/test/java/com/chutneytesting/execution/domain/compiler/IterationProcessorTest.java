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
    @Ignore
    public void should_create_indexable_iterations_1() {

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
                        .overrideDataSetWith(singletonMap("letter", ""))
                        .build(),
                    ExecutableComposedStep.builder()
                        .withName("Assert index & uppercased char")
                        .withImplementation(Optional.of(new StepImplementation("assert", null, Maps.of(
                            "${#result_1}", "A",
                            "${#result_2}", "B",
                            "${#result_3}", "C"), emptyMap())))
                        .build(),
                    ExecutableComposedStep.builder()
                        .withName("Context put - should create one context entry for each uppercased char")
                        .withImplementation(Optional.of(new StepImplementation("context-put", null, singletonMap("entries", singletonMap("${#result}", "fake_value")), emptyMap())))
                        .build(),
                    ExecutableComposedStep.builder()
                        .withName("Assert - each context entry contains a value")
                        .withImplementation(Optional.of(new StepImplementation("assert", null, Maps.of(
                            "${#result_1}", "fake_value",
                            "${#result_2}", "fake_value",
                            "${#result_3}", "fake_value"), emptyMap())
                        ))
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
                    .withSteps(Arrays.asList(
                        ExecutableComposedStep.builder()
                            .withName("Success - should uppercase each char - iteration 1")
                            .withImplementation(Optional.of(new StepImplementation("success", null, emptyMap(), singletonMap("result_1", "${new String(\"**letter**\").toUpperCase()"))))
                            .overrideDataSetWith(singletonMap("letter", "a"))
                            .build(),
                        ExecutableComposedStep.builder()
                            .withName("Success - should uppercase each char - iteration 2")
                            .withImplementation(Optional.of(new StepImplementation("success", null, emptyMap(), singletonMap("result_2", "${new String(\"**letter**\").toUpperCase()"))))
                            .overrideDataSetWith(singletonMap("letter", "b"))
                            .build(),
                        ExecutableComposedStep.builder()
                            .withName("Success - should uppercase each char - iteration 3")
                            .withImplementation(Optional.of(new StepImplementation("success", null, emptyMap(), singletonMap("result_3", "${new String(\"**letter**\").toUpperCase()"))))
                            .overrideDataSetWith(singletonMap("letter", "c"))
                            .build()
                        ))
                    .build(),
                ExecutableComposedStep.builder()
                    .withName("Assert index & uppercased char")
                    .withImplementation(Optional.of(
                        new StepImplementation("assert", null, Maps.of(
                            "${#result_1}", "A",
                            "${#result_2}", "B",
                            "${#result_3}", "C"), emptyMap())))
                    .build(),
                ExecutableComposedStep.builder()
                    .withName("Context put - should create one context entry for each uppercased char")
                    .withSteps(Arrays.asList(
                        ExecutableComposedStep.builder()
                            .withName("Context put - should create one context entry for each uppercased char - iteration 1")
                            .withImplementation(Optional.of(new StepImplementation("context-put", null, singletonMap("entries", singletonMap("${#result_1}", "fake_value")), emptyMap())))
                            .build(),ExecutableComposedStep.builder()
                            .withName("Context put - should create one context entry for each uppercased char - iteration 2")
                            .withImplementation(Optional.of(new StepImplementation("context-put", null, singletonMap("entries", singletonMap("${#result_2}", "fake_value")), emptyMap())))
                            .build(),
                        ExecutableComposedStep.builder()
                            .withName("Context put - should create one context entry for each uppercased char - iteration 3")
                            .withImplementation(Optional.of(new StepImplementation("context-put", null, singletonMap("entries", singletonMap("${#result_3}", "fake_value")), emptyMap())))
                            .build()
                    ))
                    .build(),
                ExecutableComposedStep.builder()
                    .withName("Assert - each context entry contains a value")
                    .withImplementation(Optional.of(
                        new StepImplementation("assert", null, Maps.of(
                            "${#result_1}", "fake_value",
                            "${#result_2}", "fake_value",
                            "${#result_3}", "fake_value"), emptyMap())
                        ))
                    .build()
                )
            )
            .build();

        assertThat(actual.composedScenario).isEqualTo(expected);

    }

    @Test
    @Ignore
    public void should_create_indexable_iterations_2() {

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
                        .withName("Success - should uppercase & assert each char")
                        .withSteps(Arrays.asList(
                            ExecutableComposedStep.builder()
                                .withName("Success - should uppercase a char")
                                .withImplementation(Optional.of(new StepImplementation("success", null, emptyMap(), singletonMap("result", "${new String(\"**letter**\").toUpperCase()"))))
                                .overrideDataSetWith(singletonMap("letter", ""))
                                .build(),
                            ExecutableComposedStep.builder()
                                .withName("Should assert an uppercased char")
                                .withImplementation(Optional.of(new StepImplementation("assert", null, Maps.of(
                                    "${#result}", "${new String(\"**letter**\").toUpperCase()"), emptyMap())))
                                .build()
                        ))
                        .overrideDataSetWith(singletonMap("letter", ""))
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
                    .withSteps(asList(
                        ExecutableComposedStep.builder()
                            .withName("Success - should uppercase & assert each char - iteration 1")
                            .withSteps(asList(
                                ExecutableComposedStep.builder()
                                    .withName("Success - should uppercase a char")
                                    .withImplementation(Optional.of(new StepImplementation("success", null, emptyMap(), singletonMap("result", "${new String(\"**letter**\").toUpperCase()"))))
                                    .overrideDataSetWith(singletonMap("letter", "a"))
                                    .build(),
                                ExecutableComposedStep.builder()
                                    .withName("Should assert an uppercased char")
                                    .withImplementation(Optional.of(
                                        new StepImplementation("assert", null, singletonMap("${#result}", "${new String(\"**letter**\").toUpperCase()"), emptyMap())
                                    ))
                                    .overrideDataSetWith(singletonMap("letter", "a"))
                                    .build()
                            ))
                            .build(),
                        ExecutableComposedStep.builder()
                            .withName("Success - should uppercase & assert each char - iteration 2")
                            .withSteps(asList(
                                ExecutableComposedStep.builder()
                                    .withName("Success - should uppercase a char")
                                    .withImplementation(Optional.of(new StepImplementation("success", null, emptyMap(), singletonMap("result", "${new String(\"**letter**\").toUpperCase()"))))
                                    .overrideDataSetWith(singletonMap("letter", "b"))
                                    .build(),
                                ExecutableComposedStep.builder()
                                    .withName("Should assert an uppercased char")
                                    .withImplementation(Optional.of(
                                        new StepImplementation("assert", null, singletonMap("${#result}", "${new String(\"**letter**\").toUpperCase()"), emptyMap())
                                    ))
                                    .overrideDataSetWith(singletonMap("letter", "b"))
                                    .build()
                            ))
                            .build(),
                        ExecutableComposedStep.builder()
                            .withName("Success - should uppercase & assert each char - iteration 3")
                            .withSteps(asList(
                                ExecutableComposedStep.builder()
                                    .withName("Success - should uppercase a char")
                                    .withImplementation(Optional.of(new StepImplementation("success", null, emptyMap(), singletonMap("result", "${new String(\"**letter**\").toUpperCase()"))))
                                    .overrideDataSetWith(singletonMap("letter", "c"))
                                    .build(),
                                ExecutableComposedStep.builder()
                                    .withName("Should assert an uppercased char")
                                    .withImplementation(Optional.of(
                                        new StepImplementation("assert", null, singletonMap("${#result}", "${new String(\"**letter**\").toUpperCase()"), emptyMap())
                                    ))
                                    .overrideDataSetWith(singletonMap("letter", "c"))
                                    .build()
                            ))
                            .build()
                    ))
                    .overrideDataSetWith(singletonMap("letter", ""))
                    .build()
                )
            )
            .build();

        assertThat(actual.composedScenario).isEqualTo(expected);

    }

    @Test
    @Ignore
    public void should_create_indexable_iterations_3() {

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
                        .overrideDataSetWith(singletonMap("letter", ""))
                        .build(),
                    ExecutableComposedStep.builder()
                        .withName("Context put - should create one context entry for each uppercased char")
                        .withImplementation(Optional.of(
                            new StepImplementation("context-put", null, singletonMap("${#result}","fake_value"), emptyMap())
                        ))
                        .overrideDataSetWith(singletonMap("myParam", "${#result}"))
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
                    .withSteps(Arrays.asList(
                        ExecutableComposedStep.builder()
                            .withName("Success - should uppercase each char - iteration 1")
                            .withImplementation(Optional.of(
                                new StepImplementation("success", null, emptyMap(), singletonMap("result_1", "${new String(\"**letter**\").toUpperCase()"))
                            ))
                            .overrideDataSetWith(singletonMap("letter", "a"))
                            .build(),
                        ExecutableComposedStep.builder()
                            .withName("Success - should uppercase each char - iteration 2")
                            .withImplementation(Optional.of(
                                new StepImplementation("success", null, emptyMap(), singletonMap("result_2", "${new String(\"**letter**\").toUpperCase()"))
                            ))
                            .overrideDataSetWith(singletonMap("letter", "b"))
                            .build(),
                        ExecutableComposedStep.builder()
                            .withName("Success - should uppercase each char - iteration 3")
                            .withImplementation(Optional.of(
                                new StepImplementation("success", null, emptyMap(), singletonMap("result_3", "${new String(\"**letter**\").toUpperCase()"))
                            ))
                            .overrideDataSetWith(singletonMap("letter", "c"))
                            .build()
                    ))
                    .build(),
                ExecutableComposedStep.builder()
                    .withName("Context put - should create one context entry for each uppercased char")
                    .withSteps(Arrays.asList(
                        ExecutableComposedStep.builder()
                            .withName("Context put - should create one context entry for each uppercased char - iteration 1")
                            .withImplementation(Optional.of(
                                new StepImplementation("context-put", null, singletonMap("entries", singletonMap("myParam_1", "${#result_1}")), emptyMap())
                            ))
                            .build(),ExecutableComposedStep.builder()
                            .withName("Context put - should create one context entry for each uppercased char - iteration 2")
                            .withImplementation(Optional.of(
                                new StepImplementation("context-put", null, singletonMap("entries", singletonMap("myParam_2", "${#result_2}")), emptyMap())
                            ))
                        .build(),
                        ExecutableComposedStep.builder()
                            .withName("Context put - should create one context entry for each uppercased char - iteration 3")
                            .withImplementation(Optional.of(
                                new StepImplementation("context-put", null, singletonMap("entries", singletonMap("myParam_3", "${#result_3}")), emptyMap())
                            ))
                            .build()
                    ))
                    .build()
                )
            )
            .build();

        assertThat(actual.composedScenario).isEqualTo(expected);

    }

}

