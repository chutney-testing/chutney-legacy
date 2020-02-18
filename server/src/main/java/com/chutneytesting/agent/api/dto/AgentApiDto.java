package com.chutneytesting.agent.api.dto;

import com.chutneytesting.agent.domain.network.Agent;
import java.util.List;

/**
 * DTO for {@link Agent} transport.
 */
public class AgentApiDto {
    public NetworkConfigurationApiDto.AgentInfoApiDto info;
    public List<String> reachableAgents;
    public List<TargetIdEntity> reachableTargets;
}
