package com.chutneytesting.execution.domain.compiler;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import com.chutneytesting.WebConfiguration;
import com.chutneytesting.design.domain.scenario.TestCaseMetadataImpl;
import com.chutneytesting.design.domain.scenario.compose.Strategy;
import com.chutneytesting.execution.domain.ExecutionRequest;
import com.chutneytesting.execution.domain.scenario.composed.ExecutableComposedScenario;
import com.chutneytesting.execution.domain.scenario.composed.ExecutableComposedStep;
import com.chutneytesting.execution.domain.scenario.composed.ExecutableComposedTestCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import org.apache.groovy.util.Maps;
import org.junit.Test;

public class ComposedTestCaseLoopPreProcessorTest {

    private final ObjectMapper objectMapper = new WebConfiguration().objectMapper();

    @Test
    public void should_create_one_iteration_and_set_parameter_value_to_zogzog_When_parameter_has_no_value() {
        ComposedTestCaseLoopPreProcessor sut = new ComposedTestCaseLoopPreProcessor(objectMapper);

        // Given
        Map<String, String> parameter = singletonMap("P", /*no default value*/ "");
        Strategy strategy = new Strategy("Loop", singletonMap("data", "[{\"P\":\"zog_zog\"}]"));

        ExecutableComposedStep step = ExecutableComposedStep.builder()
            .withDataset(parameter)
            .withStrategy(strategy)
            .build();

        ExecutableComposedScenario composableScenario = ExecutableComposedScenario.builder()
            .withComposedSteps(singletonList(step))
            .build();

        ExecutableComposedTestCase composedTestCase = new ExecutableComposedTestCase(TestCaseMetadataImpl.builder().build(), composableScenario);

        // When
        ExecutableComposedTestCase actual = sut.apply(
            new ExecutionRequest(composedTestCase, "env", "user")
        );

        // Then
        assertThat(actual.composedScenario.composedSteps).hasSize(1);
        assertThat(actual.composedScenario.composedSteps.get(0).steps).hasSize(1);
        assertThat(actual.composedScenario.composedSteps.get(0).steps.get(0).dataset).containsEntry("P", "zog_zog");
    }

    @Test
    public void should_create_one_iteration_and_set_parameter_value_to_zogzog_When_component_has_no_parameter() {
        ComposedTestCaseLoopPreProcessor sut = new ComposedTestCaseLoopPreProcessor(objectMapper);

        // Given
        Strategy strategy = new Strategy("Loop", singletonMap("data", "[{\"P\":\"zog_zog\"}]"));

        ExecutableComposedStep step = ExecutableComposedStep.builder()
            .withDataset(emptyMap())
            .withStrategy(strategy)
            .build();

        ExecutableComposedScenario composableScenario = ExecutableComposedScenario.builder()
            .withComposedSteps(singletonList(step))
            .build();

        ExecutableComposedTestCase composedTestCase = new ExecutableComposedTestCase(TestCaseMetadataImpl.builder().build(), composableScenario);

        // When
        ExecutableComposedTestCase actual = sut.apply(
            new ExecutionRequest(composedTestCase, "env", "user")
        );

        // Then
        assertThat(actual.composedScenario.composedSteps).hasSize(1);
        assertThat(actual.composedScenario.composedSteps.get(0).steps).hasSize(1);
        assertThat(actual.composedScenario.composedSteps.get(0).steps.get(0).dataset).containsEntry("P", "zog_zog");
    }

    @Test
    public void should_create_one_iteration_and_keep_parameter_default_value_When_strategy_has_no_data() {
        ComposedTestCaseLoopPreProcessor sut = new ComposedTestCaseLoopPreProcessor(objectMapper);

        // Given
        Map<String, String> parameter = singletonMap("P", "default value");
        Strategy strategy = new Strategy("Loop", emptyMap());

        ExecutableComposedStep step = ExecutableComposedStep.builder()
            .withDataset(parameter)
            .withStrategy(strategy)
            .build();

        ExecutableComposedScenario composableScenario = ExecutableComposedScenario.builder()
            .withComposedSteps(singletonList(step))
            .build();

        ExecutableComposedTestCase composedTestCase = new ExecutableComposedTestCase(TestCaseMetadataImpl.builder().build(), composableScenario);

        // When
        ExecutableComposedTestCase actual = sut.apply(
            new ExecutionRequest(composedTestCase, "env", "user")
        );

        // Then
        assertThat(actual.composedScenario.composedSteps).hasSize(1);
        assertThat(actual.composedScenario.composedSteps.get(0).steps).hasSize(1);
        assertThat(actual.composedScenario.composedSteps.get(0).steps.get(0).dataset).containsEntry("P", "default value");
    }

    @Test
    public void should_create_one_iteration_and_override_parameter_value_to_zogzog_When_parameter_has_default_value() {
        ComposedTestCaseLoopPreProcessor sut = new ComposedTestCaseLoopPreProcessor(objectMapper);

        // Given
        Map<String, String> parameter = singletonMap("P", "default_value");
        Strategy strategy = new Strategy("Loop", singletonMap("data", "[{\"P\":\"zog_zog\"}]"));

        ExecutableComposedStep step = ExecutableComposedStep.builder()
            .withDataset(parameter)
            .withStrategy(strategy)
            .build();

        ExecutableComposedScenario composableScenario = ExecutableComposedScenario.builder()
            .withComposedSteps(singletonList(step))
            .build();

        ExecutableComposedTestCase composedTestCase = new ExecutableComposedTestCase(TestCaseMetadataImpl.builder().build(), composableScenario);

        // When
        ExecutableComposedTestCase actual = sut.apply(
            new ExecutionRequest(composedTestCase, "env", "user")
        );

        // Then
        assertThat(actual.composedScenario.composedSteps).hasSize(1);
        assertThat(actual.composedScenario.composedSteps.get(0).steps).hasSize(1);
        assertThat(actual.composedScenario.composedSteps.get(0).steps.get(0).dataset).containsEntry("P", "zog_zog");
    }

    @Test
    public void should_create_one_iteration_and_keep_parameter_default_value_When_strategy_has_no_key_matching_parameter_key() {
        ComposedTestCaseLoopPreProcessor sut = new ComposedTestCaseLoopPreProcessor(objectMapper);

        // Given
        Map<String, String> parameter = singletonMap("P", "default_value");
        Strategy strategy = new Strategy("Loop", singletonMap("data", "[{\"X\":\"zog_zog\"}]"));

        ExecutableComposedStep step = ExecutableComposedStep.builder()
            .withDataset(parameter)
            .withStrategy(strategy)
            .build();

        ExecutableComposedScenario composableScenario = ExecutableComposedScenario.builder()
            .withComposedSteps(singletonList(step))
            .build();

        ExecutableComposedTestCase composedTestCase = new ExecutableComposedTestCase(TestCaseMetadataImpl.builder().build(), composableScenario);

        // When
        ExecutableComposedTestCase actual = sut.apply(
            new ExecutionRequest(composedTestCase, "env", "user")
        );

        // Then
        assertThat(actual.composedScenario.composedSteps).hasSize(1);
        assertThat(actual.composedScenario.composedSteps.get(0).steps).hasSize(1);
        assertThat(actual.composedScenario.composedSteps.get(0).steps.get(0).dataset).containsEntry("P", "default_value");

    }

    @Test
    public void should_create_one_iteration_and_add_one_parameter_When_step_has_no_parameter_matching_iteration_param() {
        ComposedTestCaseLoopPreProcessor sut = new ComposedTestCaseLoopPreProcessor(objectMapper);

        // Given
        Strategy strategy = new Strategy("Loop", singletonMap("data", "[{\"X\":\"zog_zog\"}]"));

        ExecutableComposedStep step = ExecutableComposedStep.builder()
            .withDataset(emptyMap())
            .withStrategy(strategy)
            .build();

        ExecutableComposedScenario composableScenario = ExecutableComposedScenario.builder()
            .withComposedSteps(singletonList(step))
            .build();

        ExecutableComposedTestCase composedTestCase = new ExecutableComposedTestCase(TestCaseMetadataImpl.builder().build(), composableScenario);

        // When
        ExecutableComposedTestCase actual = sut.apply(
            new ExecutionRequest(composedTestCase, "env", "user")
        );

        // Then
        assertThat(actual.composedScenario.composedSteps).hasSize(1);
        assertThat(actual.composedScenario.composedSteps.get(0).steps).hasSize(1);
        assertThat(actual.composedScenario.composedSteps.get(0).steps.get(0).dataset).containsOnly(new HashMap.SimpleEntry("X", "zog_zog"));

    }

    @Test
    public void should_create_two_iterations_and_override_parameter_value_When_strategy_has_two_iterations() {
        ComposedTestCaseLoopPreProcessor sut = new ComposedTestCaseLoopPreProcessor(objectMapper);

        // Given
        Map<String, String> parameter = singletonMap("P", "default_value");
        Strategy strategy = new Strategy("Loop", singletonMap("data", "[{\"P\":\"dabu\"},{\"P\":\"zog_zog\"} ]"));

        ExecutableComposedStep step = ExecutableComposedStep.builder()
            .withDataset(parameter)
            .withStrategy(strategy)
            .build();

        ExecutableComposedScenario composableScenario = ExecutableComposedScenario.builder()
            .withComposedSteps(singletonList(step))
            .build();

        ExecutableComposedTestCase composedTestCase = new ExecutableComposedTestCase(TestCaseMetadataImpl.builder().build(), composableScenario);

        // When
        ExecutableComposedTestCase actual = sut.apply(
            new ExecutionRequest(composedTestCase, "env", "user")
        );

        // Then
        assertThat(actual.composedScenario.composedSteps).hasSize(1);
        assertThat(actual.composedScenario.composedSteps.get(0).steps).hasSize(2);
        assertThat(actual.composedScenario.composedSteps.get(0).steps.get(0).dataset).containsEntry("P", "dabu");
        assertThat(actual.composedScenario.composedSteps.get(0).steps.get(1).dataset).containsEntry("P", "zog_zog");
    }

    @Test
    public void should_create_two_iterations_and_override_parameters_values_for_all_matching_parameters() {
        ComposedTestCaseLoopPreProcessor sut = new ComposedTestCaseLoopPreProcessor(objectMapper);

        // Given
        Map<String, String> parameter = Maps.of("P_1", "default_value", "P_2", "");
        Strategy strategy = new Strategy("Loop", singletonMap("data", "[ {\"P_1\":\"dabu\",\"P_2\":\"zog_zog\"}, {\"P_2\":\"goz_goz\"} ]"));

        ExecutableComposedStep step = ExecutableComposedStep.builder()
            .withDataset(parameter)
            .withStrategy(strategy)
            .build();

        ExecutableComposedScenario composableScenario = ExecutableComposedScenario.builder()
            .withComposedSteps(singletonList(step))
            .build();

        ExecutableComposedTestCase composedTestCase = new ExecutableComposedTestCase(TestCaseMetadataImpl.builder().build(), composableScenario);

        // When
        ExecutableComposedTestCase actual = sut.apply(
            new ExecutionRequest(composedTestCase, "env", "user")
        );

        // Then
        assertThat(actual.composedScenario.composedSteps).hasSize(1);
        assertThat(actual.composedScenario.composedSteps.get(0).steps).hasSize(2);
        assertThat(actual.composedScenario.composedSteps.get(0).steps.get(0).dataset).contains(entry("P_1", "dabu"), entry("P_2", "zog_zog"));
        assertThat(actual.composedScenario.composedSteps.get(0).steps.get(1).dataset).contains(entry("P_1", "default_value"), entry("P_2", "goz_goz"));
    }

    @Test
    public void should_create_one_iteration_for_substeps_using_loop_strategy() {
        ComposedTestCaseLoopPreProcessor sut = new ComposedTestCaseLoopPreProcessor(objectMapper);

        // Given
        Map<String, String> parameter = singletonMap("P", "");
        Strategy strategy = new Strategy("Loop", singletonMap("data", "[{\"P\":\"zog_zog\"}]"));

        ExecutableComposedStep substep = ExecutableComposedStep.builder()
            .withName("substep with loop strategy")
            .withDataset(parameter)
            .withStrategy(strategy)
            .build();

        ExecutableComposedStep parentStep = ExecutableComposedStep.builder()
            .withName("parent with default strategy")
            .withSteps(singletonList(substep))
            .build();

        ExecutableComposedScenario composableScenario = ExecutableComposedScenario.builder()
            .withComposedSteps(singletonList(parentStep))
            .build();

        ExecutableComposedTestCase composedTestCase = new ExecutableComposedTestCase(TestCaseMetadataImpl.builder().build(), composableScenario);

        // When
        ExecutableComposedTestCase actual = sut.apply(
            new ExecutionRequest(composedTestCase, "env", "user")
        );

        // Then

        ExecutableComposedStep actualParentStep = actual.composedScenario.composedSteps.get(0);
        ExecutableComposedStep actualSubStep = actualParentStep.steps.get(0);

        assertThat(actualSubStep.steps).hasSize(1);
        assertThat(actualSubStep.steps.get(0).dataset).containsEntry("P", "zog_zog");
        assertThat(actualSubStep.steps.get(0).strategy).isEqualTo(Strategy.DEFAULT);
    }

    @Test
    public void should_name_iteration_with_parent_name_and_index_counter() {
        ComposedTestCaseLoopPreProcessor sut = new ComposedTestCaseLoopPreProcessor(objectMapper);

        // Given
        Map<String, String> parameter = Maps.of("P_1", "", "P_2", "");
        Strategy strategy = new Strategy("Loop", singletonMap("data", "[ {}, {} ]"));

        ExecutableComposedStep step = ExecutableComposedStep.builder()
            .withName("fake_name")
            .withDataset(parameter)
            .withStrategy(strategy)
            .build();

        ExecutableComposedScenario composableScenario = ExecutableComposedScenario.builder()
            .withComposedSteps(singletonList(step))
            .build();

        ExecutableComposedTestCase composedTestCase = new ExecutableComposedTestCase(TestCaseMetadataImpl.builder().build(), composableScenario);

        // When
        ExecutableComposedTestCase actual = sut.apply(
            new ExecutionRequest(composedTestCase, "env", "user")
        );

        // Then
        assertThat(actual.composedScenario.composedSteps).hasSize(1);
        assertThat(actual.composedScenario.composedSteps.get(0).steps).hasSize(2);
        assertThat(actual.composedScenario.composedSteps.get(0).steps.get(0).name).isEqualToIgnoringCase("fake_name - iteration 1");
        assertThat(actual.composedScenario.composedSteps.get(0).steps.get(1).name).isEqualToIgnoringCase("fake_name - iteration 2");
    }

    @Test
    public void should_remove_loop_strategy_on_parent() {
        ComposedTestCaseLoopPreProcessor sut = new ComposedTestCaseLoopPreProcessor(objectMapper);

        // Given
        Map<String, String> parameter = Maps.of("P", "");
        Strategy strategy = new Strategy("Loop", singletonMap("data", "[ {} ]"));

        ExecutableComposedStep step = ExecutableComposedStep.builder()
            .withDataset(parameter)
            .withStrategy(strategy)
            .build();

        ExecutableComposedScenario composableScenario = ExecutableComposedScenario.builder()
            .withComposedSteps(singletonList(step))
            .build();

        ExecutableComposedTestCase composedTestCase = new ExecutableComposedTestCase(TestCaseMetadataImpl.builder().build(), composableScenario);

        // When
        ExecutableComposedTestCase actual = sut.apply(
            new ExecutionRequest(composedTestCase, "env", "user")
        );

        // Then
        assertThat(actual.composedScenario.composedSteps).hasSize(1);
        assertThat(actual.composedScenario.composedSteps.get(0).steps).hasSize(1);
        assertThat(actual.composedScenario.composedSteps.get(0).strategy).isEqualTo(Strategy.DEFAULT);
    }

}
