package com.chutneytesting.component.scenario.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.chutneytesting.component.scenario.api.dto.ComposableStepDto;
import com.chutneytesting.component.scenario.api.dto.ImmutableComposableStepDto;
import com.chutneytesting.component.scenario.domain.ComposableStep;
import com.chutneytesting.server.core.domain.tools.ui.KeyValue;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings("OptionalGetWithoutIsPresent")
public class ComposableStepMapperTest {

    @Test
    public void should_map_func_step_id_and_name_to_dto_when_toDto_called() {
        // Given
        String FSTEPID = "1-1";
        String FSTEP_NAME = "a functional step";
        ComposableStep fStep = ComposableStep.builder()
            .withId(FSTEPID)
            .withName(FSTEP_NAME)
            .build();

        // When
        ComposableStepDto composableStepDto = ComposableStepMapper.toDto(fStep);

        // Then
        Assertions.assertThat(composableStepDto.id().get()).isEqualTo("1-1");
        Assertions.assertThat(composableStepDto.name()).isEqualTo(FSTEP_NAME);
    }

    @Test
    public void should_map_func_step_sub_tech_steps_to_dto_when_toDto_called() {
        // Given
        String TECHNICAL_CONTENT = "{\"type\": \"debug\"}";
        String TECHNICAL_CONTENT_B = "{\"type\": \"debug\"}";
        ComposableStep fStep = ComposableStep.builder()
            .withId("#1:1")
            .withName("a functional step with sub step with implementation")
            .withSteps(
                Arrays.asList(
                    ComposableStep.builder()
                        .withId("#1:2")
                        .withName("a functional sub step with implementation")
                        .withImplementation(TECHNICAL_CONTENT)
                        .build(),
                    ComposableStep.builder()
                        .withId("#1:3")
                        .withName("a functional sub step with sub step with implementation")
                        .withSteps(
                            Collections.singletonList(
                                ComposableStep.builder()
                                    .withName("a functional sub sub step with implementation")
                                    .withImplementation(TECHNICAL_CONTENT_B)
                                    .build()
                            )
                        )
                        .build()
                )
            )
            .build();

        // When
        ComposableStepDto composableStepDto = ComposableStepMapper.toDto(fStep);

        // Then
        Assertions.assertThat(composableStepDto.steps().get(0).action().get()).isEqualTo(TECHNICAL_CONTENT);
        Assertions.assertThat(composableStepDto.steps().get(1).steps().get(0).action().get()).isEqualTo(TECHNICAL_CONTENT_B);
    }

    @Test
    public void should_map_func_step_sub_func_steps_to_dto_when_toDto_called() {
        // Given
        String FSTEP_NAME = "a functional step with functional sub steps";
        String ANOTHER_FSTEP_NAME = "a functional sub step";
        ComposableStep fStep = ComposableStep.builder()
            .withId("#1:1")
            .withName(FSTEP_NAME)
            .withSteps(
                Arrays.asList(
                    ComposableStep.builder()
                        .withId("#1:2")
                        .withName(FSTEP_NAME)
                        .withSteps(
                            Collections.singletonList(
                                ComposableStep.builder()
                                    .withName(ANOTHER_FSTEP_NAME)
                                    .build()
                            )
                        )
                        .build(),
                    ComposableStep.builder()
                        .withId("#1:3")
                        .withName(ANOTHER_FSTEP_NAME)
                        .build()
                )
            )
            .build();

        // When
        ComposableStepDto composableStepDto = ComposableStepMapper.toDto(fStep);

        // Then
        Assertions.assertThat(composableStepDto.steps().get(0).name()).isEqualTo(FSTEP_NAME);
        Assertions.assertThat(composableStepDto.steps().get(0).steps().get(0).name()).isEqualTo(ANOTHER_FSTEP_NAME);
        Assertions.assertThat(composableStepDto.steps().get(1).name()).isEqualTo(ANOTHER_FSTEP_NAME);
    }

    @Test
    public void should_map_func_step_parameters_and_dataset_to_dto_when_toDto_called() {
        // Given
        ComposableStep fStep = ComposableStep.builder()
            .withId("#1:1")
            .withName("a functional step")
            .withDefaultParameters(
                Map.of(
                    "param1", "param1 value",
                    "param2", ""
                )
            )
            .withExecutionParameters(
                Map.of(
                    "param1", "param1 value",
                    "param2", "",
                    "param3 from children", "",
                    "param4 from child", "param4 value"
                )
            )
            .build();

        // When
        ComposableStepDto composableStepDto = ComposableStepMapper.toDto(fStep);

        // Then
        Assertions.assertThat(composableStepDto.defaultParameters()).containsExactlyElementsOf(KeyValue.fromMap(fStep.defaultParameters));
        Assertions.assertThat(composableStepDto.executionParameters()).containsExactlyElementsOf(KeyValue.fromMap(fStep.executionParameters));
    }

    @Test
    public void should_map_dto_to_func_step_when_fromDto_called() {
        // Given
        String TECHNICAL_CONTENT = "{\"type\": \"debug\"}";

        ComposableStepDto dto = ImmutableComposableStepDto.builder()
            .id("id")
            .name("name")
            .usage(ComposableStepDto.StepUsage.GIVEN)
            .addSteps(
                ImmutableComposableStepDto.builder()
                    .name("sub step 1")
                    .action(TECHNICAL_CONTENT)
                    .build()
            )
            .addSteps(
                ImmutableComposableStepDto.builder()
                    .name("sub step 2")
                    .build()
            )
            .addAllDefaultParameters(
                KeyValue.fromMap(
                    Map.of(
                        "param1", "param1 value",
                        "param2", ""
                    )
                ))
            .addAllExecutionParameters(
                KeyValue.fromMap(
                    Map.of(
                        "param1", "param1 value",
                        "param2", "",
                        "param3 from children", "",
                        "param4 from child", "param4 value"
                    )
                )
            )
            .build();

        // When
        ComposableStep step = ComposableStepMapper.fromDto(dto);

        // Then
        assertThat(step.id).isEqualTo(dto.id().get());
        assertThat(step.name).isEqualTo(dto.name());
        assertThat(step.steps.get(0).implementation.get()).isEqualTo(TECHNICAL_CONTENT);
        assertThat(step.steps.get(1).name).isEqualTo(dto.steps().get(1).name());
        assertThat(step.defaultParameters).containsAllEntriesOf(KeyValue.toMap(dto.defaultParameters()));
        assertThat(step.executionParameters).containsAllEntriesOf(KeyValue.toMap(dto.executionParameters()));
    }
}
