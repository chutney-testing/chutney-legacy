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
