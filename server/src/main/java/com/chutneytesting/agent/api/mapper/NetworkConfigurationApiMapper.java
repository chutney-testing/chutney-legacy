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

import static java.util.stream.Collectors.toSet;

import com.chutneytesting.agent.api.dto.NetworkConfigurationApiDto;
import com.chutneytesting.agent.domain.configure.ImmutableNetworkConfiguration;
import com.chutneytesting.agent.domain.configure.ImmutableNetworkConfiguration.AgentNetworkConfiguration;
import com.chutneytesting.agent.domain.configure.NetworkConfiguration;
import com.chutneytesting.environment.api.dto.EnvironmentDto;
import java.time.Instant;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class NetworkConfigurationApiMapper {

    private final AgentInfoApiMapper agentInfoMapper;
    private final EnvironmentApiMapper targetApiMapper;

    public NetworkConfigurationApiMapper(AgentInfoApiMapper agentInfoMapper, EnvironmentApiMapper targetApiMapper) {
        this.agentInfoMapper = agentInfoMapper;
        this.targetApiMapper = targetApiMapper;
    }

    public NetworkConfigurationApiDto toDto(NetworkConfiguration networkConfiguration) {
        NetworkConfigurationApiDto dto = new NetworkConfigurationApiDto();

        dto.creationDate = networkConfiguration.creationDate();
        dto.agentNetworkConfiguration = networkConfiguration.agentNetworkConfiguration().agentInfos().stream()
            .map(agentInfoMapper::toDto)
            .collect(toSet());

        dto.environmentsConfiguration = networkConfiguration.environmentConfiguration().stream()
            .map(targetApiMapper::toDto)
            .collect(toSet());

        return dto;
    }

    public NetworkConfiguration fromDto(NetworkConfigurationApiDto dto) {
        return fromDtoAt(dto, dto.creationDate);
    }

    public NetworkConfiguration fromDtoAtNow(NetworkConfigurationApiDto dto) {
        return fromDtoAt(dto, Instant.now());
    }

    private NetworkConfiguration fromDtoAt(NetworkConfigurationApiDto entity, Instant creation) {
        return ImmutableNetworkConfiguration.builder()
            .agentNetworkConfiguration(
                AgentNetworkConfiguration.builder()
                    .agentInfos(
                        entity.agentNetworkConfiguration.stream()
                            .map(agentInfoMapper::fromDto)
                            .collect(toSet()))

                    .build())
            .environmentConfiguration(ImmutableNetworkConfiguration.EnvironmentConfiguration.builder()
                .environments(entity.environmentsConfiguration.stream().map(targetApiMapper::fromDto).collect(toSet()))
                .build())
            .creationDate(creation)
            .build();
    }

    public NetworkConfiguration enhanceWithEnvironment(NetworkConfiguration networkConfiguration, Set<EnvironmentDto> localEnvironments) {
        return ImmutableNetworkConfiguration.builder()
            .from(networkConfiguration)
            .environmentConfiguration(ImmutableNetworkConfiguration.EnvironmentConfiguration.builder()
                .addAllEnvironments(localEnvironments)
                .build())
            .build();
    }
}
