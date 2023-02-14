package com.chutneytesting.component.execution.infra;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.chutneytesting.agent.domain.explore.CurrentNetworkDescription;
import com.chutneytesting.component.execution.domain.ExecutableComposedScenario;
import com.chutneytesting.component.execution.domain.ExecutableComposedStep;
import com.chutneytesting.component.execution.domain.ExecutableComposedTestCase;
import com.chutneytesting.component.execution.domain.StepImplementation;
import com.chutneytesting.engine.api.execution.ExecutionRequestDto;
import com.chutneytesting.engine.api.execution.TargetExecutionDto;
import com.chutneytesting.environment.api.EmbeddedEnvironmentApi;
import com.chutneytesting.environment.api.dto.TargetDto;
import com.chutneytesting.server.core.domain.execution.ExecutionRequest;
import com.chutneytesting.server.core.domain.execution.ExecutionRequestMapper;
import com.chutneytesting.server.core.domain.scenario.TestCaseMetadataImpl;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.groovy.util.Maps;
import org.junit.jupiter.api.Test;

@SuppressWarnings("unchecked")
public class ComponentExecutionRequestMapperTest {

    private final EmbeddedEnvironmentApi environmentApplication = mock(EmbeddedEnvironmentApi.class);
    private final CurrentNetworkDescription currentNetworkDescription = mock(CurrentNetworkDescription.class);

    private final ExecutionRequestMapper sut = new ComponentExecutionRequestMapper(environmentApplication, currentNetworkDescription);

    @Test
    public void should_map_composed_test_case_to_execution_request() {
        // Given
        String expectedType = "action-id";
        String expectedTargetId = "target name";
        TargetExecutionDto expectedTarget = new TargetExecutionDto(expectedTargetId, "", emptyMap(), emptyList());

        LinkedHashMap<String, Object> expectedOutputs = new LinkedHashMap<>(Maps.of(
            "output1", "value1",
            "output2", "value2"
        ));

        LinkedHashMap<String, Object> expectedValidations = new LinkedHashMap<>(Maps.of(
            "first assert", "${#output1.equals('value1')}",
            "second assert", "${#output2.equals('value2')}"
        ));

        LinkedHashMap<String, Object> expectedInputs = new LinkedHashMap<>(
            Maps.of(
                "input 1 name", "v1 input 1 name",
                "input name empty", null
            )
        );

        final StepImplementation implementationFull = new StepImplementation(
            expectedType,
            expectedTargetId,
            Maps.of("input 1 name", "v1 input 1 name", "input name empty", null),
            Maps.of("output1", "value1", "output2", "value2"),
            Maps.of("first assert", "${#output1.equals('value1')}", "second assert", "${#output2.equals('value2')}")
        );

        List<ExecutableComposedStep> steps = new ArrayList<>();
        steps.add(ExecutableComposedStep.builder()
            .withName("first child step")
            .withImplementation(Optional.of(implementationFull))
            .build());
        steps.add(ExecutableComposedStep.builder()
            .withName("second child step - parent")
            .withSteps(
                Collections.singletonList(ExecutableComposedStep.builder()
                    .withName("first inner child step")
                    .withImplementation(Optional.of(implementationFull))
                    .build())
            )
            .build());

        ExecutableComposedTestCase testCase = new ExecutableComposedTestCase(
            TestCaseMetadataImpl.builder()
                .withTitle("fake title")
                .build(),
            ExecutableComposedScenario.builder()
                .withComposedSteps(
                    Arrays.asList(
                        ExecutableComposedStep.builder()
                            .withName("first root step")
                            .withImplementation(Optional.of(implementationFull))
                            .build(),
                        ExecutableComposedStep.builder()
                            .withName("second root step - parent")
                            .withSteps(
                                steps
                            )
                            .build()
                    ))
                .build()
        );

        when(environmentApplication.getTarget(any(), eq(expectedTargetId)))
            .thenReturn(new TargetDto(expectedTargetId, "", null));

        // When
        ExecutionRequest request = new ExecutionRequest(testCase, "", "");
        final ExecutionRequestDto executionRequestDto = sut.toDto(request);

        // Then
        ExecutionRequestDto.StepDefinitionRequestDto rootStep = executionRequestDto.scenario;
        assertRootStepDefinitionRequestDto(rootStep, testCase.metadata.title());
        assertThat(rootStep.steps).hasSize(2);

        ExecutionRequestDto.StepDefinitionRequestDto step = rootStep.steps.get(0);
        assertStepDefinitionRequestDtoImplementation(step, testCase.composedScenario.composedSteps.get(0).name, expectedType, expectedTarget, expectedInputs, expectedOutputs, expectedValidations);

        step = rootStep.steps.get(1);
        assertRootStepDefinitionRequestDto(step, testCase.composedScenario.composedSteps.get(1).name);
        assertThat(step.steps).hasSize(2);

        step = rootStep.steps.get(1).steps.get(0);
        assertStepDefinitionRequestDtoImplementation(step, testCase.composedScenario.composedSteps.get(1).steps.get(0).name, expectedType, expectedTarget, expectedInputs, expectedOutputs, expectedValidations);

        step = rootStep.steps.get(1).steps.get(1);
        assertRootStepDefinitionRequestDto(step, testCase.composedScenario.composedSteps.get(1).steps.get(1).name);
        assertThat(step.steps).hasSize(1);

        step = rootStep.steps.get(1).steps.get(1).steps.get(0);
        assertStepDefinitionRequestDtoImplementation(step, testCase.composedScenario.composedSteps.get(1).steps.get(1).steps.get(0).name, expectedType, expectedTarget, expectedInputs, expectedOutputs, expectedValidations);
    }

    private void assertRootStepDefinitionRequestDto(ExecutionRequestDto.StepDefinitionRequestDto stepDefinitionRequestDto, String name) {
        assertThat(stepDefinitionRequestDto).isNotNull();
        assertThat(stepDefinitionRequestDto.name).isEqualTo(name);
        assertThat(stepDefinitionRequestDto.type).isNullOrEmpty();
        assertThat(stepDefinitionRequestDto.target).isEqualTo(
            new TargetExecutionDto("", "", emptyMap(), emptyList())
        );
        assertThat(stepDefinitionRequestDto.inputs).isNullOrEmpty();
        assertThat(stepDefinitionRequestDto.outputs).isNullOrEmpty();
    }

    private void assertStepDefinitionRequestDtoImplementation(ExecutionRequestDto.StepDefinitionRequestDto stepDefinitionRequestDto,
                                                              String name,
                                                              String implementationType,
                                                              TargetExecutionDto implementationTarget,
                                                              LinkedHashMap<String, Object> implementationInputs,
                                                              LinkedHashMap<String, Object> implementationOuputs,
                                                              LinkedHashMap<String, Object> implementationValidations) {
        assertThat(stepDefinitionRequestDto).isNotNull();
        assertThat(stepDefinitionRequestDto.name).isEqualTo(name);
        assertThat(stepDefinitionRequestDto.type).isEqualTo(implementationType);
        assertThat(stepDefinitionRequestDto.target).isEqualTo(implementationTarget);
        implementationInputs.forEach((k, v) -> {
            if (v instanceof String) {
                assertThat(stepDefinitionRequestDto.inputs).containsEntry(k, v);
            } else if (v instanceof List) {
                assertThat((List<Object>) v).containsExactlyElementsOf((List<Object>) stepDefinitionRequestDto.inputs.get(k));
            } else if (v instanceof Map) {
                assertThat((Map<String, Object>) v).containsExactlyEntriesOf((Map<String, Object>) stepDefinitionRequestDto.inputs.get(k));
            }
        });
        assertThat(implementationOuputs).containsExactlyEntriesOf(stepDefinitionRequestDto.outputs);
        assertThat(implementationValidations).containsExactlyEntriesOf(stepDefinitionRequestDto.validations);
        assertThat(stepDefinitionRequestDto.steps).isEmpty();
    }
}
