package com.chutneytesting.design.api.scenario.compose.mapper;

import static com.chutneytesting.tools.functional.ComposableIdUtils.fromFrontId;
import static com.chutneytesting.tools.functional.ComposableIdUtils.toFrontId;
import static java.util.stream.Collectors.toList;

import com.chutneytesting.design.api.scenario.compose.dto.ComposableStepDto;
import com.chutneytesting.design.api.scenario.compose.dto.ImmutableComposableStepDto;
import com.chutneytesting.design.api.scenario.compose.dto.ImmutableStrategy;
import com.chutneytesting.design.domain.scenario.compose.ComposableStep;
import com.chutneytesting.design.domain.scenario.compose.Strategy;
import com.chutneytesting.tools.ui.KeyValue;
import java.util.HashMap;
import java.util.Map;

public class ComposableStepMapper {

    public static ComposableStepDto toDto(ComposableStep composableStep) {
        ImmutableComposableStepDto.Builder builder = ImmutableComposableStepDto.builder()
            .id(toFrontId(composableStep.id))
            .name(composableStep.name)
            .tags(composableStep.tags);

        composableStep.implementation.ifPresent(
            builder::task
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
            .putAllParameters(toDto(strategy.parameters))
            .build();
    }

    private static Map<String, Object> toDto(Map<String, Object> parameters) {
        Map<String, Object> parametersDto = new HashMap<>(parameters);

        if (parametersDto.containsKey("timeOut")) {
            parametersDto.put("timeout", parameters.get("timeOut"));
            parametersDto.remove("timeOut");
        }

        if (parametersDto.containsKey("retryDelay")) {
            parametersDto.put("delay", parameters.get("retryDelay"));
            parametersDto.remove("retryDelay");
        }

        return parametersDto;
    }

    public static ComposableStep fromDto(ComposableStepDto dto) {
        return ComposableStep.builder()
            .withId(fromFrontId(dto.id()))
            .withName(dto.name())
            .withStrategy(fromDto(dto.strategy()))
            .withImplementation(dto.task().orElse(""))
            .withSteps(dto.steps().stream().map(ComposableStepMapper::fromDto).collect(toList()))
            .withDefaultParameters(KeyValue.toMap(dto.defaultParameters()))
            .withExecutionParameters(KeyValue.toMap(dto.executionParameters()))
            .withTags(dto.tags().stream().map(String::trim).filter(t -> !t.isEmpty()).collect(toList()))
            .build();
    }

    public static Map<String, Object> fromDtoParameters(Map<String, Object> parametersDto) {
        Map<String, Object> parameters = new HashMap<>(parametersDto);

        if (parameters.containsKey("timeout")) {
            parameters.put("timeOut", parametersDto.get("timeout"));
            parameters.remove("timeout");
        }

        if (parameters.containsKey("delay")) {
            parameters.put("retryDelay", parametersDto.get("delay"));
            parameters.remove("delay");
        }

        return parameters;
    }

    private static Strategy fromDto(com.chutneytesting.design.api.scenario.compose.dto.Strategy strategyDto) {
        return new Strategy(
            ComposableStrategyType.fromName(strategyDto.type()).engineType,
            fromDtoParameters(strategyDto.parameters())
        );
    }

}
