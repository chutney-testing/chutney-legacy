package com.chutneytesting.glacio.api;

import com.chutneytesting.engine.api.execution.ExecutionRequestDto;
import com.chutneytesting.engine.api.execution.ExecutionRequestDto.StepDefinitionRequestDto;
import com.chutneytesting.engine.api.execution.StepDefinitionDto;
import java.util.List;
import java.util.stream.Collectors;

public class ExecutionRequestMapper {

    public static ExecutionRequestDto toDto(StepDefinitionDto stepDefinitionDto, String environment) {
        final StepDefinitionRequestDto stepDefinitionRequestDto = getStepDefinitionRequestFromStepDef(stepDefinitionDto);
        return new ExecutionRequestDto(stepDefinitionRequestDto, environment);
    }

    private static StepDefinitionRequestDto getStepDefinitionRequestFromStepDef(StepDefinitionDto definition) {
        final ExecutionRequestDto.StepStrategyDefinitionRequestDto strategy;
        if (definition.strategy != null) {
            strategy = new ExecutionRequestDto.StepStrategyDefinitionRequestDto(definition.strategy.type, definition.strategy.strategyProperties);
        } else {
            strategy = null;
        }

        List<StepDefinitionRequestDto> steps = definition.steps.stream()
            .map(ExecutionRequestMapper::getStepDefinitionRequestFromStepDef)
            .collect(Collectors.toList());

        return new StepDefinitionRequestDto(
            definition.name,
            definition.getTarget().orElse(null),
            strategy,
            definition.type,
            definition.inputs,
            steps,
            definition.outputs,
            definition.validations
        );
    }

}
