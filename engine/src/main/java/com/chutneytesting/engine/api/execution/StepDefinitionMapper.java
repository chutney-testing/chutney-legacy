package com.chutneytesting.engine.api.execution;


import static java.util.stream.Collectors.toList;

import com.chutneytesting.engine.domain.environment.ImmutableTarget;
import com.chutneytesting.engine.domain.environment.Target;
import com.chutneytesting.engine.domain.execution.StepDefinition;
import com.chutneytesting.engine.domain.execution.strategies.StepStrategyDefinition;
import com.chutneytesting.engine.domain.execution.strategies.StrategyProperties;
import java.util.List;

class StepDefinitionMapper {

    private StepDefinitionMapper() {
    }

    static StepDefinition toStepDefinition(StepDefinitionDto dto) {
        StepStrategyDefinition strategy = null;
        if (dto.strategy != null) {
            StrategyProperties strategyProperties = new StrategyProperties(dto.strategy.strategyProperties);
            strategy = new StepStrategyDefinition(
                dto.strategy.type,
                strategyProperties
            );
        }

        List<StepDefinition> steps = dto.steps.stream()
            .map(StepDefinitionMapper::toStepDefinition)
            .collect(toList());

        return new StepDefinition(
            dto.name != null ? dto.name : "",
            dto.getTarget().isPresent() ? fromDto(dto.getTarget().get()) : null,
            dto.type != null ? dto.type : "",
            strategy,
            dto.inputs,
            steps,
            dto.outputs
        );
    }

    private static Target fromDto(TargetDto targetDto) {
        return ImmutableTarget.builder()
            .id(ImmutableTarget.TargetId.of(targetDto.id))
            .url(targetDto.url)
            .agents(targetDto.agents)
            .properties(targetDto.properties)
            .security(targetDto.security)
            .build();
    }
}
