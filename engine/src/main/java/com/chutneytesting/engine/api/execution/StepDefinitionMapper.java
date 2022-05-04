package com.chutneytesting.engine.api.execution;

import static java.util.stream.Collectors.toList;

import com.chutneytesting.engine.domain.environment.SecurityInfoImpl;
import com.chutneytesting.engine.domain.environment.TargetImpl;
import com.chutneytesting.engine.domain.execution.StepDefinition;
import com.chutneytesting.engine.domain.execution.strategies.StepStrategyDefinition;
import com.chutneytesting.engine.domain.execution.strategies.StrategyProperties;
import java.util.List;
import java.util.Optional;

class StepDefinitionMapper {

    private StepDefinitionMapper() {
    }

    static StepDefinition toStepDefinition(StepDefinitionDto dto, String environment) {
        StepStrategyDefinition strategy = null;
        if (dto.strategy != null) {
            StrategyProperties strategyProperties = new StrategyProperties(dto.strategy.strategyProperties);
            strategy = new StepStrategyDefinition(
                dto.strategy.type,
                strategyProperties
            );
        }

        List<StepDefinition> steps = dto.steps.stream()
            .map(dto1 -> toStepDefinition(dto1, environment))
            .collect(toList());

        return new StepDefinition(
            dto.name != null ? dto.name : "",
            dto.getTarget().isPresent() ? fromDto(dto.getTarget().get()) : null,
            dto.type != null ? dto.type : "",
            strategy,
            dto.inputs,
            steps,
            dto.outputs,
            dto.validations,
            environment
        );
    }

    private static TargetImpl fromDto(TargetExecutionDto targetDto) {
        return TargetImpl.builder()
            .withName(targetDto.id)
            .withUrl(targetDto.url)
            .withAgents(targetDto.agents)
            .withProperties(targetDto.properties)
            .withSecurity(fromDto(targetDto.security))
            .build();
    }

    private static SecurityInfoImpl fromDto(SecurityInfoExecutionDto dto) {
        return SecurityInfoImpl.builder()
            .credential(fromDto(dto.credential))
            .keyStore(dto.keyStore)
            .keyStorePassword(dto.keyStorePassword)
            .trustStore(dto.trustStore)
            .trustStorePassword(dto.trustStorePassword)
            .keyPassword(dto.keyPassword)
            .privateKey(dto.privateKey)
            .build();
    }

    private static SecurityInfoImpl.Credential fromDto(CredentialExecutionDto credential) {
        return Optional.ofNullable(credential)
            .map(c -> SecurityInfoImpl.Credential.of(c.username, c.password))
            .orElse(SecurityInfoImpl.Credential.NONE);
    }
}
