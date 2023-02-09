package com.chutneytesting.component.scenario.api;

import com.chutneytesting.component.execution.domain.ExecutableComposedStep;
import com.chutneytesting.component.scenario.api.dto.ComposableStepDto;
import com.chutneytesting.component.scenario.api.dto.ImmutableComposableStepDto;
import com.chutneytesting.component.scenario.api.dto.ImmutableStrategyDto;
import com.chutneytesting.component.scenario.api.dto.StrategyDto;
import com.chutneytesting.server.core.domain.execution.Strategy;
import com.chutneytesting.server.core.domain.tools.ui.KeyValue;
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

    private static StrategyDto toDto(Strategy strategy) {
        return ImmutableStrategyDto.builder()
            .type(ComposableStrategyType.fromEngineType(strategy.type).name)
            .putAllParameters(ComposableStepMapper.fromDtoParameters(strategy.parameters))
            .build();
    }


}
