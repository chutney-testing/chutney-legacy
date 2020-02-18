package com.chutneytesting.agent.api.dto;

import com.chutneytesting.agent.domain.network.AgentGraph;
import java.util.List;

/**
 * DTO for {@link AgentGraph} transport.
 */
public class AgentsGraphApiDto {
    public List<AgentApiDto> agents;
}
