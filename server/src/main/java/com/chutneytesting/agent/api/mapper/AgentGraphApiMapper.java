package com.chutneytesting.agent.api.mapper;

import com.chutneytesting.agent.api.dto.AgentApiDto;
import com.chutneytesting.agent.api.dto.AgentsGraphApiDto;
import com.chutneytesting.agent.api.dto.TargetIdEntity;
import com.chutneytesting.agent.domain.network.Agent;
import com.chutneytesting.agent.domain.network.AgentGraph;
import com.chutneytesting.design.domain.environment.Target;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class AgentGraphApiMapper {

    private final AgentInfoApiMapper agentInfoApiMapper;

    public AgentGraphApiMapper(AgentInfoApiMapper agentInfoApiMapper) {
        this.agentInfoApiMapper = agentInfoApiMapper;
    }

    public AgentsGraphApiDto toDto(AgentGraph agentGraph) {
        AgentsGraphApiDto dto = new AgentsGraphApiDto();

        dto.agents = agentGraph.agents().stream().map(this::toDto).collect(Collectors.toList());

        return dto;
    }

    private AgentApiDto toDto(Agent agent) {
        AgentApiDto dto = new AgentApiDto();

        dto.info = agentInfoApiMapper.toDto(agent.agentInfo);
        dto.reachableAgents = agent.reachableAgents().stream().map(a -> a.agentInfo.name()).collect(Collectors.toList());
        dto.reachableTargets = agent.reachableTargets().stream().map(t -> new TargetIdEntity(t.name, t.environment)).collect(Collectors.toList());

        return dto;
    }

    public AgentGraph fromDto(AgentsGraphApiDto agentsGraph) {
        Map<String, Agent> agents = agentsGraph.agents.stream()
            .map(agent -> agent.info)
            .map(agentInfoApiMapper::fromDto)
            .map(Agent::new)
            .collect(Collectors.toMap(agent -> agent.agentInfo.name(), Function.identity()));

        for (AgentApiDto agentApiDto : agentsGraph.agents) {
            Agent agent = agents.get(agentApiDto.info.name);

            for (String remote : agentApiDto.reachableAgents)
                agent.addReachable(agents.get(remote));

            for (TargetIdEntity targetIdEntity : agentApiDto.reachableTargets)
                agent.addReachable(fromDto(targetIdEntity));
        }


        return new AgentGraph(agents.values());
    }

    private Target.TargetId fromDto(TargetIdEntity targetIdEntity) {
        return Target.TargetId.of(targetIdEntity.name, targetIdEntity.environment);
    }
}
