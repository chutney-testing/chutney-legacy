package com.chutneytesting.execution.infra.execution;

import static com.chutneytesting.environment.api.dto.NoTargetDto.NO_TARGET_DTO;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isBlank;

import com.chutneytesting.agent.domain.explore.CurrentNetworkDescription;
import com.chutneytesting.agent.domain.network.Agent;
import com.chutneytesting.agent.domain.network.NetworkDescription;
import com.chutneytesting.design.domain.scenario.gwt.GwtStep;
import com.chutneytesting.design.domain.scenario.gwt.GwtTestCase;
import com.chutneytesting.design.domain.scenario.gwt.Strategy;
import com.chutneytesting.design.domain.scenario.raw.RawTestCase;
import com.chutneytesting.engine.api.execution.CredentialExecutionDto;
import com.chutneytesting.engine.api.execution.ExecutionRequestDto;
import com.chutneytesting.engine.api.execution.ExecutionRequestDto.StepDefinitionRequestDto;
import com.chutneytesting.engine.api.execution.SecurityInfoExecutionDto;
import com.chutneytesting.engine.api.execution.TargetExecutionDto;
import com.chutneytesting.engine.domain.delegation.NamedHostAndPort;
import com.chutneytesting.environment.api.EmbeddedEnvironmentApi;
import com.chutneytesting.environment.api.dto.TargetDto;
import com.chutneytesting.execution.domain.ExecutionRequest;
import com.chutneytesting.execution.domain.compiler.ScenarioConversionException;
import com.chutneytesting.execution.domain.scenario.composed.ExecutableComposedStep;
import com.chutneytesting.execution.domain.scenario.composed.ExecutableComposedTestCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.hjson.JsonValue;
import org.springframework.stereotype.Component;

@Component
public class ExecutionRequestMapper {

    private final ObjectMapper objectMapper;
    private final EmbeddedEnvironmentApi environmentApplication;
    private final CurrentNetworkDescription currentNetworkDescription;

    public ExecutionRequestMapper(ObjectMapper objectMapper, EmbeddedEnvironmentApi environmentApplication, CurrentNetworkDescription currentNetworkDescription) {
        this.objectMapper = objectMapper;
        this.environmentApplication = environmentApplication;
        this.currentNetworkDescription = currentNetworkDescription;
    }

    public ExecutionRequestDto toDto(ExecutionRequest executionRequest) {
        final StepDefinitionRequestDto stepDefinitionRequestDto = convertToStepDef(executionRequest);
        return new ExecutionRequestDto(stepDefinitionRequestDto);
    }

    private StepDefinitionRequestDto convertToStepDef(ExecutionRequest executionRequest) { // TODO - shameless green - might be refactored later
        if (executionRequest.testCase instanceof RawTestCase) {
            return convertRaw(executionRequest);
        }

        if (executionRequest.testCase instanceof GwtTestCase) {
            return convertGwt(executionRequest);
        }

        if (executionRequest.testCase instanceof ExecutableComposedTestCase) {
            return convertComposed(executionRequest);
        }

        throw new ScenarioConversionException(executionRequest.testCase.metadata().id(),
            "Cannot create an executable StepDefinition from a " + executionRequest.testCase.getClass().getCanonicalName());
    }

    private StepDefinitionRequestDto convertRaw(ExecutionRequest executionRequest) {
        RawTestCase rawTestCase = (RawTestCase) executionRequest.testCase;
        try {
            ScenarioContent scenarioContent = objectMapper.readValue(JsonValue.readHjson(rawTestCase.scenario).toString(), ScenarioContent.class);
            return getStepDefinitionRequestFromStepDef(scenarioContent.scenario, executionRequest.environment);
        } catch (IOException e) {
            throw new ScenarioConversionException(rawTestCase.metadata().id(), e);
        }
    }

    private StepDefinitionRequestDto getStepDefinitionRequestFromStepDef(UnmarshalledStepDefinition definition, String env) {
        final ExecutionRequestDto.StepStrategyDefinitionRequestDto retryStrategy = ofNullable(definition.strategy)
            .map(s -> new ExecutionRequestDto.StepStrategyDefinitionRequestDto(s.type, s.parameters))
            .orElse(null);

        List<StepDefinitionRequestDto> steps = definition.steps.stream()
            .map(d -> getStepDefinitionRequestFromStepDef(d, env))
            .collect(toList());

        return new StepDefinitionRequestDto(
            definition.name,
            toExecutionTargetDto(getTargetForExecution(env, definition.target), env),
            retryStrategy,
            definition.type,
            definition.inputs,
            steps,
            definition.outputs,
            env);
    }

    private StepDefinitionRequestDto convertGwt(ExecutionRequest executionRequest) {
        GwtTestCase gwtTestCase = (GwtTestCase) executionRequest.testCase;
        return new StepDefinitionRequestDto(
            gwtTestCase.metadata.title,
            null,
            null,
            null,
            emptyMap(),
            convert(gwtTestCase.scenario.steps(), executionRequest.environment),
            emptyMap(),
            executionRequest.environment
        );
    }

    private List<StepDefinitionRequestDto> convert(List<GwtStep> steps, String env) {
        return steps.stream()
            .map(s -> convert(s, env))
            .collect(toList());
    }

    private StepDefinitionRequestDto convert(GwtStep step, String env) {
        return new StepDefinitionRequestDto(
            step.description,
            step.implementation.map(i -> toExecutionTargetDto(getTargetForExecution(env, i.target), env)).orElse(toExecutionTargetDto(NO_TARGET_DTO, env)),
            step.strategy.map(this::mapStrategy).orElse(null),
            step.implementation.map(i -> i.type).orElse(""),
            step.implementation.map(i -> i.inputs).orElse(emptyMap()),
            convert(step.subSteps, env),
            step.implementation.map(i -> i.outputs).orElse(emptyMap()),
            env
        );
    }

    private ExecutionRequestDto.StepStrategyDefinitionRequestDto mapStrategy(Strategy strategy) {
        return new ExecutionRequestDto.StepStrategyDefinitionRequestDto(
            strategy.type,
            strategy.parameters
        );
    }

    private ExecutionRequestDto.StepStrategyDefinitionRequestDto mapStrategy(com.chutneytesting.design.domain.scenario.compose.Strategy strategy) {
        return new ExecutionRequestDto.StepStrategyDefinitionRequestDto(
            strategy.type,
            strategy.parameters
        );
    }

    private TargetExecutionDto toExecutionTargetDto(TargetDto targetDto, String env) {
        if (targetDto == null || NO_TARGET_DTO.equals(targetDto)) {
            targetDto = NO_TARGET_DTO;
        }
        return new TargetExecutionDto(
            targetDto.name,
            targetDto.url,
            targetDto.propertiesToMap(),
            toSecurityInfoDto(targetDto),
            getAgents(targetDto, env)
        );
    }

    private static SecurityInfoExecutionDto toSecurityInfoDto(TargetDto targetDto) {
        return new SecurityInfoExecutionDto(
            toCredentialDto(targetDto),
            null,
            null,
            targetDto.keyStore,
            targetDto.keyStorePassword,
            targetDto.privateKey
        );
    }

    private static CredentialExecutionDto toCredentialDto(TargetDto targetDto) {
        if(targetDto.hasCredential()) {
            return new CredentialExecutionDto(targetDto.username, targetDto.password);
        } else {
            return null;
        }
    }

    private StepDefinitionRequestDto convertComposed(ExecutionRequest executionRequest) {
        ExecutableComposedTestCase composedTestCase = (ExecutableComposedTestCase) executionRequest.testCase;
        try {
            return new StepDefinitionRequestDto(
                composedTestCase.metadata.title(),
                toExecutionTargetDto(NO_TARGET_DTO, executionRequest.environment),
                null,
                null,
                null,
                convertComposedSteps(composedTestCase.composedScenario.composedSteps, executionRequest.environment),
                null,
                executionRequest.environment
            );
        } catch (Exception e) {
            throw new ScenarioConversionException(composedTestCase.metadata().id(), e);
        }
    }

    private List<StepDefinitionRequestDto> convertComposedSteps(List<ExecutableComposedStep> composedSteps, String env) {
        return composedSteps.stream().map(f -> convert(f, env)).collect(toList());
    }

    private StepDefinitionRequestDto convert(ExecutableComposedStep composedStep, String env) {
        return new StepDefinitionRequestDto(
            composedStep.name,
            toExecutionTargetDto(getTargetForExecution(env, composedStep.stepImplementation.map(si -> si.target).orElse("")), env),
            this.mapStrategy(composedStep.strategy),
            composedStep.stepImplementation.map(si -> si.type).orElse(""),
            composedStep.stepImplementation.map(si -> si.inputs).orElse(emptyMap()),
            composedStep.steps.stream().map(f -> convert(f, env)).collect(toList()),
            composedStep.stepImplementation.map(si -> si.outputs).orElse(emptyMap()),
            env
        );
    }

    private TargetDto getTargetForExecution(String environmentName, String targetName) {
        if (isBlank(targetName)) {
            return NO_TARGET_DTO;
        }
        return environmentApplication.getTarget(environmentName, targetName);
    }

    private List<NamedHostAndPort> getAgents(TargetDto targetDto, String env) {
        List<NamedHostAndPort> nhaps = emptyList();
        Optional<NetworkDescription> networkDescription = currentNetworkDescription.findCurrent();
        if (networkDescription.isPresent() && networkDescription.get().localAgent().isPresent()) {
            final Agent localAgent = networkDescription.get().localAgent().get();
            List<Agent> agents = localAgent.findFellowAgentForReaching(targetDto.name, env);
            nhaps = agents.stream().map(a -> a.agentInfo).collect(toList());
        }
        return nhaps;
    }
}
