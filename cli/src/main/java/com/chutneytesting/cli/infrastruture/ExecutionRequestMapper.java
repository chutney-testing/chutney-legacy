package com.chutneytesting.cli.infrastruture;

import com.chutneytesting.engine.api.execution.ExecutionRequestDto;
import com.chutneytesting.engine.api.execution.ExecutionRequestDto.StepDefinitionRequestDto;
import com.chutneytesting.engine.api.execution.TargetDto;
import com.chutneytesting.engine.domain.environment.Target;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ExecutionRequestMapper {

    public static ExecutionRequestDto toDto(ScenarioContent stepDefinitionDto, Environment originalEnvironmentObject) {
        final StepDefinitionCore localDef = buildStepDefinitionCore(stepDefinitionDto, originalEnvironmentObject);
        final StepDefinitionRequestDto stepDefinitionRequestDto = getStepDefinitionRequestFromStepDef(localDef);
        return new ExecutionRequestDto(stepDefinitionRequestDto);
    }

    private static StepDefinitionCore buildStepDefinitionCore(ScenarioContent dto, Environment originalEnvironmentObject) {
        StepDefinitionCore.StepStrategyDefinitionCore strategy = null;
        if (dto.scenario().strategy().isPresent()) {
            StepDefinitionCore.StrategyPropertiesCore strategyProperties = new StepDefinitionCore.StrategyPropertiesCore(dto.scenario().strategy().get().parameters());
            strategy = new StepDefinitionCore.StepStrategyDefinitionCore(
                dto.scenario().strategy().get().type(),
                strategyProperties
            );
        }

        return new StepDefinitionCore(
            null,
            dto.scenario().name()
                .orElse(""),
            dto.scenario().target()
                .map(t -> getTarget(t, originalEnvironmentObject).orElse(Target.NONE)),
            dto.scenario().type()
                .orElse(""),
            strategy,
            dto.scenario().inputs(),
            dto.scenario().steps().stream()
                .map(s -> buildStepDefinitionCore(s, originalEnvironmentObject))
                .collect(Collectors.toList()),
            dto.scenario().outputs());
    }

    private static Optional<Target> getTarget(String targetName, Environment originalEnvironmentObject) {
        if(targetName == null || targetName.isEmpty()){
            return Optional.empty();
        }
        return originalEnvironmentObject.targets().stream().filter( t -> t.name().equals(targetName)).findFirst();
    }

    private static StepDefinitionCore buildStepDefinitionCore(ScenarioContent.UnmarshalledStepDefinition dto, Environment originalEnvironmentObject) {
        StepDefinitionCore.StepStrategyDefinitionCore strategy = null;
        if (dto.strategy().isPresent()) {
            StepDefinitionCore.StrategyPropertiesCore strategyProperties = new StepDefinitionCore.StrategyPropertiesCore(dto.strategy().get().parameters());
            strategy = new StepDefinitionCore.StepStrategyDefinitionCore(
                dto.strategy().get().type(),
                strategyProperties
            );
        }

        return new StepDefinitionCore(
            null,
            dto.name()
                .orElse(""),
            dto.target()
                .map(t -> getTarget(t, originalEnvironmentObject).orElse(Target.NONE)),
            dto.type()
                .orElse(""),
            strategy,
            dto.inputs(),
            dto.steps().stream()
                .map(s -> buildStepDefinitionCore(s, originalEnvironmentObject))
                .collect(Collectors.toList()),
            dto.outputs());
    }

    private static StepDefinitionRequestDto getStepDefinitionRequestFromStepDef(StepDefinitionCore definition) {
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
            toDto(definition.target.orElse(Target.NONE)),
            strategy,
            definition.type,
            definition.inputs,
            steps,
            definition.outputs);
    }

    private static TargetDto toDto(Target target) {
        return new TargetDto(
            target.id.name,
            target.url,
            target.properties,
            target.security,
            target.agents
        );
    }
}
