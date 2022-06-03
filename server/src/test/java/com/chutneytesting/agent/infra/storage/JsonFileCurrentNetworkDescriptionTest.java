package com.chutneytesting.agent.infra.storage;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import com.chutneytesting.agent.domain.configure.LocalServerIdentifier;
import com.chutneytesting.agent.domain.configure.NetworkConfiguration;
import com.chutneytesting.agent.domain.network.Agent;
import com.chutneytesting.agent.domain.network.AgentGraph;
import com.chutneytesting.agent.domain.network.ImmutableNetworkDescription;
import com.chutneytesting.agent.domain.network.NetworkDescription;
import com.chutneytesting.environment.api.EmbeddedEnvironmentApi;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;

public class JsonFileCurrentNetworkDescriptionTest {

    JsonFileCurrentNetworkDescription underTest;

    EmbeddedEnvironmentApi environmentApi = mock(EmbeddedEnvironmentApi.class);
    AgentNetworkMapperJsonFileMapper agentNetworkMapperJsonFileMapper = mock(AgentNetworkMapperJsonFileMapper.class);
    JsonFileAgentNetworkDao jsonFileAgentNetworkDao = mock(JsonFileAgentNetworkDao.class);
    LocalServerIdentifier localServerIdentifier = mock(LocalServerIdentifier.class);

    NetworkDescription originalNetworkDescription;
    Agent originalLocalAgent = mock(Agent.class);

    @BeforeEach
    public void setUp() {
        reset(environmentApi, agentNetworkMapperJsonFileMapper, jsonFileAgentNetworkDao, localServerIdentifier);

        originalNetworkDescription = createNetworkDescription();
        NetworkDescription anotherNetworkDescription = createNetworkDescription();

        when(jsonFileAgentNetworkDao.read()).thenReturn(Optional.of(mock(AgentNetworkForJsonFile.class)));
        when(agentNetworkMapperJsonFileMapper.fromDto(any(), any())).thenReturn(originalNetworkDescription).thenReturn(anotherNetworkDescription);
        when(localServerIdentifier.findLocalAgent(any())).thenReturn(originalLocalAgent);

        underTest = new JsonFileCurrentNetworkDescription(
            environmentApi,
            agentNetworkMapperJsonFileMapper,
            jsonFileAgentNetworkDao,
            localServerIdentifier);
    }

    private NetworkDescription createNetworkDescription() {
        NetworkConfiguration networkConfiguration = mock(NetworkConfiguration.class);
        AgentGraph agentGraph = mock(AgentGraph.class);
        return ImmutableNetworkDescription.builder().configuration(networkConfiguration).agentGraph(agentGraph).build();
    }
}
