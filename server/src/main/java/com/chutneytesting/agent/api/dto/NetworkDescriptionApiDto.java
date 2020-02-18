package com.chutneytesting.agent.api.dto;

import com.chutneytesting.agent.domain.network.NetworkDescription;

/**
 * DTO for {@link NetworkDescription} transport.
 */
public class NetworkDescriptionApiDto {
    public AgentsGraphApiDto agentsGraph;
    public NetworkConfigurationApiDto networkConfiguration;
}
