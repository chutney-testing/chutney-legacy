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
