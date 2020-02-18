package com.chutneytesting.agent.domain.configure;

import com.chutneytesting.agent.domain.explore.CurrentNetworkDescription;
import com.chutneytesting.agent.domain.network.AgentGraph;
import com.chutneytesting.agent.domain.network.ImmutableNetworkDescription;
import com.chutneytesting.agent.domain.network.NetworkDescription;
import java.time.Instant;
import java.util.Collections;

public class GetCurrentNetworkDescriptionService {

    private final CurrentNetworkDescription currentNetworkDescription;
    private final NetworkDescription defaultCurrent = ImmutableNetworkDescription.builder()
        .agentGraph(new AgentGraph(Collections.emptyList()))
        .configuration(ImmutableNetworkConfiguration.builder()
            .creationDate(Instant.now())
            .agentNetworkConfiguration(ImmutableNetworkConfiguration.AgentNetworkConfiguration.of(Collections.emptySet()))
            .environmentConfiguration(ImmutableNetworkConfiguration.EnvironmentConfiguration.of(Collections.emptySet()))
            .build())
        .build();

    public GetCurrentNetworkDescriptionService(CurrentNetworkDescription currentNetworkDescription) {
        this.currentNetworkDescription = currentNetworkDescription;
    }

    public NetworkDescription getCurrentNetworkDescription() {
        return currentNetworkDescription.findCurrent()
            .orElse(defaultCurrent);
    }
}
