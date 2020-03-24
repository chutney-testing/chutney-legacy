package com.chutneytesting.engine.api.execution;


import static java.util.stream.Collectors.toList;

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
            // TODO is a step without a name is really a step ?
            dto.name != null ? dto.name : "",
            dto.getTarget().isPresent() ? fromDto(dto.getTarget().get()) : null,
            // TODO is a step without a type is really a step ?
            dto.type != null ? dto.type : "",
            strategy,
            dto.inputs,
            steps,
            dto.outputs
        );
    }

    private static Target fromDto(TargetDto targetDto) {
        return Target.builder()
            .withId(targetDto.id)
            .withUrl(targetDto.url)
            .withAgents(targetDto.agents)
            .withProperties(targetDto.properties)
            .withSecurity(targetDto.security)
            .build();
    }
}
