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

package com.chutneytesting.agent.domain;

import com.chutneytesting.agent.domain.configure.ConfigurationState;
import com.chutneytesting.agent.domain.configure.NetworkConfiguration;
import com.chutneytesting.agent.domain.explore.ExploreResult;
import com.chutneytesting.agent.domain.network.NetworkDescription;
import com.chutneytesting.engine.domain.delegation.NamedHostAndPort;

/**
 * Used to communicate from the current local agent to a remote one.
 */
public interface AgentClient {

    /**
     * @return empty if remote agent is unreachable, otherwise, return the link <b>from local to remote</b> and all agentLinks known by the remote
     */
    ExploreResult explore(String localName, NamedHostAndPort agentInfo, NetworkConfiguration networkConfiguration);

    /**
     * Propagate final {@link NetworkDescription} to agents discovered during {@link ConfigurationState#EXPLORING} phase.
     */
    void wrapUp(NamedHostAndPort agentInfo, NetworkDescription networkDescription);

}
