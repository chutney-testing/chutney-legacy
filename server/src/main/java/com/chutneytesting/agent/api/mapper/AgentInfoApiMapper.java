package com.chutneytesting.agent.api.mapper;

import com.chutneytesting.agent.api.dto.NetworkConfigurationApiDto;
import com.chutneytesting.engine.domain.delegation.NamedHostAndPort;
import org.springframework.stereotype.Component;

@Component
public class AgentInfoApiMapper {

    public NetworkConfigurationApiDto.AgentInfoApiDto toDto(NamedHostAndPort agentInfo) {
        NetworkConfigurationApiDto.AgentInfoApiDto dto = new NetworkConfigurationApiDto.AgentInfoApiDto();
        dto.name = agentInfo.name();
        dto.host = agentInfo.host();
        dto.port = agentInfo.port();
        return dto;
    }

    public NamedHostAndPort fromDto(NetworkConfigurationApiDto.AgentInfoApiDto entity) {
        return new NamedHostAndPort(entity.name, entity.host, entity.port);
    }
}
