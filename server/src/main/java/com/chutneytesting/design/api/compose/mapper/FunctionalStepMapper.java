package com.chutneytesting.design.api.compose.mapper;

import static com.chutneytesting.design.api.compose.mapper.ComposableTestCaseMapper.fromFrontId;
import static com.chutneytesting.design.api.compose.mapper.ComposableTestCaseMapper.toFrontId;
import static java.util.stream.Collectors.toList;

import com.chutneytesting.design.api.compose.dto.FunctionalStepDto;
import com.chutneytesting.design.api.compose.dto.ImmutableFunctionalStepDto;
import com.chutneytesting.design.api.compose.dto.ImmutableStrategy;
import com.chutneytesting.design.api.compose.dto.KeyValue;
import com.chutneytesting.design.domain.compose.FunctionalStep;
import com.chutneytesting.design.domain.compose.StepUsage;
import com.chutneytesting.design.domain.compose.Strategy;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FunctionalStepMapper {

    public static FunctionalStepDto toDto(FunctionalStep functionalStep) {
        ImmutableFunctionalStepDto.Builder builder = ImmutableFunctionalStepDto.builder()
            .id(toFrontId(functionalStep.id))
            .name(functionalStep.name)
            .tags(functionalStep.tags);

        functionalStep.usage.ifPresent(
            stepUsage -> builder.usage(FunctionalStepDto.StepUsage.valueOf(stepUsage.name())));

        functionalStep.implementation.ifPresent(
            builder::task
        );

        functionalStep.steps
            .forEach(step -> builder.addSteps(toDto(step)));

        builder.parameters(KeyValue.fromMap(functionalStep.parameters));

        builder.strategy(toDto(functionalStep.strategy));

        builder.addAllDataSet(KeyValue.fromMap(functionalStep.dataSet));

        return builder.build();
    }

    private static com.chutneytesting.design.api.compose.dto.Strategy toDto(Strategy strategy) {
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

    public static FunctionalStep fromDto(FunctionalStepDto dto) {
        FunctionalStep.FunctionalStepBuilder functionalStepBuilder = FunctionalStep.builder()
            .withId(fromFrontId(dto.id()))
            .withName(dto.name())
            .withUsage(java.util.Optional.of(StepUsage.valueOf(dto.usage().name())))
            .withStrategy(fromDto(dto.strategy()))
            .withImplementation(dto.task())
            .withSteps(dto.steps().stream().map(FunctionalStepMapper::fromDto).collect(toList()))
            .withParameters(KeyValue.toMap(dto.parameters()))
            .overrideDataSetWith(KeyValue.toMap(dto.dataSet()))
            .withTags(dto.tags().stream().map(String::trim).filter(t -> !t.isEmpty()).collect(toList()));

        return functionalStepBuilder.build();
    }

    private static Strategy fromDto(com.chutneytesting.design.api.compose.dto.Strategy strategyDto) {
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
        SOFT("Soft", "soft-assert"),
        Loop("Loop", "Loop");

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
