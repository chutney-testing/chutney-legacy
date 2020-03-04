package com.chutneytesting.execution.infra.execution;

import static com.chutneytesting.design.domain.environment.NoTarget.NO_TARGET;
import static java.util.Collections.emptyMap;
import static java.util.Optional.ofNullable;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import com.chutneytesting.agent.domain.explore.CurrentNetworkDescription;
import com.chutneytesting.agent.domain.network.Agent;
import com.chutneytesting.agent.domain.network.NetworkDescription;
import com.chutneytesting.design.domain.compose.ComposableTestCase;
import com.chutneytesting.design.domain.compose.FunctionalStep;
import com.chutneytesting.design.domain.environment.EnvironmentRepository;
import com.chutneytesting.design.domain.environment.Target;
import com.chutneytesting.design.domain.scenario.gwt.GwtStep;
import com.chutneytesting.design.domain.scenario.gwt.GwtTestCase;
import com.chutneytesting.design.domain.scenario.gwt.Strategy;
import com.chutneytesting.design.domain.scenario.raw.RawTestCase;
import com.chutneytesting.engine.api.execution.ExecutionRequestDto;
import com.chutneytesting.engine.api.execution.ExecutionRequestDto.StepDefinitionRequestDto;
import com.chutneytesting.engine.api.execution.TargetDto;
import com.chutneytesting.execution.domain.ExecutionRequest;
import com.chutneytesting.execution.domain.compiler.ScenarioConversionException;
import com.chutneytesting.task.api.EmbeddedTaskEngine;
import com.chutneytesting.task.api.TaskDto;
import com.chutneytesting.task.api.TaskDto.InputsDto;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.hjson.JsonValue;
import org.springframework.stereotype.Component;

@Component
public class ExecutionRequestMapper {

    private final ObjectMapper objectMapper;
    private final EnvironmentRepository environmentRepository;
    private final CurrentNetworkDescription currentNetworkDescription;
    private final EmbeddedTaskEngine embeddedTaskEngine;

    public ExecutionRequestMapper(ObjectMapper objectMapper, EnvironmentRepository environmentRepository, CurrentNetworkDescription currentNetworkDescription, EmbeddedTaskEngine embeddedTaskEngine) {
        this.objectMapper = objectMapper;
        this.environmentRepository = environmentRepository;
        this.currentNetworkDescription = currentNetworkDescription;
        this.embeddedTaskEngine = embeddedTaskEngine;
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

        if (executionRequest.testCase instanceof ComposableTestCase) {
            return convertComposable(executionRequest);
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
            definition.outputs);
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
            emptyMap()
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
            step.implementation.map(i -> i.outputs).orElse(emptyMap())
        );
    }

    private ExecutionRequestDto.StepStrategyDefinitionRequestDto mapStrategy(Strategy strategy) {
        return new ExecutionRequestDto.StepStrategyDefinitionRequestDto(
            strategy.type,
            strategy.parameters
        );
    }

    private ExecutionRequestDto.StepStrategyDefinitionRequestDto mapStrategy(com.chutneytesting.design.domain.compose.Strategy strategy) {
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
            target.security,
            target.agents
        );
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

    private StepDefinitionRequestDto convertComposable(ExecutionRequest executionRequest) {
        ComposableTestCase composableTestCase = (ComposableTestCase) executionRequest.testCase;
        try {
            return new StepDefinitionRequestDto(
                composableTestCase.metadata.title(),
                toDto(NO_TARGET),
                null,
                null,
                null,
                convertComposableSteps(composableTestCase.composableScenario.functionalSteps, executionRequest.environment),
                null
            );
        } catch (Exception e) {
            throw new ScenarioConversionException(composableTestCase.metadata().id(), e);
        }
    }

    private List<StepDefinitionRequestDto> convertComposableSteps(List<FunctionalStep> functionalSteps, String env) {
        return functionalSteps.stream().map(f -> convert(f, env)).collect(Collectors.toList());
    }

    private StepDefinitionRequestDto convert(FunctionalStep functionalStep, String env) {
        Optional<ComposableImplementation> implementation = functionalStep.implementation.map(ComposableImplementation::new);

        return new StepDefinitionRequestDto(
            functionalStep.name,
            toDto(findTargetByName(implementation.map(ComposableImplementation::targetName).orElse(""), env)),
            this.mapStrategy(functionalStep.strategy),
            implementation.map(ComposableImplementation::type).orElse(""),
            implementation.map(ComposableImplementation::inputs).orElse(emptyMap()),
            functionalStep.steps.stream().map(f -> convert(f, env)).collect(Collectors.toList()),
            implementation.map(ComposableImplementation::outputs).orElse(emptyMap())
        );
    }

    private class ComposableImplementation {

        private JsonNode implementation;

        ComposableImplementation(String jsonImplementation) {
            try {
                this.implementation = objectMapper.readTree(jsonImplementation);
            } catch (IOException e) {
                throw new ScenarioConversionException(e);
            }
        }

        String targetName() {
            return Optional.ofNullable(implementation.get("target")).orElse(TextNode.valueOf("")).textValue();
        }

        String type() {
            if (implementation.hasNonNull("identifier")) {
                return implementation.get("identifier").textValue();
            }
            return null;
        }

        Map<String, Object> outputs() {
            Map<String, Object> outputs = new HashMap<>();
            if (implementation.hasNonNull("outputs")) {
                final JsonNode outputsNode = implementation.get("outputs");
                outputsNode.forEach(in -> {
                    String name = in.get("key").asText();
                    outputs.put(name, in.get("value").asText());
                });
            }
            return outputs;
        }

        Map<String, Object> inputs() {
            Map<String, Object> inputs = new HashMap<>();
            // Simple inputs
            if (implementation.hasNonNull("inputs")) {
                final JsonNode simpleInputs = implementation.get("inputs");
                simpleInputs.forEach(in -> {
                    String inputName = in.get("name").asText();
                    inputs.put(inputName, transformSimpleInputValue(in, inputName));
                });
            }
            // List inputs
            if (implementation.hasNonNull("listInputs")) {
                final JsonNode listInputs = implementation.get("listInputs");
                listInputs.forEach(in -> {
                    List<Object> values = new ArrayList<>();
                    in.get("values").forEach(v -> values.add(transformListInputValue(v)));
                    inputs.put(in.get("name").asText(), values);
                });
            }
            // Map inputs
            if (implementation.hasNonNull("mapInputs")) {
                final JsonNode mapInputs = implementation.get("mapInputs");
                mapInputs.forEach(in -> {
                    Map<String, String> values = new HashMap<>();
                    final JsonNode jsonValues = in.get("values");
                    jsonValues.iterator().forEachRemaining(n -> values.put(n.get("key").asText(), n.get("value").asText()));
                    inputs.put(in.get("name").asText(), values);
                });
            }
            return inputs;
        }

        private Object transformSimpleInputValue(JsonNode in, String inputRead) {
            Optional<TaskDto> task = embeddedTaskEngine.getAllTasks().stream().filter(t -> t.getIdentifier().equals(this.type())).findFirst();
            if (task.isPresent()) {
                Optional<InputsDto> optionalInput = task.get().getInputs().stream().filter(i -> i.getName().equals(inputRead)).findFirst();
                if (optionalInput.isPresent()) {
                    InputsDto input = optionalInput.get();
                    if (input.getType().getName().equals(Integer.class.getName())) {
                        return transformIntegerValue(in);
                    }
                }
            }

            String value = in.get("value").asText();
            return !value.isEmpty() ? value : null;
        }

        private Object transformListInputValue(JsonNode in) {
            if (in.isObject()) {
                try {
                    return objectMapper.readValue(in.toString(), HashMap.class);
                } catch (Exception e) {
                    return in.toString();
                }
            }
            return in.asText();
        }
    }

    private Integer transformIntegerValue(JsonNode in) {
        String value = in.get("value").asText();
        return StringUtils.isNotBlank(value) ? Integer.valueOf(value) : null;
    }
}
