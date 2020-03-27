package com.chutneytesting.engine.infrastructure.delegation;

import com.chutneytesting.engine.api.execution.CredentialDto;
import com.chutneytesting.engine.api.execution.ExecutionRequestDto;
import com.chutneytesting.engine.api.execution.ExecutionRequestDto.StepDefinitionRequestDto;
import com.chutneytesting.engine.api.execution.ExecutionRequestDto.StepStrategyDefinitionRequestDto;
import com.chutneytesting.engine.api.execution.SecurityInfoDto;
import com.chutneytesting.engine.api.execution.TargetDto;
import com.chutneytesting.engine.domain.environment.SecurityInfo;
import com.chutneytesting.engine.domain.execution.StepDefinition;
import java.util.List;
import java.util.stream.Collectors;

class ExecutionRequestMapper {

    static ExecutionRequestDto from(StepDefinition stepDefinition) {
        final StepDefinitionRequestDto stepDefinitionRequestDto = getStepDefinitionRequestFromStepDef(stepDefinition);
        return new ExecutionRequestDto(stepDefinitionRequestDto);
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
            definition.outputs);
    }

    private static TargetDto extractTarget(StepDefinition definition) {
        return definition.getTarget().map(t -> new TargetDto(
                t.name,
                t.url,
                t.properties,
                from(t.security),
                t.agents
            ))
            .orElse(null);
    }

    private static SecurityInfoDto from(SecurityInfo security) {
        return new SecurityInfoDto(
            from(security.credential()),
            security.trustStore(),
            security.trustStorePassword(),
            security.keyStore(),
            security.keyStorePassword(),
            security.privateKey()
        );
    }

    private static CredentialDto from(SecurityInfo.Credential credential) {
        return new CredentialDto(credential.username, credential.password);
    }
}
