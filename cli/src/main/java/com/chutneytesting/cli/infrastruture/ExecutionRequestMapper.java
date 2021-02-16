package com.chutneytesting.cli.infrastruture;

import com.chutneytesting.engine.api.execution.CredentialExecutionDto;
import com.chutneytesting.engine.api.execution.ExecutionRequestDto;
import com.chutneytesting.engine.api.execution.ExecutionRequestDto.StepDefinitionRequestDto;
import com.chutneytesting.engine.api.execution.SecurityInfoExecutionDto;
import com.chutneytesting.engine.api.execution.TargetExecutionDto;
import com.chutneytesting.engine.domain.environment.TargetImpl;
import com.chutneytesting.task.spi.injectable.SecurityInfo;
import com.chutneytesting.task.spi.injectable.Target;
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
                .map(t -> getTarget(t, originalEnvironmentObject).orElse(TargetImpl.NONE)),
            dto.scenario().type()
                .orElse(""),
            strategy,
            dto.scenario().inputs(),
            dto.scenario().steps().stream()
                .map(s -> buildStepDefinitionCore(s, originalEnvironmentObject))
                .collect(Collectors.toList()),
            dto.scenario().outputs(),
            originalEnvironmentObject.name());
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
                .map(t -> getTarget(t, originalEnvironmentObject).orElse(TargetImpl.NONE)),
            dto.type()
                .orElse(""),
            strategy,
            dto.inputs(),
            dto.steps().stream()
                .map(s -> buildStepDefinitionCore(s, originalEnvironmentObject))
                .collect(Collectors.toList()),
            dto.outputs(),
            originalEnvironmentObject.name());
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
            toDto(definition.target.orElse(TargetImpl.NONE)),
            strategy,
            definition.type,
            definition.inputs,
            steps,
            definition.outputs,
            definition.environment);
    }

    private static TargetExecutionDto toDto(Target target) {
        return new TargetExecutionDto(
            target.name(),
            target.url(),
            target.properties(),
            toDto(target.security()),
            ((TargetImpl) target).agents
        );
    }

    private static SecurityInfoExecutionDto toDto(SecurityInfo security) {
        return new SecurityInfoExecutionDto(
            security.credential().map(ExecutionRequestMapper::toDto).orElse(null),
            security.trustStore().orElse(null),
            security.trustStorePassword().orElse(null),
            security.keyStore().orElse(null),
            security.keyStorePassword().orElse(null),
            security.privateKey().orElse(null)
        );
    }

    private static CredentialExecutionDto toDto(SecurityInfo.Credential credential) {
        return new CredentialExecutionDto(credential.username(), credential.password());
    }
}
