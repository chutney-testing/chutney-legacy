package com.chutneytesting.execution.domain.compiler;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import com.chutneytesting.WebConfiguration;
import com.chutneytesting.design.domain.compose.ComposableScenario;
import com.chutneytesting.design.domain.compose.ComposableTestCase;
import com.chutneytesting.design.domain.compose.FunctionalStep;
import com.chutneytesting.design.domain.compose.Strategy;
import com.chutneytesting.design.domain.scenario.TestCaseMetadataImpl;
import com.chutneytesting.execution.domain.ExecutionRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import org.apache.groovy.util.Maps;
import org.junit.Test;

public class ComposableTestCaseLoopPreProcessorTest {

    private final ObjectMapper objectMapper = new WebConfiguration().objectMapper();

    @Test
    public void should_create_one_iteration_and_set_parameter_value_to_zogzog_When_parameter_has_no_value() {
        ComposableTestCaseLoopPreProcessor sut = new ComposableTestCaseLoopPreProcessor(objectMapper);

        // Given
        Map<String, String> parameter = singletonMap("P", /*no default value*/ "");
        Strategy strategy = new Strategy("Loop", singletonMap("data", "[{\"P\":\"zog_zog\"}]"));

        FunctionalStep step = FunctionalStep.builder()
            .overrideDataSetWith(parameter)
            .withStrategy(strategy)
            .build();

        ComposableScenario composableScenario = ComposableScenario.builder()
            .withFunctionalSteps(singletonList(step))
            .build();

        ComposableTestCase composableTestCase = new ComposableTestCase("0", TestCaseMetadataImpl.builder().build(), composableScenario);

        // When
        ComposableTestCase actual = sut.apply(
            new ExecutionRequest(composableTestCase, "env", "user")
        );

        // Then
        assertThat(actual.composableScenario.functionalSteps).hasSize(1);
        assertThat(actual.composableScenario.functionalSteps.get(0).steps).hasSize(1);
        assertThat(actual.composableScenario.functionalSteps.get(0).steps.get(0).dataSet).containsEntry("P", "zog_zog");
    }

    @Test
    public void should_create_one_iteration_and_set_parameter_value_to_zogzog_When_component_has_no_parameter() {
        ComposableTestCaseLoopPreProcessor sut = new ComposableTestCaseLoopPreProcessor(objectMapper);

        // Given
        Strategy strategy = new Strategy("Loop", singletonMap("data", "[{\"P\":\"zog_zog\"}]"));

        FunctionalStep step = FunctionalStep.builder()
            .overrideDataSetWith(emptyMap())
            .withStrategy(strategy)
            .build();

        ComposableScenario composableScenario = ComposableScenario.builder()
            .withFunctionalSteps(singletonList(step))
            .build();

        ComposableTestCase composableTestCase = new ComposableTestCase("0", TestCaseMetadataImpl.builder().build(), composableScenario);

        // When
        ComposableTestCase actual = sut.apply(
            new ExecutionRequest(composableTestCase, "env", "user")
        );

        // Then
        assertThat(actual.composableScenario.functionalSteps).hasSize(1);
        assertThat(actual.composableScenario.functionalSteps.get(0).steps).hasSize(1);
        assertThat(actual.composableScenario.functionalSteps.get(0).steps.get(0).dataSet).containsEntry("P", "zog_zog");
    }

    @Test
    public void should_create_one_iteration_and_keep_parameter_default_value_When_strategy_has_no_data() {
        ComposableTestCaseLoopPreProcessor sut = new ComposableTestCaseLoopPreProcessor(objectMapper);

        // Given
        Map<String, String> parameter = singletonMap("P", "default value");
        Strategy strategy = new Strategy("Loop", emptyMap());

        FunctionalStep step = FunctionalStep.builder()
            .overrideDataSetWith(parameter)
            .withStrategy(strategy)
            .build();

        ComposableScenario composableScenario = ComposableScenario.builder()
            .withFunctionalSteps(singletonList(step))
            .build();

        ComposableTestCase composableTestCase = new ComposableTestCase("0", TestCaseMetadataImpl.builder().build(), composableScenario);

        // When
        ComposableTestCase actual = sut.apply(
            new ExecutionRequest(composableTestCase, "env", "user")
        );

        // Then
        assertThat(actual.composableScenario.functionalSteps).hasSize(1);
        assertThat(actual.composableScenario.functionalSteps.get(0).steps).hasSize(1);
        assertThat(actual.composableScenario.functionalSteps.get(0).steps.get(0).dataSet).containsEntry("P", "default value");
    }

    @Test
    public void should_create_one_iteration_and_override_parameter_value_to_zogzog_When_parameter_has_default_value() {
        ComposableTestCaseLoopPreProcessor sut = new ComposableTestCaseLoopPreProcessor(objectMapper);

        // Given
        Map<String, String> parameter = singletonMap("P", "default_value");
        Strategy strategy = new Strategy("Loop", singletonMap("data", "[{\"P\":\"zog_zog\"}]"));

        FunctionalStep step = FunctionalStep.builder()
            .overrideDataSetWith(parameter)
            .withStrategy(strategy)
            .build();

        ComposableScenario composableScenario = ComposableScenario.builder()
            .withFunctionalSteps(singletonList(step))
            .build();

        ComposableTestCase composableTestCase = new ComposableTestCase("0", TestCaseMetadataImpl.builder().build(), composableScenario);

        // When
        ComposableTestCase actual = sut.apply(
            new ExecutionRequest(composableTestCase, "env", "user")
        );

        // Then
        assertThat(actual.composableScenario.functionalSteps).hasSize(1);
        assertThat(actual.composableScenario.functionalSteps.get(0).steps).hasSize(1);
        assertThat(actual.composableScenario.functionalSteps.get(0).steps.get(0).dataSet).containsEntry("P", "zog_zog");
    }

    @Test
    public void should_create_one_iteration_and_keep_parameter_default_value_When_strategy_has_no_key_matching_parameter_key() {
        ComposableTestCaseLoopPreProcessor sut = new ComposableTestCaseLoopPreProcessor(objectMapper);

        // Given
        Map<String, String> parameter = singletonMap("P", "default_value");
        Strategy strategy = new Strategy("Loop", singletonMap("data", "[{\"X\":\"zog_zog\"}]"));

        FunctionalStep step = FunctionalStep.builder()
            .overrideDataSetWith(parameter)
            .withStrategy(strategy)
            .build();

        ComposableScenario composableScenario = ComposableScenario.builder()
            .withFunctionalSteps(singletonList(step))
            .build();

        ComposableTestCase composableTestCase = new ComposableTestCase("0", TestCaseMetadataImpl.builder().build(), composableScenario);

        // When
        ComposableTestCase actual = sut.apply(
            new ExecutionRequest(composableTestCase, "env", "user")
        );

        // Then
        assertThat(actual.composableScenario.functionalSteps).hasSize(1);
        assertThat(actual.composableScenario.functionalSteps.get(0).steps).hasSize(1);
        assertThat(actual.composableScenario.functionalSteps.get(0).steps.get(0).dataSet).containsEntry("P", "default_value");

    }

    @Test
    public void should_create_one_iteration_and_add_one_parameter_When_step_has_no_parameter_matching_iteration_param() {
        ComposableTestCaseLoopPreProcessor sut = new ComposableTestCaseLoopPreProcessor(objectMapper);

        // Given
        Strategy strategy = new Strategy("Loop", singletonMap("data", "[{\"X\":\"zog_zog\"}]"));

        FunctionalStep step = FunctionalStep.builder()
            .overrideDataSetWith(emptyMap())
            .withStrategy(strategy)
            .build();

        ComposableScenario composableScenario = ComposableScenario.builder()
            .withFunctionalSteps(singletonList(step))
            .build();

        ComposableTestCase composableTestCase = new ComposableTestCase("0", TestCaseMetadataImpl.builder().build(), composableScenario);

        // When
        ComposableTestCase actual = sut.apply(
            new ExecutionRequest(composableTestCase, "env", "user")
        );

        // Then
        assertThat(actual.composableScenario.functionalSteps).hasSize(1);
        assertThat(actual.composableScenario.functionalSteps.get(0).steps).hasSize(1);
        assertThat(actual.composableScenario.functionalSteps.get(0).steps.get(0).dataSet).containsOnly(new HashMap.SimpleEntry("X", "zog_zog"));

    }

    @Test
    public void should_create_two_iterations_and_override_parameter_value_When_strategy_has_two_iterations() {
        ComposableTestCaseLoopPreProcessor sut = new ComposableTestCaseLoopPreProcessor(objectMapper);

        // Given
        Map<String, String> parameter = singletonMap("P", "default_value");
        Strategy strategy = new Strategy("Loop", singletonMap("data", "[{\"P\":\"dabu\"},{\"P\":\"zog_zog\"} ]"));

        FunctionalStep step = FunctionalStep.builder()
            .overrideDataSetWith(parameter)
            .withStrategy(strategy)
            .build();

        ComposableScenario composableScenario = ComposableScenario.builder()
            .withFunctionalSteps(singletonList(step))
            .build();

        ComposableTestCase composableTestCase = new ComposableTestCase("0", TestCaseMetadataImpl.builder().build(), composableScenario);

        // When
        ComposableTestCase actual = sut.apply(
            new ExecutionRequest(composableTestCase, "env", "user")
        );

        // Then
        assertThat(actual.composableScenario.functionalSteps).hasSize(1);
        assertThat(actual.composableScenario.functionalSteps.get(0).steps).hasSize(2);
        assertThat(actual.composableScenario.functionalSteps.get(0).steps.get(0).dataSet).containsEntry("P", "dabu");
        assertThat(actual.composableScenario.functionalSteps.get(0).steps.get(1).dataSet).containsEntry("P", "zog_zog");
    }

    @Test
    public void should_create_two_iterations_and_override_parameters_values_for_all_matching_parameters() {
        ComposableTestCaseLoopPreProcessor sut = new ComposableTestCaseLoopPreProcessor(objectMapper);

        // Given
        Map<String, String> parameter = Maps.of("P_1", "default_value", "P_2", "");
        Strategy strategy = new Strategy("Loop", singletonMap("data", "[ {\"P_1\":\"dabu\",\"P_2\":\"zog_zog\"}, {\"P_2\":\"goz_goz\"} ]"));

        FunctionalStep step = FunctionalStep.builder()
            .overrideDataSetWith(parameter)
            .withStrategy(strategy)
            .build();

        ComposableScenario composableScenario = ComposableScenario.builder()
            .withFunctionalSteps(singletonList(step))
            .build();

        ComposableTestCase composableTestCase = new ComposableTestCase("0", TestCaseMetadataImpl.builder().build(), composableScenario);

        // When
        ComposableTestCase actual = sut.apply(
            new ExecutionRequest(composableTestCase, "env", "user")
        );

        // Then
        assertThat(actual.composableScenario.functionalSteps).hasSize(1);
        assertThat(actual.composableScenario.functionalSteps.get(0).steps).hasSize(2);
        assertThat(actual.composableScenario.functionalSteps.get(0).steps.get(0).dataSet).contains(entry("P_1", "dabu"), entry("P_2", "zog_zog"));
        assertThat(actual.composableScenario.functionalSteps.get(0).steps.get(1).dataSet).contains(entry("P_1", "default_value"), entry("P_2", "goz_goz"));
    }

    @Test
    public void should_create_one_iteration_for_substeps_using_loop_strategy() {
        ComposableTestCaseLoopPreProcessor sut = new ComposableTestCaseLoopPreProcessor(objectMapper);

        // Given
        Map<String, String> parameter = singletonMap("P", "");
        Strategy strategy = new Strategy("Loop", singletonMap("data", "[{\"P\":\"zog_zog\"}]"));

        FunctionalStep substep = FunctionalStep.builder()
            .withName("substep with loop strategy")
            .overrideDataSetWith(parameter)
            .withStrategy(strategy)
            .build();

        FunctionalStep parentStep = FunctionalStep.builder()
            .withName("parent with default strategy")
            .withSteps(singletonList(substep))
            .build();

        ComposableScenario composableScenario = ComposableScenario.builder()
            .withFunctionalSteps(singletonList(parentStep))
            .build();

        ComposableTestCase composableTestCase = new ComposableTestCase("0", TestCaseMetadataImpl.builder().build(), composableScenario);

        // When
        ComposableTestCase actual = sut.apply(
            new ExecutionRequest(composableTestCase, "env", "user")
        );

        // Then

        FunctionalStep actualParentStep = actual.composableScenario.functionalSteps.get(0);
        FunctionalStep actualSubStep = actualParentStep.steps.get(0);

        assertThat(actualSubStep.steps).hasSize(1);
        assertThat(actualSubStep.steps.get(0).dataSet).containsEntry("P", "zog_zog");
        assertThat(actualSubStep.steps.get(0).strategy).isEqualTo(Strategy.DEFAULT);
    }

    @Test
    public void should_name_iteration_with_parent_name_and_index_counter() {
        ComposableTestCaseLoopPreProcessor sut = new ComposableTestCaseLoopPreProcessor(objectMapper);

        // Given
        Map<String, String> parameter = Maps.of("P_1", "", "P_2", "");
        Strategy strategy = new Strategy("Loop", singletonMap("data", "[ {}, {} ]"));

        FunctionalStep step = FunctionalStep.builder()
            .withName("fake_name")
            .overrideDataSetWith(parameter)
            .withStrategy(strategy)
            .build();

        ComposableScenario composableScenario = ComposableScenario.builder()
            .withFunctionalSteps(singletonList(step))
            .build();

        ComposableTestCase composableTestCase = new ComposableTestCase("0", TestCaseMetadataImpl.builder().build(), composableScenario);

        // When
        ComposableTestCase actual = sut.apply(
            new ExecutionRequest(composableTestCase, "env", "user")
        );

        // Then
        assertThat(actual.composableScenario.functionalSteps).hasSize(1);
        assertThat(actual.composableScenario.functionalSteps.get(0).steps).hasSize(2);
        assertThat(actual.composableScenario.functionalSteps.get(0).steps.get(0).name).isEqualToIgnoringCase("fake_name - iteration 1");
        assertThat(actual.composableScenario.functionalSteps.get(0).steps.get(1).name).isEqualToIgnoringCase("fake_name - iteration 2");
    }

    @Test
    public void should_remove_loop_strategy_on_parent() {
        ComposableTestCaseLoopPreProcessor sut = new ComposableTestCaseLoopPreProcessor(objectMapper);

        // Given
        Map<String, String> parameter = Maps.of("P", "");
        Strategy strategy = new Strategy("Loop", singletonMap("data", "[ {} ]"));

        FunctionalStep step = FunctionalStep.builder()
            .overrideDataSetWith(parameter)
            .withStrategy(strategy)
            .build();

        ComposableScenario composableScenario = ComposableScenario.builder()
            .withFunctionalSteps(singletonList(step))
            .build();

        ComposableTestCase composableTestCase = new ComposableTestCase("0", TestCaseMetadataImpl.builder().build(), composableScenario);

        // When
        ComposableTestCase actual = sut.apply(
            new ExecutionRequest(composableTestCase, "env", "user")
        );

        // Then
        assertThat(actual.composableScenario.functionalSteps).hasSize(1);
        assertThat(actual.composableScenario.functionalSteps.get(0).steps).hasSize(1);
        assertThat(actual.composableScenario.functionalSteps.get(0).strategy).isEqualTo(Strategy.DEFAULT);
    }

}
