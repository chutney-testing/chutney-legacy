package com.chutneytesting.execution.infra.execution;

import static com.chutneytesting.design.domain.environment.NoTarget.NO_TARGET;
import static java.util.Collections.emptyMap;
import static java.util.Optional.ofNullable;

import com.chutneytesting.agent.domain.explore.CurrentNetworkDescription;
import com.chutneytesting.agent.domain.network.Agent;
import com.chutneytesting.agent.domain.network.NetworkDescription;
import com.chutneytesting.design.domain.environment.EnvironmentRepository;
import com.chutneytesting.design.domain.environment.SecurityInfo;
import com.chutneytesting.design.domain.environment.Target;
import com.chutneytesting.design.domain.scenario.gwt.GwtStep;
import com.chutneytesting.design.domain.scenario.gwt.GwtTestCase;
import com.chutneytesting.design.domain.scenario.gwt.Strategy;
import com.chutneytesting.design.domain.scenario.raw.RawTestCase;
import com.chutneytesting.engine.api.execution.CredentialDto;
import com.chutneytesting.engine.api.execution.ExecutionRequestDto;
import com.chutneytesting.engine.api.execution.ExecutionRequestDto.StepDefinitionRequestDto;
import com.chutneytesting.engine.api.execution.SecurityInfoDto;
import com.chutneytesting.engine.api.execution.TargetDto;
import com.chutneytesting.execution.domain.ExecutionRequest;
import com.chutneytesting.execution.domain.compiler.ScenarioConversionException;
import com.chutneytesting.execution.domain.scenario.composed.ExecutableComposedStep;
import com.chutneytesting.execution.domain.scenario.composed.ExecutableComposedTestCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.hjson.JsonValue;
import org.springframework.stereotype.Component;

@Component
public class ExecutionRequestMapper {

    private final ObjectMapper objectMapper;
    private final EnvironmentRepository environmentRepository;
    private final CurrentNetworkDescription currentNetworkDescription;

    public ExecutionRequestMapper(ObjectMapper objectMapper, EnvironmentRepository environmentRepository, CurrentNetworkDescription currentNetworkDescription) {
        this.objectMapper = objectMapper;
        this.environmentRepository = environmentRepository;
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
            ScenarioContent scenarioContent = objectMapper.readValue(JsonValue.readHjson(rawTestCase.content).toString(), ScenarioContent.class);
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
            .collect(Collectors.toList());

        return new StepDefinitionRequestDto(
            definition.name,
            toDto(findTargetByName(definition.target, env)),
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
            .collect(Collectors.toList());
    }

    private StepDefinitionRequestDto convert(GwtStep step, String env) {
        return new StepDefinitionRequestDto(
            step.description,
            step.implementation.map(i -> toDto(findTargetByName(i.target, env))).orElse(toDto(NO_TARGET)),
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

    private static TargetDto toDto(Target target) {
        if (target == null) {
            target = NO_TARGET;
        }

        return new TargetDto(
            target.id.name,
            target.url,
            target.properties,
            toDto(target.security),
            target.agents
        );
    }

    private static SecurityInfoDto toDto(SecurityInfo security) {
        return new SecurityInfoDto(
            ofNullable(security.credential).map(ExecutionRequestMapper::toDto).orElse(null),
            security.trustStore,
            security.trustStorePassword,
            security.keyStore,
            security.keyStorePassword,
            security.privateKey
        );
    }

    private static CredentialDto toDto(SecurityInfo.Credential credential) {
        return new CredentialDto(credential.username, credential.password);
    }

    // TODO - see if it might be validated before in the domain
    private Target findTargetByName(String targetName, String environment) {
        if (targetName == null || targetName.isEmpty()) {
            return NO_TARGET;
        }

        Target target = environmentRepository.getAndValidateServer(targetName, environment);

        Optional<NetworkDescription> networkDescription = currentNetworkDescription.findCurrent();
        if (networkDescription.isPresent() && networkDescription.get().localAgent().isPresent()) {
            final Agent localAgent = networkDescription.get().localAgent().get();
            List<Agent> agents = localAgent.findFellowAgentForReaching(target.id);
            return Target.builder()
                .copyOf(target)
                .withAgents(agents.stream().map(a -> a.agentInfo).collect(Collectors.toList()))
                .build();
        }
        return target;
    }

    private StepDefinitionRequestDto convertComposed(ExecutionRequest executionRequest) {
        ExecutableComposedTestCase composedTestCase = (ExecutableComposedTestCase) executionRequest.testCase;
        try {
            return new StepDefinitionRequestDto(
                composedTestCase.metadata.title(),
                toDto(NO_TARGET),
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
        return composedSteps.stream().map(f -> convert(f, env)).collect(Collectors.toList());
    }

    private StepDefinitionRequestDto convert(ExecutableComposedStep composedStep, String env) {
        return new StepDefinitionRequestDto(
            composedStep.name,
            toDto(findTargetByName(composedStep.stepImplementation.map(si -> si.target).orElse(""), env)),
            this.mapStrategy(composedStep.strategy),
            composedStep.stepImplementation.map(si -> si.type).orElse(""),
            composedStep.stepImplementation.map(si -> si.inputs).orElse(emptyMap()),
            composedStep.steps.stream().map(f -> convert(f, env)).collect(Collectors.toList()),
            composedStep.stepImplementation.map(si -> si.outputs).orElse(emptyMap()),
            env
        );
    }

}
