package com.chutneytesting.design.api.scenario.compose.mapper;

import static com.chutneytesting.design.api.scenario.compose.mapper.ComposableStepMapper.fromDto;
import static com.chutneytesting.design.api.scenario.compose.mapper.ComposableStepMapper.toDto;
import static org.assertj.core.api.Assertions.assertThat;

import com.chutneytesting.design.api.scenario.compose.dto.ComposableStepDto;
import com.chutneytesting.design.api.scenario.compose.dto.ImmutableComposableStepDto;
import com.chutneytesting.design.domain.scenario.compose.ComposableStep;
import com.chutneytesting.tools.ui.KeyValue;
import java.util.Arrays;
import java.util.Collections;
import org.apache.groovy.util.Maps;
import org.junit.jupiter.api.Test;

@SuppressWarnings("OptionalGetWithoutIsPresent")
public class ComposableStepMapperTest {

    @Test
    public void should_map_func_step_id_and_name_to_dto_when_toDto_called() {
        // Given
        String FSTEPID = "#1:1";
        String FSTEP_NAME = "a functional step";
        ComposableStep fStep = ComposableStep.builder()
            .withId(FSTEPID)
            .withName(FSTEP_NAME)
            .build();

        // When
        ComposableStepDto composableStepDto = toDto(fStep);

        // Then
        assertThat(composableStepDto.id().get()).isEqualTo("1-1");
        assertThat(composableStepDto.name()).isEqualTo(FSTEP_NAME);
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
                        .withImplementation(java.util.Optional.of(TECHNICAL_CONTENT))
                        .build(),
                    ComposableStep.builder()
                        .withId("#1:3")
                        .withName("a functional sub step with sub step with implementation")
                        .withSteps(
                            Collections.singletonList(
                                ComposableStep.builder()
                                    .withName("a functional sub sub step with implementation")
                                    .withImplementation(java.util.Optional.of(TECHNICAL_CONTENT_B))
                                    .build()
                            )
                        )
                        .build()
                )
            )
            .build();

        // When
        ComposableStepDto composableStepDto = toDto(fStep);

        // Then
        assertThat(composableStepDto.steps().get(0).task().get()).isEqualTo(TECHNICAL_CONTENT);
        assertThat(composableStepDto.steps().get(1).steps().get(0).task().get()).isEqualTo(TECHNICAL_CONTENT_B);
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
        ComposableStepDto composableStepDto = toDto(fStep);

        // Then
        assertThat(composableStepDto.steps().get(0).name()).isEqualTo(FSTEP_NAME);
        assertThat(composableStepDto.steps().get(0).steps().get(0).name()).isEqualTo(ANOTHER_FSTEP_NAME);
        assertThat(composableStepDto.steps().get(1).name()).isEqualTo(ANOTHER_FSTEP_NAME);
    }

    @Test
    public void should_map_func_step_parameters_and_dataset_to_dto_when_toDto_called() {
        // Given
        ComposableStep fStep = ComposableStep.builder()
            .withId("#1:1")
            .withName("a functional step")
            .withBuiltInParameters(
                Maps.of(
                    "param1", "param1 value",
                    "param2", ""
                )
            )
            .overrideEnclosedUsageParametersWith(
                Maps.of(
                    "param1", "param1 value",
                    "param2", "",
                    "param3 from children", "",
                    "param4 from child", "param4 value"
                )
            )
            .build();

        // When
        ComposableStepDto composableStepDto = toDto(fStep);

        // Then
        assertThat(composableStepDto.builtInParameters()).containsExactlyElementsOf(KeyValue.fromMap(fStep.builtInParameters));
        assertThat(composableStepDto.enclosedUsageParameters()).containsExactlyElementsOf(KeyValue.fromMap(fStep.enclosedUsageParameters));
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
                    .task(TECHNICAL_CONTENT)
                    .build()
            )
            .addSteps(
                ImmutableComposableStepDto.builder()
                    .name("sub step 2")
                    .build()
            )
            .addAllBuiltInParameters(
                KeyValue.fromMap(
                    Maps.of(
                        "param1", "param1 value",
                        "param2", ""
                    )
                ))
            .addAllEnclosedUsageParameters(
                KeyValue.fromMap(
                    Maps.of(
                        "param1", "param1 value",
                        "param2", "",
                        "param3 from children", "",
                        "param4 from child", "param4 value"
                    )
                )
            )
            .build();

        // When
        ComposableStep step = fromDto(dto);

        // Then
        assertThat(step.id).isEqualTo(dto.id().get());
        assertThat(step.name).isEqualTo(dto.name());
        assertThat(step.steps.get(0).implementation.get()).isEqualTo(TECHNICAL_CONTENT);
        assertThat(step.steps.get(1).name).isEqualTo(dto.steps().get(1).name());
        assertThat(step.builtInParameters).containsAllEntriesOf(KeyValue.toMap(dto.builtInParameters()));
        assertThat(step.enclosedUsageParameters).containsAllEntriesOf(KeyValue.toMap(dto.enclosedUsageParameters()));
    }
}
