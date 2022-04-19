package com.chutneytesting.scenario.api.compose.mapper;

import com.chutneytesting.scenario.api.compose.dto.ComposableStepDto;
import com.chutneytesting.design.api.scenario.compose.dto.ImmutableComposableStepDto;
import com.chutneytesting.design.api.scenario.compose.dto.ImmutableStrategy;
import com.chutneytesting.scenario.domain.compose.Strategy;
import com.chutneytesting.execution.domain.scenario.composed.ExecutableComposedStep;
import com.chutneytesting.tools.ui.KeyValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ExecutableComposableStepMapper {

    private static final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    public static ComposableStepDto toDto(ExecutableComposedStep composableStep) {
        ImmutableComposableStepDto.Builder builder = ImmutableComposableStepDto.builder()
            .id("no id")
            .name(composableStep.name);

        composableStep.stepImplementation.ifPresent(
            implementation -> {
                try {
                    builder.task(objectMapper.writeValueAsString(implementation));
                } catch (JsonProcessingException e) {
                    builder.task("Cannot deserialize implementation because " + e.getMessage());
                }
            }
        );

        composableStep.steps
            .forEach(step -> builder.addSteps(toDto(step)));

        builder.defaultParameters(KeyValue.fromMap(composableStep.defaultParameters));

        builder.strategy(toDto(composableStep.strategy));

        builder.addAllExecutionParameters(KeyValue.fromMap(composableStep.executionParameters));

        return builder.build();
    }

    private static com.chutneytesting.scenario.api.compose.dto.Strategy toDto(Strategy strategy) {
        return ImmutableStrategy.builder()
            .type(ComposableStrategyType.fromEngineType(strategy.type).name)
            .putAllParameters(ComposableStepMapper.fromDtoParameters(strategy.parameters))
            .build();
    }


}
