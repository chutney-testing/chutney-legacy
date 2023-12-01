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

import static com.chutneytesting.agent.domain.configure.ImmutableNetworkConfiguration.AgentNetworkConfiguration.of;
import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;

import com.chutneytesting.agent.api.dto.AgentApiDto;
import com.chutneytesting.agent.api.dto.AgentsGraphApiDto;
import com.chutneytesting.agent.api.dto.NetworkConfigurationApiDto;
import com.chutneytesting.agent.api.dto.NetworkConfigurationApiDto.EnvironmentApiDto;
import com.chutneytesting.agent.api.dto.NetworkConfigurationApiDto.TargetsApiDto;
import com.chutneytesting.agent.api.dto.NetworkDescriptionApiDto;
import com.chutneytesting.agent.domain.configure.ImmutableNetworkConfiguration;
import com.chutneytesting.agent.domain.configure.NetworkConfiguration;
import com.chutneytesting.agent.domain.explore.AgentId;
import com.chutneytesting.agent.domain.explore.ExploreResult;
import com.chutneytesting.agent.domain.explore.ImmutableExploreResult;
import com.chutneytesting.agent.domain.network.AgentGraph;
import com.chutneytesting.agent.domain.network.ImmutableNetworkDescription;
import com.chutneytesting.agent.domain.network.NetworkDescription;
import com.chutneytesting.engine.domain.delegation.NamedHostAndPort;
import com.chutneytesting.environment.api.dto.EnvironmentDto;
import com.chutneytesting.environment.api.dto.TargetDto;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

public class NetworkDescriptionApiMapperTest {

    private final NetworkDescriptionApiMapper networkDescriptionApiMapper = new NetworkDescriptionApiMapper(
        new NetworkConfigurationApiMapper(new AgentInfoApiMapper(), new EnvironmentApiMapper()),
        new AgentGraphApiMapper(new AgentInfoApiMapper()));

    @Test
    public void toDto_basic_test() {
        List<TargetDto> targets = Arrays.asList(
            createTarget("s1", "proto://lol:75/truc"),
            createTarget("s2", "proto://lol3:75/truc")
        );
        EnvironmentDto environment = new EnvironmentDto("env", null, targets);
        Set<EnvironmentDto> environments = new HashSet<>();
        environments.add(environment);

        NetworkConfiguration networkConfiguration = ImmutableNetworkConfiguration.builder()
            .creationDate(Instant.now())
            .agentNetworkConfiguration(of(Arrays.asList(
                new NamedHostAndPort("name1", "host", 1000),
                new NamedHostAndPort("name2", "host2", 1001))))
            .environmentConfiguration(ImmutableNetworkConfiguration.EnvironmentConfiguration.of(environments))
            .build();

        ExploreResult exploreResult = ImmutableExploreResult.of(
            ImmutableExploreResult.Links.of(
                Collections.singleton(ImmutableExploreResult.Link.of(AgentId.of("name1"), AgentId.of("name2")))
            ), ExploreResult.Links.empty()
        );

        NetworkDescription networkDescription = ImmutableNetworkDescription.builder()
            .agentGraph(AgentGraph.of(exploreResult, networkConfiguration))
            .configuration(networkConfiguration)
            .build();

        NetworkDescriptionApiDto dto = networkDescriptionApiMapper.toDto(networkDescription);

        assertThat(dto.networkConfiguration.creationDate).isEqualTo(networkConfiguration.creationDate());
        assertThat(dto.networkConfiguration.agentNetworkConfiguration.size()).isEqualTo(2);
        assertThat(dto.agentsGraph.agents).hasSize(2);
    }

    private TargetDto createTarget(String name, String url) {
        return new TargetDto(name, url, emptySet());
    }

    @Test
    public void fromDto_basic_test() {
        NetworkConfigurationApiDto.AgentInfoApiDto agentInfoApiDto = new NetworkConfigurationApiDto.AgentInfoApiDto();
        agentInfoApiDto.name = "name";
        agentInfoApiDto.host = "host";
        agentInfoApiDto.port = 1000;

        AgentApiDto agentApiDto = new AgentApiDto();
        agentApiDto.info = agentInfoApiDto;
        agentApiDto.reachableAgents = new ArrayList<>();
        agentApiDto.reachableTargets = new ArrayList<>();

        NetworkDescriptionApiDto dto = new NetworkDescriptionApiDto();
        dto.networkConfiguration = new NetworkConfigurationApiDto();
        dto.networkConfiguration.creationDate = Instant.now();
        dto.networkConfiguration.agentNetworkConfiguration = new HashSet<>();
        dto.networkConfiguration.agentNetworkConfiguration.add(agentInfoApiDto);
        dto.networkConfiguration.environmentsConfiguration = new HashSet<>();
        dto.networkConfiguration.environmentsConfiguration.add(createTargetInfoApiDto("s1", "pro://host4:456/12"));


        dto.agentsGraph = new AgentsGraphApiDto();
        dto.agentsGraph.agents = Collections.singletonList(agentApiDto);

        NetworkDescription networkDescription = networkDescriptionApiMapper.fromDto(dto);

        assertThat(networkDescription.configuration()).isNotNull();
        assertThat(networkDescription.agentGraph().agents()).hasSize(1);
    }

    private EnvironmentApiDto createTargetInfoApiDto(String name, String url) {
        TargetsApiDto targetsApiDto = new TargetsApiDto(name, url, null);
        Set<TargetsApiDto> targets = new HashSet<>();
        targets.add(targetsApiDto);
        return new EnvironmentApiDto("env", targets);
    }
}
