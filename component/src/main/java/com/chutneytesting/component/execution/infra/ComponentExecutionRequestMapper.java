package com.chutneytesting.component.execution.infra;

import static com.chutneytesting.environment.api.dto.NoTargetDto.NO_TARGET_DTO;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isBlank;

import com.chutneytesting.agent.domain.explore.CurrentNetworkDescription;
import com.chutneytesting.agent.domain.network.Agent;
import com.chutneytesting.agent.domain.network.NetworkDescription;
import com.chutneytesting.component.execution.domain.ExecutableComposedStep;
import com.chutneytesting.component.execution.domain.ExecutableComposedTestCase;
import com.chutneytesting.engine.api.execution.ExecutionRequestDto;
import com.chutneytesting.engine.api.execution.ExecutionRequestDto.StepDefinitionRequestDto;
import com.chutneytesting.engine.api.execution.TargetExecutionDto;
import com.chutneytesting.engine.domain.delegation.NamedHostAndPort;
import com.chutneytesting.environment.api.EmbeddedEnvironmentApi;
import com.chutneytesting.environment.api.EnvironmentApi;
import com.chutneytesting.environment.api.dto.TargetDto;
import com.chutneytesting.execution.infra.execution.DefaultExecutionRequestMapper;
import com.chutneytesting.execution.infra.execution.ExecutionRequestMapper;
import com.chutneytesting.scenario.domain.gwt.Strategy;
import com.chutneytesting.server.core.domain.execution.ExecutionRequest;
import com.chutneytesting.server.core.domain.execution.ScenarioConversionException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@Component
public class ComponentExecutionRequestMapper implements ExecutionRequestMapper {

    private final EnvironmentApi environmentApi;
    private final CurrentNetworkDescription currentNetworkDescription;
    private final DefaultExecutionRequestMapper defaultExecutionRequestMapper;

    public ComponentExecutionRequestMapper(ObjectMapper objectMapper, EmbeddedEnvironmentApi environmentApi, CurrentNetworkDescription currentNetworkDescription) {
        this.environmentApi = environmentApi;
        this.currentNetworkDescription = currentNetworkDescription;
        this.defaultExecutionRequestMapper = new DefaultExecutionRequestMapper(objectMapper, environmentApi, currentNetworkDescription);
    }

    @Override
    public ExecutionRequestDto toDto(ExecutionRequest executionRequest) {
        if (executionRequest.testCase.id().contains("-")) {
            final StepDefinitionRequestDto stepDefinitionRequestDto = convertToStepDef(executionRequest);
            return new ExecutionRequestDto(stepDefinitionRequestDto, executionRequest.environment);
        } else {
            return this.defaultExecutionRequestMapper.toDto(executionRequest);
        }
    }

    private StepDefinitionRequestDto convertToStepDef(ExecutionRequest executionRequest) {
        if (executionRequest.testCase.id().contains("-")) {
            return convertComposed(executionRequest);
        }

        throw new ScenarioConversionException(executionRequest.testCase.metadata().id(),
            "Cannot create an executable StepDefinition from a " + executionRequest.testCase.getClass().getCanonicalName());
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
                null
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
            composedStep.stepImplementation.map(si -> si.validations).orElse(emptyMap())
        );
    }

    private ExecutionRequestDto.StepStrategyDefinitionRequestDto mapStrategy(Strategy strategy) {
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
            getAgents(targetDto, env)
        );
    }

    private TargetDto getTargetForExecution(String environmentName, String targetName) {
        if (isBlank(targetName)) {
            return NO_TARGET_DTO;
        }
        return environmentApi.getTarget(environmentName, targetName);
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
