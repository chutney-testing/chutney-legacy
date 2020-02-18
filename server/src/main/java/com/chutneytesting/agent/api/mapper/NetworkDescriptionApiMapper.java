package com.chutneytesting.agent.api.mapper;

import com.chutneytesting.agent.api.dto.NetworkDescriptionApiDto;
import com.chutneytesting.agent.domain.network.ImmutableNetworkDescription;
import com.chutneytesting.agent.domain.network.NetworkDescription;
import org.springframework.stereotype.Component;

@Component
public class NetworkDescriptionApiMapper {
    private final NetworkConfigurationApiMapper networkConfigurationMapper;
    private final AgentGraphApiMapper agentGraphMapper;

    public NetworkDescriptionApiMapper(NetworkConfigurationApiMapper networkConfigurationMapper, AgentGraphApiMapper agentGraphMapper) {
        this.networkConfigurationMapper = networkConfigurationMapper;
        this.agentGraphMapper = agentGraphMapper;
    }

    public NetworkDescriptionApiDto toDto(NetworkDescription networkDescription) {
        NetworkDescriptionApiDto networkDescriptionApiDto = new NetworkDescriptionApiDto();
        networkDescriptionApiDto.agentsGraph = agentGraphMapper.toDto(networkDescription.agentGraph());
        networkDescriptionApiDto.networkConfiguration = networkConfigurationMapper.toDto(networkDescription.configuration());
        return networkDescriptionApiDto;
    }

    public NetworkDescription fromDto(NetworkDescriptionApiDto networkDescriptionApiDto) {
        return ImmutableNetworkDescription.builder()
            .agentGraph(agentGraphMapper.fromDto(networkDescriptionApiDto.agentsGraph))
            .configuration(networkConfigurationMapper.fromDto(networkDescriptionApiDto.networkConfiguration))
            .build();
    }
}
