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
