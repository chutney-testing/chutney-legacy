package com.chutneytesting.design.api.scenario.compose.mapper;

import static com.chutneytesting.design.api.scenario.compose.mapper.ComposableStepMapper.fromDtoParameters;

import com.chutneytesting.design.api.scenario.compose.dto.ComposableStepDto;
import com.chutneytesting.design.api.scenario.compose.dto.ImmutableComposableStepDto;
import com.chutneytesting.design.api.scenario.compose.dto.ImmutableStrategy;
import com.chutneytesting.design.domain.scenario.compose.Strategy;
import com.chutneytesting.execution.domain.scenario.composed.ExecutableComposedStep;
import com.chutneytesting.tools.ui.KeyValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static com.chutneytesting.design.api.scenario.compose.dto.Strategy toDto(Strategy strategy) {
        return ImmutableStrategy.builder()
            .type(ComposableStrategyType.fromEngineType(strategy.type).name)
            .putAllParameters(fromDtoParameters(strategy.parameters))
            .build();
    }


}
