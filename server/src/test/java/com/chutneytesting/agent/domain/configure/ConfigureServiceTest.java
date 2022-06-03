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
import com.chutneytesting.environment.api.EnvironmentApi;
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
