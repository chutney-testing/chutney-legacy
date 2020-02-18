package com.chutneytesting.agent.infra.storage;

import static java.util.Optional.ofNullable;
import static java.util.function.Function.identity;

import com.chutneytesting.agent.domain.configure.ImmutableNetworkConfiguration;
import com.chutneytesting.agent.domain.network.Agent;
import com.chutneytesting.agent.domain.network.AgentGraph;
import com.chutneytesting.agent.domain.network.ImmutableNetworkDescription;
import com.chutneytesting.agent.domain.network.NetworkDescription;
import com.chutneytesting.design.domain.environment.Environment;
import com.chutneytesting.design.domain.environment.Target;
import com.chutneytesting.engine.domain.delegation.NamedHostAndPort;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class AgentNetworkMapperJsonFileMapper {

    AgentNetworkForJsonFile toDto(NetworkDescription networkDescription) {
        AgentNetworkForJsonFile dto = new AgentNetworkForJsonFile();

        dto.configurationCreationDate = networkDescription.configuration().creationDate();
        dto.agents = networkDescription.agentGraph().agents().stream()
            .map(this::toDto)
            .collect(Collectors.toList());

        return dto;
    }

    private AgentForJsonFile toDto(Agent agent) {
        AgentForJsonFile dto = new AgentForJsonFile();

        dto.name = agent.agentInfo.name();
        dto.host = agent.agentInfo.host();
        dto.port = agent.agentInfo.port();
        dto.reachableAgentNames = agent.reachableAgents().stream()
            .map(_agent -> _agent.agentInfo.name())
            .collect(Collectors.toList());
        dto.reachableTargetIds = agent.reachableTargets().stream().map(target -> {
            TargetForJsonFile targetDto = new TargetForJsonFile();
            targetDto.name = target.name;
            targetDto.environment = target.environment;
            return targetDto;
        }).collect(Collectors.toList());

        return dto;
    }

    NetworkDescription fromDto(AgentNetworkForJsonFile dto, List<Environment> environment) {
        List<Agent> agents = ofNullable(dto.agents)
            .map(this::fromDto)
            .orElse(Collections.emptyList());

        ImmutableNetworkConfiguration.Builder configuration = ImmutableNetworkConfiguration.builder().creationDate(dto.configurationCreationDate);

        configuration.agentNetworkConfiguration(ImmutableNetworkConfiguration.AgentNetworkConfiguration.of(agents.stream()
            .map(agent -> agent.agentInfo)
            .collect(Collectors.toList())));

        configuration.environmentConfiguration(ImmutableNetworkConfiguration.EnvironmentConfiguration.of(environment));

        return ImmutableNetworkDescription.builder()
            .configuration(configuration.build())
            .agentGraph(new AgentGraph(agents))
            .build();
    }

    private Iterable<Target> getTargets(Collection<AgentForJsonFile> agents, List<Target> targets) {
        return agents.stream()
            .flatMap(agent -> agent.reachableTargetIds.stream())
            .map(targetId -> targets.stream()
                .filter(target -> target.name.equals(targetId.name))
                .findAny())
            .collect(Collectors.toList())
            // If name does not match in environment, fuck it !!
            .stream()
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toList());
    }

    private List<Agent> fromDto(Collection<AgentForJsonFile> dtos) {
        Map<String, Agent> agentsByName = dtos.stream()
            .map(dto -> new Agent(new NamedHostAndPort(dto.name, dto.host, dto.port)))
            .collect(Collectors.toMap(
                (Agent agent) -> agent.agentInfo.name(),
                identity(),
                (dto1, dto2) -> {
                    throw new IllegalStateException("saveral agents are named : " + dto1.agentInfo.name());
                },
                LinkedHashMap::new));

        for (AgentForJsonFile dto : dtos) {
            Agent agent = agentsByName.get(dto.name);
            addAgentLinks(agent, dto, agentsByName);
            addTargetLinks(agent, dto);
        }

        return new ArrayList<>(agentsByName.values());
    }

    private void addTargetLinks(Agent agent, AgentForJsonFile dto) {
        dto.reachableTargetIds.stream()
            .map(target -> Target.TargetId.of(target.name, target.environment))
            .forEach(agent::addReachable);
    }

    private void addAgentLinks(Agent agent, AgentForJsonFile dto, Map<String, Agent> agentsByName) {
        for (String reachableAgentName : dto.reachableAgentNames) {
            Agent reachableAgent = agentsByName.get(reachableAgentName);
            if (reachableAgent == null)
                throw new IllegalStateException(String.format(
                    "the agent %s contains a link to the agent %S, but the linked agent does not exist",
                    dto.name,
                    reachableAgentName));
            agent.addReachable(reachableAgent);
        }
    }
}
