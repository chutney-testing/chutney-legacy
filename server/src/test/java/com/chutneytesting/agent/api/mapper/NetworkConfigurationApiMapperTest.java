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

import static com.chutneytesting.agent.domain.configure.ImmutableNetworkConfiguration.AgentNetworkConfiguration.builder;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static util.WaitUtils.awaitDuring;

import com.chutneytesting.agent.api.dto.NetworkConfigurationApiDto;
import com.chutneytesting.agent.api.dto.NetworkConfigurationApiDto.EnvironmentApiDto;
import com.chutneytesting.agent.api.dto.NetworkConfigurationApiDto.TargetsApiDto;
import com.chutneytesting.agent.domain.configure.ImmutableNetworkConfiguration;
import com.chutneytesting.agent.domain.configure.ImmutableNetworkConfiguration.AgentNetworkConfiguration;
import com.chutneytesting.agent.domain.configure.NetworkConfiguration;
import com.chutneytesting.engine.domain.delegation.NamedHostAndPort;
import com.chutneytesting.environment.api.environment.dto.EnvironmentDto;
import com.chutneytesting.environment.api.target.dto.TargetDto;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

public class NetworkConfigurationApiMapperTest {

    private final NetworkConfigurationApiMapper networkConfigurationApiMapper = new NetworkConfigurationApiMapper(new AgentInfoApiMapper(), new EnvironmentApiMapper());

    @Test
    public void fromDto() {
        NetworkConfigurationApiDto dto = new NetworkConfigurationApiDto();
        dto.creationDate = Instant.now();
        NetworkConfigurationApiDto.AgentInfoApiDto agentInfoApiDto = new NetworkConfigurationApiDto.AgentInfoApiDto();
        agentInfoApiDto.name = "name";
        agentInfoApiDto.host = "host";
        agentInfoApiDto.port = 1000;
        dto.agentNetworkConfiguration = new LinkedHashSet<>(singletonList(agentInfoApiDto));

        TargetsApiDto targetsApiDto = new TargetsApiDto("s1", "proto://host:12/lol", null);

        LinkedHashSet<TargetsApiDto> targetSet = new LinkedHashSet<>();
        targetSet.add(targetsApiDto);
        EnvironmentApiDto envDto = new EnvironmentApiDto("name", targetSet);
        LinkedHashSet<EnvironmentApiDto> envSet = new LinkedHashSet<>();
        envSet.add(envDto);
        dto.environmentsConfiguration = envSet;
        new LinkedHashSet<>(singletonList(targetsApiDto));

        NetworkConfiguration networkConfiguration = networkConfigurationApiMapper.fromDto(dto);

        assertThat(networkConfiguration.creationDate()).isEqualTo(dto.creationDate);
        Set<NamedHostAndPort> agentInfos = networkConfiguration.agentNetworkConfiguration().agentInfos();
        assertThat(agentInfos).hasSize(1);
        assertThat(agentInfos).haveExactly(1, new Condition<>(agentInfo -> "name".equals(agentInfo.name()), "agent with name"));
        assertThat(agentInfos).haveExactly(1, new Condition<>(agentInfo -> "host".equals(agentInfo.host()), "agent with host"));
        assertThat(agentInfos).haveExactly(1, new Condition<>(agentInfo -> agentInfo.port() == 1000, "agent with port 1000"));

        Set<EnvironmentDto> env = networkConfiguration.environmentConfiguration().environments();
        assertThat(env).hasSize(1);
        EnvironmentDto environment = env.iterator().next();

        List<TargetDto> targets = environment.targets;
        assertThat(targets).hasSize(1);

        TargetDto singleValue = targets.iterator().next();
        assertThat(singleValue.name).as("target name").isEqualTo("s1");
        assertThat(singleValue.url).as("target url").isEqualTo("proto://host:12/lol");
        assertThat(singleValue.properties).as("target properties").isEmpty();
    }

    @Test
    public void fromDtoAtNow_use_another_instant() {
        NetworkConfigurationApiDto dto = new NetworkConfigurationApiDto();
        dto.creationDate = Instant.now();
        NetworkConfigurationApiDto.AgentInfoApiDto agentInfoApiDto = new NetworkConfigurationApiDto.AgentInfoApiDto();
        agentInfoApiDto.name = "name";
        agentInfoApiDto.host = "host";
        agentInfoApiDto.port = 1000;
        dto.agentNetworkConfiguration = new LinkedHashSet<>(singletonList(agentInfoApiDto));

        TargetsApiDto targetsApiDto = new TargetsApiDto("s1", "proto://host:1/lol", new HashMap<>());
        EnvironmentApiDto envApiDto = new EnvironmentApiDto("envName", singleton(targetsApiDto));
        dto.environmentsConfiguration = new LinkedHashSet<>(singletonList(envApiDto));

        awaitDuring(1, MILLISECONDS);
        NetworkConfiguration networkConfiguration = networkConfigurationApiMapper.fromDtoAtNow(dto);

        assertThat(networkConfiguration.creationDate()).isNotEqualTo(dto.creationDate);
    }

    @Test
    public void toDTO_basic_test() {

        List<TargetDto> targets = new ArrayList<>();
        targets.add(new TargetDto("s2", "pro://host2:45/lol", emptySet()));

        EnvironmentDto env = new EnvironmentDto("name", null, targets, emptyList());

        ImmutableNetworkConfiguration networkConfiguration = ImmutableNetworkConfiguration.builder()
            .creationDate(Instant.now())
            .agentNetworkConfiguration(builder()
                .addAgentInfos(new NamedHostAndPort("name", "host", 1000))
                .build())
            .environmentConfiguration(ImmutableNetworkConfiguration.EnvironmentConfiguration.builder()
                .addEnvironments(env).build())
            .build();


        NetworkConfigurationApiDto dto = networkConfigurationApiMapper.toDto(networkConfiguration);

        assertThat(dto.creationDate).isEqualTo(networkConfiguration.creationDate());
        assertThat(dto.agentNetworkConfiguration).hasSize(1);
        assertThat(dto.agentNetworkConfiguration).haveExactly(1, new Condition<>(agentInfoDto ->
            "name".equals(agentInfoDto.name) &&
                "host".equals(agentInfoDto.host) &&
                agentInfoDto.port == 1000,
            "right agent"));

        assertThat(dto.environmentsConfiguration).hasSize(1);
        EnvironmentApiDto envToAssert = dto.environmentsConfiguration.iterator().next();
        assertThat(envToAssert.targetsConfiguration).hasSize(1);
        TargetsApiDto targetsApiDto = envToAssert.targetsConfiguration.iterator().next();
        assertThat(targetsApiDto.name).as("DTO target name").isEqualTo("s2");
        assertThat(targetsApiDto.url).as("DTO target url").isEqualTo("pro://host2:45/lol");
        assertThat(targetsApiDto.properties).as("DTO target properties").isEmpty();
    }

    @Test
    public void enhanceWithEnvironment_basic_test() {

        EnvironmentDto env = new EnvironmentDto("env_name");

        NetworkConfiguration networkConfiguration = ImmutableNetworkConfiguration.builder()
            .creationDate(Instant.now())
            .agentNetworkConfiguration(AgentNetworkConfiguration.builder()
                .addAgentInfos(new NamedHostAndPort("name", "host", 1000))
                .build())
            .environmentConfiguration(ImmutableNetworkConfiguration.EnvironmentConfiguration.builder()
                .addEnvironments(env).build())
            .build();

        List<TargetDto> targets = new ArrayList<>();
        targets.add(new TargetDto("APP_1", "https://host_of_app:443/api", emptySet()));
        EnvironmentDto newEnv = new EnvironmentDto("env_name", null, targets, emptyList());
        Set<EnvironmentDto> newEnvs = new HashSet<>();
        newEnvs.add(newEnv);
        NetworkConfiguration enhancedNetworkConfiguration = networkConfigurationApiMapper.enhanceWithEnvironment(networkConfiguration, newEnvs);

        assertThat(enhancedNetworkConfiguration.agentNetworkConfiguration().agentInfos())
            .as("Agent infos")
            .hasSize(1);

        assertThat(enhancedNetworkConfiguration.environmentConfiguration().environments()).hasSize(1);
        EnvironmentDto environment = enhancedNetworkConfiguration.environmentConfiguration().environments().iterator().next();
        assertThat(environment.targets)
            .as("Target infos")
            .hasSize(1)
            .extracting(targetInfo -> targetInfo.name + " -> " + targetInfo.url)
            .containsExactlyInAnyOrder("APP_1 -> https://host_of_app:443/api");
    }
}
