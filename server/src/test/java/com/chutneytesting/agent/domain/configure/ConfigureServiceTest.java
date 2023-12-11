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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.chutneytesting.agent.AgentNetworkTestUtils;
import com.chutneytesting.agent.domain.explore.CurrentNetworkDescription;
import com.chutneytesting.agent.domain.explore.ExploreAgentsService;
import com.chutneytesting.environment.api.environment.EnvironmentApi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ConfigureServiceTest {

    private ConfigureService sut;

    private final ExploreAgentsService exploreAgentsService = mock(ExploreAgentsService.class);
    private final CurrentNetworkDescription currentNetworkDescription = mock(CurrentNetworkDescription.class);
    private final LocalServerIdentifier localServerIdentifier = mock(LocalServerIdentifier.class);
    private final EnvironmentApi environmentApi = mock(EnvironmentApi.class);

    @BeforeEach
    public void setUp() {
        sut = new ConfigureService(exploreAgentsService, currentNetworkDescription, localServerIdentifier, environmentApi);
    }

    @Test
    public void configure_should_explore_and_wrapUp_description_while_saving_environment() {
        NetworkConfiguration networkConfiguration = AgentNetworkTestUtils.createNetworkConfiguration(
            "A=hosta:1000", "B=hostb:1000", "C=hostc:1000",
            "e1|s1=lol:45", "e1|s2=lol:46", "e2|s3=lol:47"
        );

        when(exploreAgentsService.explore(networkConfiguration)).thenReturn(AgentNetworkTestUtils.createExploreResult("A->B", "A->C"));
        when(localServerIdentifier.withLocalHost(same(networkConfiguration))).thenReturn(networkConfiguration);

        sut.configure(networkConfiguration);

        verify(currentNetworkDescription).switchTo(any());
        verify(exploreAgentsService).wrapUp(any());
        verify(environmentApi).createEnvironment(any(), anyBoolean());
    }
}
