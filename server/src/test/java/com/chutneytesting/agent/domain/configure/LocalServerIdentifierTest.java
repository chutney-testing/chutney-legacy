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

package com.chutneytesting.agent.domain.configure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.chutneytesting.agent.domain.network.Agent;
import com.chutneytesting.agent.domain.network.AgentGraph;
import com.chutneytesting.engine.domain.delegation.NamedHostAndPort;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;
import java.util.Collections;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

public class LocalServerIdentifierTest {

    LocalServerIdentifier localServerIdentifier = new LocalServerIdentifier(42, "defaultLocalName", "localhost");

    @Test
    public void getLocalName_returns_empty_if_no_host_is_local() {
        NetworkConfiguration networkConfiguration = createNetConf(
            new NamedHostAndPort("testName1", "host1", 1),
            new NamedHostAndPort("testName2", "host2", 1));

        String localName = localServerIdentifier.getLocalName(networkConfiguration);

        assertThat(localName).isEqualTo("defaultLocalName");
    }

    @Test
    public void getLocalName_returns_name_if_an_host_is_local() throws UnknownHostException {
        NetworkConfiguration networkConfiguration = createNetConf(
            new NamedHostAndPort("testName1", "host1", 1),
            new NamedHostAndPort("testName2", InetAddress.getLocalHost().getCanonicalHostName(), 42)
        );

        assertThat(localServerIdentifier.getLocalName(networkConfiguration)).isEqualTo("testName2");
    }

    @Test
    public void getLocalName_returns_name_if_an_host_is_loopback() {
        NetworkConfiguration networkConfiguration = createNetConf(
            new NamedHostAndPort("testName1", "host1", 1),
            new NamedHostAndPort("testName2", "127.0.0.1", 42)
        );

        assertThat(localServerIdentifier.getLocalName(networkConfiguration)).isEqualTo("testName2");
    }

    @Test
    public void should_find_local_agent_in_given_graph() {
        Agent agent = new Agent(new NamedHostAndPort("toto", "127.0.0.1", 42));
        AgentGraph agentGraph = new AgentGraph(Collections.singletonList(agent));

        assertThat(localServerIdentifier.findLocalAgent(agentGraph)).isEqualTo(agent);
    }

    @Test
    public void should_throw_when_local_agent_is_absent_from_given_graph() {
        Agent agent = new Agent(new NamedHostAndPort("bou", "bou", 24));
        AgentGraph agentGraph = new AgentGraph(Collections.singletonList(agent));

        assertThatExceptionOfType(IllegalStateException.class)
            .isThrownBy(() -> localServerIdentifier.findLocalAgent(agentGraph))
            .withMessageContaining("Impossible to find");
    }

    @Test public void withLocal_should_add_local_if_missing() {
        NetworkConfiguration networkConfiguration = createNetConf(new NamedHostAndPort("testName1", "host1", 1));

        networkConfiguration = localServerIdentifier.withLocalHost(networkConfiguration);

        assertThat(networkConfiguration.agentNetworkConfiguration().agentInfos())
            .contains(new NamedHostAndPort("defaultLocalName", "localhost", 42));
    }

    @Test public void withLocal_should_do_nothing_if_local_is_already_there() {
        NetworkConfiguration networkConfiguration = createNetConf(
            new NamedHostAndPort("testName1", "host1", 1),
            new NamedHostAndPort("testName1", "localhost", 1));

        networkConfiguration = localServerIdentifier.withLocalHost(networkConfiguration);

        assertThat(networkConfiguration.agentNetworkConfiguration().agentInfos())
            .contains(new NamedHostAndPort("defaultLocalName", "localhost", 42));
    }

    private ImmutableNetworkConfiguration createNetConf(NamedHostAndPort... nhp) {
        ImmutableNetworkConfiguration.AgentNetworkConfiguration.Builder agentConf = ImmutableNetworkConfiguration.AgentNetworkConfiguration.builder();

        Stream.of(nhp).forEach(agentConf::addAgentInfos);

        return ImmutableNetworkConfiguration.builder()
                                            .creationDate(Instant.now())
                                            .agentNetworkConfiguration(agentConf.build())
                                            .environmentConfiguration(ImmutableNetworkConfiguration.EnvironmentConfiguration.builder().build())
                                            .build();
    }
}

