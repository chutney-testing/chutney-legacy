package com.chutneytesting.execution.domain.compiler;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.Optional.ofNullable;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.chutneytesting.design.domain.compose.ComposableScenario;
import com.chutneytesting.design.domain.compose.ComposableTestCase;
import com.chutneytesting.design.domain.compose.FunctionalStep;
import com.chutneytesting.design.domain.compose.Strategy;
import com.chutneytesting.design.domain.dataset.DataSet;
import com.chutneytesting.design.domain.dataset.DataSetRepository;
import com.chutneytesting.design.domain.scenario.TestCaseMetadataImpl;
import com.chutneytesting.engine.domain.execution.strategies.DataSetIterationsStrategy;
import com.chutneytesting.engine.domain.execution.strategies.DefaultStepExecutionStrategy;
import com.chutneytesting.engine.domain.execution.strategies.RetryWithTimeOutStrategy;
import com.chutneytesting.engine.domain.execution.strategies.SoftAssertStrategy;
import com.chutneytesting.execution.domain.ExecutionRequest;
import java.util.List;
import java.util.Map;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.apache.groovy.util.Maps;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JUnitParamsRunner.class)
public class ComposableTestCaseDataSetPreProcessorTest {

    private DataSetRepository dataSetRepository;
    private String dataSetId;

    @Before
    public void setUp() {
        dataSetId = "dataSetId";
        dataSetRepository = mock(DataSetRepository.class);
    }

    private void stubDataSetrepository(Map<String, String> uniqueValues, List<Map<String, String>> multipleValues) {
        when(dataSetRepository.findById(dataSetId)).thenReturn(
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
        stubDataSetrepository(uniqueValues, null);

        Map<String, String> computedParameters = Maps.of(
            "testcase param key", "testcase param value",
            "testcase key", "testcase value"
        );

        ComposableTestCase testCase = new ComposableTestCase(
            "1",
            TestCaseMetadataImpl.builder()
                .withDatasetId(dataSetId)
                .build(),
            ComposableScenario.builder()
                .build(),
            computedParameters
        );

        // When
        ComposableTestCaseDataSetPreProcessor sut = new ComposableTestCaseDataSetPreProcessor(dataSetRepository);
        ComposableTestCase processedTestCase = sut.apply(
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
        stubDataSetrepository(null, multipleValues);

        Map<String, String> computedParameters = Maps.of(
            "testcase key for dataset", "testcase default value for dataset",
            "testcase key no dataset", "testcase value",
            "another param key", "another testcase value"
        );

        ComposableTestCase testCase = new ComposableTestCase(
            "1",
            TestCaseMetadataImpl.builder()
                .withDatasetId(dataSetId)
                .build(),
            ComposableScenario.builder()
                .withFunctionalSteps(
                    asList(
                        FunctionalStep.builder()
                            .withName("a step with no valued parameter")
                            .overrideDataSetWith(
                                Maps.of(
                                    "testcase key for dataset", "",
                                    "step 1 param", "value 1"
                                )
                            )
                            .build(),
                        FunctionalStep.builder()
                            .withName("a step with no dataset matched parameter")
                            .overrideDataSetWith(
                                Maps.of("step param", "value 2")
                            )
                            .build(),
                        FunctionalStep.builder()
                            .withName("a step with ref parameter")
                            .overrideDataSetWith(
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
        ComposableTestCaseDataSetPreProcessor sut = new ComposableTestCaseDataSetPreProcessor(dataSetRepository);
        ComposableTestCase processedTestCase = sut.apply(
            new ExecutionRequest(testCase, "env", "user")
        );

        // Then
        assertThat(processedTestCase.computedParameters).containsOnly(
            entry("testcase key no dataset", "testcase value"),
            entry("another param key", "another testcase value")
        );
        assertThat(processedTestCase.composableScenario.functionalSteps).hasSize(3);

        FunctionalStep fs = processedTestCase.composableScenario.functionalSteps.get(0);
        assertThat(fs.name).isEqualTo("a step with no valued parameter");
        assertThat(fs.dataSet).containsOnly(
            entry("testcase key no dataset", ""),
            entry("another param key", "")
        );
        assertThat(fs.steps).hasSize(3);
        assertThat(fs.steps.get(0).name).isEqualTo("a step with no valued parameter - dataset iteration 1");
        assertThat(fs.steps.get(0).dataSet).containsOnly(
            entry("testcase key for dataset", "dataset first value"),
            entry("step 1 param", "value 1")
        );
        assertThat(fs.steps.get(1).name).isEqualTo("a step with no valued parameter - dataset iteration 2");
        assertThat(fs.steps.get(1).dataSet).containsOnly(
            entry("testcase key for dataset", "dataset second value"),
            entry("step 1 param", "value 1")
        );
        assertThat(fs.steps.get(2).name).isEqualTo("a step with no valued parameter - dataset iteration 3");
        assertThat(fs.steps.get(2).dataSet).containsOnly(
            entry("testcase key for dataset", "dataset third value"),
            entry("step 1 param", "value 1")
        );

        fs = processedTestCase.composableScenario.functionalSteps.get(1);
        assertThat(fs.name).isEqualTo("a step with no dataset matched parameter");
        assertThat(fs.dataSet).containsOnly(
            entry("step param", "value 2")
        );
        assertThat(fs.steps).hasSize(0);

        fs = processedTestCase.composableScenario.functionalSteps.get(2);
        assertThat(fs.name).isEqualTo("a step with ref parameter");
        assertThat(fs.dataSet).containsOnly(
            entry("another param key", ""),
            entry("testcase key no dataset", "")
        );
        assertThat(fs.steps).hasSize(3);
        assertThat(fs.steps.get(0).name).isEqualTo("a step with ref parameter - dataset iteration 1");
        assertThat(fs.steps.get(0).dataSet).containsOnly(
            entry("ref step param", "value with ref dataset first value"),
            entry("step 3 alias", "**another param key**")
        );
        assertThat(fs.steps.get(1).name).isEqualTo("a step with ref parameter - dataset iteration 2");
        assertThat(fs.steps.get(1).dataSet).containsOnly(
            entry("ref step param", "value with ref dataset second value"),
            entry("step 3 alias", "**another param key**")
        );
        assertThat(fs.steps.get(2).name).isEqualTo("a step with ref parameter - dataset iteration 3");
        assertThat(fs.steps.get(2).dataSet).containsOnly(
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
        stubDataSetrepository(null, multipleValues);

        Map<String, String> computedParameters = Maps.of(
            "testcase key for dataset", "testcase default value for dataset"
        );

        ComposableTestCase testCase = new ComposableTestCase(
            "1",
            TestCaseMetadataImpl.builder().withDatasetId(dataSetId).build(),
            ComposableScenario.builder()
                .withFunctionalSteps(
                    singletonList(
                        FunctionalStep.builder()
                            .withName("a step with no valued parameter")
                            .overrideDataSetWith(
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
        ComposableTestCaseDataSetPreProcessor sut = new ComposableTestCaseDataSetPreProcessor(dataSetRepository);
        ComposableTestCase processedTestCase = sut.apply(
            new ExecutionRequest(testCase, "env", "user")
        );

        // Then
        assertThat(processedTestCase.composableScenario.functionalSteps.get(0).strategy).isEqualTo(
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
        stubDataSetrepository(null, multipleValues);

        Map<String, String> computedParameters = Maps.of(
            "testcase key for dataset", "testcase default value for dataset"
        );

        ComposableTestCase testCase = new ComposableTestCase(
            "1",
            TestCaseMetadataImpl.builder().withDatasetId(dataSetId).build(),
            ComposableScenario.builder()
                .withFunctionalSteps(
                    singletonList(
                        FunctionalStep.builder()
                            .withStrategy(strategyDefinition)
                            .withName("a step with no valued parameter")
                            .overrideDataSetWith(
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
        ComposableTestCaseDataSetPreProcessor sut = new ComposableTestCaseDataSetPreProcessor(dataSetRepository);
        ComposableTestCase processedTestCase = sut.apply(
            new ExecutionRequest(testCase, "env", "user")
        );

        // Then
        assertThat(processedTestCase.composableScenario.functionalSteps.get(0).steps.get(0).strategy).isEqualTo(
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
        stubDataSetrepository(null, multipleValues);

        Map<String, String> computedParameters = Maps.of(
            "key 1", "testcase value for key 1"
        );

        ComposableTestCase testCase = new ComposableTestCase(
            "1",
            TestCaseMetadataImpl.builder()
                .withDatasetId(dataSetId)
                .build(),
            ComposableScenario.builder()
                .withFunctionalSteps(
                    singletonList(
                        FunctionalStep.builder()
                            .withName("a step with key 1 dependency")
                            .overrideDataSetWith(
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
        ComposableTestCaseDataSetPreProcessor sut = new ComposableTestCaseDataSetPreProcessor(dataSetRepository);
        ComposableTestCase processedTestCase = sut.apply(
            new ExecutionRequest(testCase, "env", "user")
        );

        // Then
        assertThat(processedTestCase.composableScenario.functionalSteps).hasSize(1);

        FunctionalStep fs = processedTestCase.composableScenario.functionalSteps.get(0);
        assertThat(fs.name).isEqualTo("a step with key 1 dependency");
        assertThat(fs.dataSet).isEmpty();
        assertThat(fs.steps).hasSize(2);
        assertThat(fs.steps.get(0).name).isEqualTo("a step with key 1 dependency - dataset iteration 1");
        assertThat(fs.steps.get(0).dataSet).containsOnly(
            entry("key 1", "value 1")
        );
        assertThat(fs.steps.get(1).name).isEqualTo("a step with key 1 dependency - dataset iteration 2");
        assertThat(fs.steps.get(1).dataSet).containsOnly(
            entry("key 1", "value 12")
        );
    }
}
