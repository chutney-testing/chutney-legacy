package com.chutneytesting.design.api.scenario.compose.mapper;

import static com.chutneytesting.design.api.scenario.compose.mapper.FunctionalStepMapper.fromDto;
import static com.chutneytesting.design.api.scenario.compose.mapper.FunctionalStepMapper.toDto;
import static org.assertj.core.api.Assertions.assertThat;

import com.chutneytesting.design.api.scenario.compose.dto.FunctionalStepDto;
import com.chutneytesting.design.api.scenario.compose.dto.ImmutableFunctionalStepDto;
import com.chutneytesting.tools.ui.KeyValue;
import com.chutneytesting.design.domain.scenario.compose.FunctionalStep;
import com.chutneytesting.design.domain.scenario.compose.StepUsage;
import java.util.Arrays;
import java.util.Collections;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.apache.groovy.util.Maps;
import org.junit.Test;
import org.junit.runner.RunWith;

@SuppressWarnings("OptionalGetWithoutIsPresent")
@RunWith(JUnitParamsRunner.class)
public class FunctionalStepMapperTest {

    @Test
    public void should_map_func_step_id_and_name_to_dto_when_toDto_called() {
        // Given
        String FSTEPID = "#1:1";
        String FSTEP_NAME = "a functional step";
        FunctionalStep fStep = FunctionalStep.builder()
            .withId(FSTEPID)
            .withName(FSTEP_NAME)
            .build();

        // When
        FunctionalStepDto functionalStepDto = toDto(fStep);

        // Then
        assertThat(functionalStepDto.id().get()).isEqualTo("1-1");
        assertThat(functionalStepDto.name()).isEqualTo(FSTEP_NAME);
    }

    @Test
    @Parameters({"GIVEN", "WHEN", "THEN"})
    public void should_map_func_usage_step_to_dto_when_toDto_called(StepUsage stepUsage) {
        // Given
        FunctionalStep fStep = FunctionalStep.builder()
            .withId("#1:1")
            .withName("a functional step")
            .withUsage(java.util.Optional.ofNullable(stepUsage))
            .build();

        // When
        FunctionalStepDto functionalStepDto = toDto(fStep);

        // Then
        assertThat(functionalStepDto.usage()).isEqualTo(FunctionalStepDto.StepUsage.valueOf(stepUsage.name()));
    }

    @Test
    public void should_map_func_step_sub_tech_steps_to_dto_when_toDto_called() {
        // Given
        String TECHNICAL_CONTENT = "{\"type\": \"debug\"}";
        String TECHNICAL_CONTENT_B = "{\"type\": \"debug\"}";
        FunctionalStep fStep = FunctionalStep.builder()
            .withId("#1:1")
            .withName("a functional step with sub step with implementation")
            .withSteps(
                Arrays.asList(
                    FunctionalStep.builder()
                        .withName("a functional sub step with implementation")
                        .withImplementation(java.util.Optional.of(TECHNICAL_CONTENT))
                        .build(),
                    FunctionalStep.builder()
                        .withName("a functional sub step with sub step with implementation")
                        .withSteps(
                            Collections.singletonList(
                                FunctionalStep.builder()
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
        FunctionalStepDto functionalStepDto = toDto(fStep);

        // Then
        assertThat(functionalStepDto.steps().get(0).task().get()).isEqualTo(TECHNICAL_CONTENT);
        assertThat(functionalStepDto.steps().get(1).steps().get(0).task().get()).isEqualTo(TECHNICAL_CONTENT_B);
    }

    @Test
    public void should_map_func_step_sub_func_steps_to_dto_when_toDto_called() {
        // Given
        String FSTEP_NAME = "a functional step with functional sub steps";
        String ANOTHER_FSTEP_NAME = "a functional sub step";
        FunctionalStep fStep = FunctionalStep.builder()
            .withId("#1:1")
            .withName(FSTEP_NAME)
            .withSteps(
                Arrays.asList(
                    FunctionalStep.builder()
                        .withName(FSTEP_NAME)
                        .withSteps(
                            Collections.singletonList(
                                FunctionalStep.builder()
                                    .withName(ANOTHER_FSTEP_NAME)
                                    .build()
                            )
                        )
                        .build(),
                    FunctionalStep.builder()
                        .withName(ANOTHER_FSTEP_NAME)
                        .build()
                )
            )
            .build();

        // When
        FunctionalStepDto functionalStepDto = toDto(fStep);

        // Then
        assertThat(functionalStepDto.steps().get(0).name()).isEqualTo(FSTEP_NAME);
        assertThat(functionalStepDto.steps().get(0).steps().get(0).name()).isEqualTo(ANOTHER_FSTEP_NAME);
        assertThat(functionalStepDto.steps().get(1).name()).isEqualTo(ANOTHER_FSTEP_NAME);
    }

    @Test
    public void should_map_func_step_parameters_and_dataset_to_dto_when_toDto_called() {
        // Given
        FunctionalStep fStep = FunctionalStep.builder()
            .withId("#1:1")
            .withName("a functional step")
            .withParameters(
                Maps.of(
                    "param1", "param1 value",
                    "param2", ""
                )
            )
            .overrideDataSetWith(
                Maps.of(
                    "param1", "param1 value",
                    "param2", "",
                    "param3 from children", "",
                    "param4 from child", "param4 value"
                )
            )
            .build();

        // When
        FunctionalStepDto functionalStepDto = toDto(fStep);

        // Then
        assertThat(functionalStepDto.parameters()).containsExactlyElementsOf(KeyValue.fromMap(fStep.parameters));
        assertThat(functionalStepDto.computedParameters()).containsExactlyElementsOf(KeyValue.fromMap(fStep.dataSet));
    }

    @Test
    public void should_map_dto_to_func_step_when_fromDto_called() {
        // Given
        String TECHNICAL_CONTENT = "{\"type\": \"debug\"}";

        FunctionalStepDto dto = ImmutableFunctionalStepDto.builder()
            .id("id")
            .name("name")
            .usage(FunctionalStepDto.StepUsage.GIVEN)
            .addSteps(
                ImmutableFunctionalStepDto.builder()
                    .name("sub step 1")
                    .task(TECHNICAL_CONTENT)
                    .build()
            )
            .addSteps(
                ImmutableFunctionalStepDto.builder()
                    .name("sub step 2")
                    .build()
            )
            .addAllParameters(
                KeyValue.fromMap(
                    Maps.of(
                        "param1", "param1 value",
                        "param2", ""
                    )
                ))
            .addAllComputedParameters(
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
        FunctionalStep step = fromDto(dto);

        // Then
        assertThat(step.id).isEqualTo(dto.id().get());
        assertThat(step.name).isEqualTo(dto.name());
        assertThat(FunctionalStepDto.StepUsage.valueOf(step.usage.get().name())).isEqualTo(dto.usage());
        assertThat(step.steps.get(0).implementation.get()).isEqualTo(TECHNICAL_CONTENT);
        assertThat(step.steps.get(1).name).isEqualTo(dto.steps().get(1).name());
        assertThat(step.parameters).containsAllEntriesOf(KeyValue.toMap(dto.parameters()));
        assertThat(step.dataSet).containsAllEntriesOf(KeyValue.toMap(dto.computedParameters()));
    }
}
