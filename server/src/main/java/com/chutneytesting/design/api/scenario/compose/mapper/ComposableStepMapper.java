package com.chutneytesting.design.api.scenario.compose.mapper;

import static com.chutneytesting.tools.ui.ComposableIdUtils.fromFrontId;
import static com.chutneytesting.tools.ui.ComposableIdUtils.toFrontId;
import static java.util.stream.Collectors.toList;

import com.chutneytesting.design.api.scenario.compose.dto.ComposableStepDto;
import com.chutneytesting.design.api.scenario.compose.dto.ImmutableComposableStepDto;
import com.chutneytesting.design.api.scenario.compose.dto.ImmutableStrategy;
import com.chutneytesting.design.domain.scenario.compose.ComposableStep;
import com.chutneytesting.design.domain.scenario.compose.Strategy;
import com.chutneytesting.tools.ui.KeyValue;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

        builder.builtInParameters(KeyValue.fromMap(composableStep.builtInParameters));

        builder.strategy(toDto(composableStep.strategy));

        builder.addAllEnclosedUsageParameters(KeyValue.fromMap(composableStep.enclosedUsageParameters));

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
        ComposableStep.ComposableStepBuilder composableStepBuilder = ComposableStep.builder()
            .withId(fromFrontId(dto.id()))
            .withName(dto.name())
            .withStrategy(fromDto(dto.strategy()))
            .withImplementation(dto.task())
            .withSteps(dto.steps().stream().map(ComposableStepMapper::fromDto).collect(toList()))
            .withBuiltInParameters(KeyValue.toMap(dto.builtInParameters()))
            .overrideEnclosedUsageParametersWith(KeyValue.toMap(dto.enclosedUsageParameters()))
            .withTags(dto.tags().stream().map(String::trim).filter(t -> !t.isEmpty()).collect(toList()));

        return composableStepBuilder.build();
    }

    private static Strategy fromDto(com.chutneytesting.design.api.scenario.compose.dto.Strategy strategyDto) {
        return new Strategy(
            ComposableStrategyType.fromName(strategyDto.type()).engineType,
            fromDto(strategyDto.parameters())
        );
    }

    private static Map<String, Object> fromDto(Map<String, Object> parametersDto) {
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

    private enum ComposableStrategyType {

        DEFAULT("Default", ""),
        RETRY("Retry", "retry-with-timeout"),
        SOFT("Soft", "soft-assert");

        private static final Logger LOGGER = LoggerFactory.getLogger(ComposableStrategyType.class);
        public final String name;
        public final String engineType;

        ComposableStrategyType(String name, String engineType) {
            this.name = name;
            this.engineType = engineType;
        }

        public static ComposableStrategyType fromName(String name) {
            for (ComposableStrategyType e : ComposableStrategyType.values()) {
                if (e.name.equals(name)) {
                    return e;
                }
            }
            LOGGER.warn("Mapping strategy [{}] for engine as default", name);
            return DEFAULT;
        }

        public static ComposableStrategyType fromEngineType(String engineType) {
            for (ComposableStrategyType e : ComposableStrategyType.values()) {
                if (e.engineType.equals(engineType)) {
                    return e;
                }
            }
            LOGGER.warn("Mapping strategy [{}] for UI as default", engineType);
            return DEFAULT;
        }
    }
}
