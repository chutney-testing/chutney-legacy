package com.chutneytesting.agent.domain;

import com.chutneytesting.agent.domain.configure.NetworkConfiguration;
import com.chutneytesting.agent.domain.explore.ExploreResult;
import com.chutneytesting.agent.domain.network.NetworkDescription;
import com.chutneytesting.engine.domain.delegation.NamedHostAndPort;
import com.chutneytesting.agent.domain.configure.ConfigurationState;

/**
 * Used to communicate from the current local agent to a remote one.
 */
public interface AgentClient {

    /**
     * @return empty if remote agent is unreachable, otherwise, return the link <b>local -> remote</b> and all agentLinks known by the remote
     */
    ExploreResult explore(String localName, NamedHostAndPort agentInfo, NetworkConfiguration networkConfiguration);

    /**
     * Propagate final {@link NetworkDescription} to agents discovered during {@link ConfigurationState#EXPLORING} phase.
     */
    void wrapUp(NamedHostAndPort agentInfo, NetworkDescription networkDescription);

}
