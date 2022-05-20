package com.chutneytesting.glacio.api;

import static java.util.stream.Collectors.toList;

import com.chutneytesting.engine.api.execution.StepDefinitionDto;
import com.chutneytesting.engine.api.execution.StepDefinitionDto.StepStrategyDefinitionDto;
import com.chutneytesting.engine.api.execution.StepDefinitionDto.StrategyPropertiesDto;
import com.chutneytesting.engine.api.execution.TargetExecutionDto;
import com.chutneytesting.engine.domain.execution.StepDefinition;
import com.chutneytesting.task.spi.injectable.Target;
import java.util.List;

class StepDefinitionMapper {

    private StepDefinitionMapper() {
    }

    static StepDefinitionDto toStepDefinitionDto(StepDefinition stepDefinition) {
        StepStrategyDefinitionDto strategy = null;
        if (stepDefinition.getStrategy().isPresent()) {
            StrategyPropertiesDto strategyProperties = new StrategyPropertiesDto(stepDefinition.getStrategy().get().strategyProperties);
            strategy = new StepStrategyDefinitionDto(
                stepDefinition.getStrategy().get().type,
                strategyProperties
            );
        }

        List<StepDefinitionDto> steps = stepDefinition.steps.stream()
            .map(StepDefinitionMapper::toStepDefinitionDto)
            .collect(toList());

        return new StepDefinitionDto(
            stepDefinition.name != null ? stepDefinition.name : "",
            stepDefinition.getTarget().isPresent() ? toDto(stepDefinition.getTarget().get()) : null,
            stepDefinition.type != null ? stepDefinition.type : "",
            strategy,
            stepDefinition.inputs,
            steps,
            stepDefinition.outputs,
            stepDefinition.validations
        );
    }

    private static TargetExecutionDto toDto(Target target) {
        return new TargetExecutionDto(
            target.name(),
            target.url(),
            null,//target.properties(),
            null // TODO no agent in glacio adapter
        );
    }
}
