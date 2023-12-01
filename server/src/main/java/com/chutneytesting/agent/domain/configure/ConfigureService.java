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
import com.chutneytesting.agent.domain.explore.ExploreAgentsService;
import com.chutneytesting.agent.domain.explore.ExploreResult;
import com.chutneytesting.agent.domain.network.AgentGraph;
import com.chutneytesting.agent.domain.network.ImmutableNetworkDescription;
import com.chutneytesting.agent.domain.network.NetworkDescription;
import com.chutneytesting.environment.api.EnvironmentApi;
import com.chutneytesting.environment.api.dto.EnvironmentDto;

public class ConfigureService {

    private final ExploreAgentsService exploreAgentsService;
    private final CurrentNetworkDescription currentNetworkDescription;
    private final LocalServerIdentifier localServerIdentifier;
    private final EnvironmentApi embeddedEnvironmentApi;

    public ConfigureService(ExploreAgentsService exploreAgentsService,
                            CurrentNetworkDescription currentNetworkDescription,
                            LocalServerIdentifier localServerIdentifier,
                            EnvironmentApi embeddedEnvironmentApi) {
        this.exploreAgentsService = exploreAgentsService;
        this.currentNetworkDescription = currentNetworkDescription;
        this.localServerIdentifier = localServerIdentifier;
        this.embeddedEnvironmentApi = embeddedEnvironmentApi;
    }

    public NetworkDescription configure(NetworkConfiguration networkConfiguration) {
        networkConfiguration = localServerIdentifier.withLocalHost(networkConfiguration);
        ExploreResult exploreResult = exploreAgentsService.explore(networkConfiguration);
        NetworkDescription networkDescription = buildNetworkDescription(networkConfiguration, exploreResult);
        wrapUpConfiguration(networkDescription);
        return networkDescription;
    }

    private NetworkDescription buildNetworkDescription(NetworkConfiguration networkConfiguration, ExploreResult exploreResult) {
        return ImmutableNetworkDescription.builder()
            .configuration(networkConfiguration)
            .agentGraph(AgentGraph.of(exploreResult, networkConfiguration))
            .build();
    }

    public void wrapUpConfiguration(NetworkDescription networkDescription) {
        currentNetworkDescription.switchTo(networkDescription);
        updateEnvironment(networkDescription.configuration().environmentConfiguration());
        exploreAgentsService.wrapUp(networkDescription);
    }

    private void updateEnvironment(NetworkConfiguration.EnvironmentConfiguration environmentConfigurations) {
        environmentConfigurations.stream().forEach(env -> {
            EnvironmentDto environment = new EnvironmentDto(env.name, env.description, env.targets);
            embeddedEnvironmentApi.createEnvironment(environment, true);
        });
    }
}
