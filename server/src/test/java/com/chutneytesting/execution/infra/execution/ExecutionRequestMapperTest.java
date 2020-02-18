package com.chutneytesting.execution.infra.execution;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import com.chutneytesting.agent.domain.explore.CurrentNetworkDescription;
import com.chutneytesting.design.domain.compose.ComposableScenario;
import com.chutneytesting.design.domain.compose.ComposableTestCase;
import com.chutneytesting.design.domain.compose.FunctionalStep;
import com.chutneytesting.design.domain.environment.EnvironmentRepository;
import com.chutneytesting.design.domain.environment.Target;
import com.chutneytesting.design.domain.scenario.TestCaseMetadataImpl;
import com.chutneytesting.design.domain.scenario.raw.RawTestCase;
import com.chutneytesting.engine.api.execution.ExecutionRequestDto;
import com.chutneytesting.engine.api.execution.TargetDto;
import com.chutneytesting.engine.domain.environment.SecurityInfo;
import com.chutneytesting.execution.domain.ExecutionRequest;
import com.chutneytesting.task.api.EmbeddedTaskEngine;
import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.groovy.util.Maps;
import org.assertj.core.util.Files;
import org.junit.Test;

public class ExecutionRequestMapperTest {

    private ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    private EnvironmentRepository environmentRepository = mock(EnvironmentRepository.class);
    private CurrentNetworkDescription currentNetworkDescription = mock(CurrentNetworkDescription.class);
    private EmbeddedTaskEngine embeddedTaskEngine = mock(EmbeddedTaskEngine.class);

    private ExecutionRequestMapper sut = new ExecutionRequestMapper(objectMapper, environmentRepository, currentNetworkDescription, embeddedTaskEngine);

    @Test
    public void should_map_test_case_to_execution_request() {
        // Given
        RawTestCase testCase = RawTestCase.builder()
            .withScenario(Files.contentOf(new File(Resources.getResource("raw_scenarios/scenario.json").getPath()), Charset.forName("UTF-8")))
            .build();
        ExecutionRequest request = new ExecutionRequest(testCase, "");

        // When
        ExecutionRequestDto executionRequestDto = sut.toDto(request);

        // Then
        assertThat(executionRequestDto.scenario).isNotNull();
        assertThat(executionRequestDto.scenario.name).isEqualTo("root step");
        assertThat(executionRequestDto.scenario.steps.get(0).name).isEqualTo("context-put name");
        assertThat(executionRequestDto.scenario.steps.get(0).inputs).containsKey("someID");
    }

    @Test
    public void should_map_composable_test_case_to_execution_request() {
        // Given
        String expectedType = "task-id";
        String expectedTargetId = "target name";
        TargetDto expectedTarget = new TargetDto(expectedTargetId, "", emptyMap(), SecurityInfo.builder().build(), emptyList());

        Map<String, Object> expectedOutputs = Maps.of("output1", "value1", "output2", "value2");

        Map<String, Object> expectedInputs = Maps.of(
            // Simple inputs
            "input 1 name", "v1 input 1 name",
            "input name empty", null,
            // List inputs
            "list 1 input name", Arrays.asList("v1 list 1 input", "v2 list 1 input"),
            "list 2 input name empty", emptyList(),
            // Map inputs
            "map 1 input name", Maps.of(
                "k1 map input", "v1 map 1 input",
                "k2 map input", "v2 map 1 input"),
            "map 2 input name with empty", Maps.of(
                "k1 map input", "v1 map 2 input",
                "k2 map input empty", ""
            )
        );


        final String implementationFull = Files.contentOf(new File(Resources.getResource("raw_scenarios/raw_composable_testcase.json").getPath()), Charset.forName("UTF-8"));

        List<FunctionalStep> steps = new ArrayList<>();
        steps.add(FunctionalStep.builder()
            .withName("first child step")
            .withImplementation(java.util.Optional.of(implementationFull))
            .build());
        steps.add(FunctionalStep.builder()
            .withName("second child step - parent")
            .withSteps(
                Collections.singletonList(FunctionalStep.builder()
                    .withName("first inner child step")
                    .withImplementation(Optional.of(implementationFull))
                    .build())
            )
            .build());

        ComposableTestCase testCase = new ComposableTestCase(
            "fake-id",
            TestCaseMetadataImpl.builder()
                .withTitle("fake title")
                .build(),
            ComposableScenario.builder()
                .withFunctionalSteps(
                    Arrays.asList(
                        FunctionalStep.builder()
                            .withName("first root step")
                            .withImplementation(java.util.Optional.of(implementationFull))
                            .build(),
                        FunctionalStep.builder()
                            .withName("second root step - parent")
                            .withSteps(
                                steps
                            )
                            .build()
                    ))
                .build()
        );

        when(environmentRepository.getAndValidateServer(eq(expectedTargetId), any()))
            .thenReturn(Target.builder()
                .withId(Target.TargetId.of(expectedTargetId,"envName"))
                .withUrl("")
                .build());

        // When
        ExecutionRequest request = new ExecutionRequest(testCase, "");
        final ExecutionRequestDto executionRequestDto = sut.toDto(request);

        // Then
        ExecutionRequestDto.StepDefinitionRequestDto rootStep = executionRequestDto.scenario;
        assertRootStepDefinitionRequestDto(rootStep, testCase.metadata.title());
        assertThat(rootStep.steps).hasSize(2);

        ExecutionRequestDto.StepDefinitionRequestDto step = rootStep.steps.get(0);
        assertStepDefinitionRequestDtoImplementation(step, testCase.composableScenario.functionalSteps.get(0).name, expectedType, expectedTarget, expectedInputs, expectedOutputs);

        step = rootStep.steps.get(1);
        assertRootStepDefinitionRequestDto(step, testCase.composableScenario.functionalSteps.get(1).name);
        assertThat(step.steps).hasSize(2);

        step = rootStep.steps.get(1).steps.get(0);
        assertStepDefinitionRequestDtoImplementation(step, testCase.composableScenario.functionalSteps.get(1).steps.get(0).name, expectedType, expectedTarget, expectedInputs, expectedOutputs);

        step = rootStep.steps.get(1).steps.get(1);
        assertRootStepDefinitionRequestDto(step, testCase.composableScenario.functionalSteps.get(1).steps.get(1).name);
        assertThat(step.steps).hasSize(1);

        step = rootStep.steps.get(1).steps.get(1).steps.get(0);
        assertStepDefinitionRequestDtoImplementation(step, testCase.composableScenario.functionalSteps.get(1).steps.get(1).steps.get(0).name, expectedType, expectedTarget, expectedInputs, expectedOutputs);
    }

    private void assertRootStepDefinitionRequestDto(ExecutionRequestDto.StepDefinitionRequestDto stepDefinitionRequestDto, String name) {
        assertThat(stepDefinitionRequestDto).isNotNull();
        assertThat(stepDefinitionRequestDto.name).isEqualTo(name);
        assertThat(stepDefinitionRequestDto.type).isNullOrEmpty();
        assertThat(stepDefinitionRequestDto.target).isEqualTo(
            new TargetDto("", "", emptyMap(), SecurityInfo.builder().build(), emptyList())
        );
        assertThat(stepDefinitionRequestDto.inputs).isNullOrEmpty();
        assertThat(stepDefinitionRequestDto.outputs).isNullOrEmpty();
    }

    private void assertStepDefinitionRequestDtoImplementation(ExecutionRequestDto.StepDefinitionRequestDto stepDefinitionRequestDto,
                                                              String name,
                                                              String implementationType,
                                                              TargetDto implementationTarget,
                                                              Map<String, Object> implementationInputs,
                                                              Map<String, Object> implementationOuputs) {
        assertThat(stepDefinitionRequestDto).isNotNull();
        assertThat(stepDefinitionRequestDto.name).isEqualTo(name);
        assertThat(stepDefinitionRequestDto.type).isEqualTo(implementationType);
        assertThat(stepDefinitionRequestDto.target).isEqualTo(implementationTarget);
        assertThat(stepDefinitionRequestDto.inputs).containsAllEntriesOf(implementationInputs);
        assertThat(stepDefinitionRequestDto.outputs).containsAllEntriesOf(implementationOuputs);
        assertThat(stepDefinitionRequestDto.steps).isEmpty();
    }
}
