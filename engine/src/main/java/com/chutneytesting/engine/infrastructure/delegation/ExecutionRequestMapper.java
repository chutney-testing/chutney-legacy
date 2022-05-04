package com.chutneytesting.engine.infrastructure.delegation;

import com.chutneytesting.engine.api.execution.CredentialExecutionDto;
import com.chutneytesting.engine.api.execution.ExecutionRequestDto;
import com.chutneytesting.engine.api.execution.ExecutionRequestDto.StepDefinitionRequestDto;
import com.chutneytesting.engine.api.execution.ExecutionRequestDto.StepStrategyDefinitionRequestDto;
import com.chutneytesting.engine.api.execution.SecurityInfoExecutionDto;
import com.chutneytesting.engine.api.execution.TargetExecutionDto;
import com.chutneytesting.engine.domain.environment.TargetImpl;
import com.chutneytesting.engine.domain.execution.StepDefinition;
import com.chutneytesting.task.spi.injectable.SecurityInfo;
import java.util.List;
import java.util.stream.Collectors;

class ExecutionRequestMapper {

    static ExecutionRequestDto from(StepDefinition stepDefinition) {
        final StepDefinitionRequestDto stepDefinitionRequestDto = getStepDefinitionRequestFromStepDef(stepDefinition);
        return new ExecutionRequestDto(stepDefinitionRequestDto, stepDefinition.environment);
    }

    private static StepDefinitionRequestDto getStepDefinitionRequestFromStepDef(StepDefinition definition) {
        final StepStrategyDefinitionRequestDto strategy = definition.getStrategy()
            .map(s -> new StepStrategyDefinitionRequestDto(
                definition.getStrategy().get().type,
                definition.getStrategy().get().strategyProperties)
            ).orElse(null);

        List<StepDefinitionRequestDto> steps = definition.steps.stream()
            .map(ExecutionRequestMapper::getStepDefinitionRequestFromStepDef)
            .collect(Collectors.toList());

        return new StepDefinitionRequestDto(
            definition.name,
            extractTarget(definition),
            strategy,
            definition.type,
            definition.inputs,
            steps,
            definition.outputs,
            definition.validations);
    }

    private static TargetExecutionDto extractTarget(StepDefinition definition) {
        return definition.getTarget().map(t -> (TargetImpl) t).map(t -> new TargetExecutionDto(
                t.name(),
                t.url,
                t.properties,
                from(t.security),
                t.agents
            ))
            .orElse(null);
    }

    private static SecurityInfoExecutionDto from(SecurityInfo security) {
        return new SecurityInfoExecutionDto(
            security.credential().map(ExecutionRequestMapper::from).orElse(null),
            security.trustStore().orElse(null),
            security.trustStorePassword().orElse(null),
            security.keyStore().orElse(null),
            security.keyStorePassword().orElse(null),
            security.keyPassword().orElse(null),
            security.privateKey().orElse(null)
        );
    }

    private static CredentialExecutionDto from(SecurityInfo.Credential credential) {
        return new CredentialExecutionDto(credential.username(), credential.password());
    }
}
