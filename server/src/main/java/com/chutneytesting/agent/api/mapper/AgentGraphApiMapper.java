/*
 * Copyright 2017-2023 Enedis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.chutneytesting.agent.api.mapper;

import com.chutneytesting.agent.api.dto.AgentApiDto;
import com.chutneytesting.agent.api.dto.AgentsGraphApiDto;
import com.chutneytesting.agent.api.dto.TargetIdEntity;
import com.chutneytesting.agent.domain.TargetId;
import com.chutneytesting.agent.domain.network.Agent;
import com.chutneytesting.agent.domain.network.AgentGraph;
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

    private TargetId fromDto(TargetIdEntity targetIdEntity) {
        return TargetId.of(targetIdEntity.name, targetIdEntity.environment);
    }
}
