package com.chutneytesting.agent.domain.configure;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.chutneytesting.agent.AgentNetworkTestUtils;
import com.chutneytesting.agent.domain.explore.CurrentNetworkDescription;
import com.chutneytesting.agent.domain.explore.ExploreAgentsService;
import com.chutneytesting.design.domain.environment.Environment;
import com.chutneytesting.design.domain.environment.EnvironmentRepository;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;

public class ConfigureServiceTest {

    @Rule public MethodRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private ExploreAgentsService exploreAgentsService;
    @Mock
    private CurrentNetworkDescription currentNetworkDescription;
    @Mock
    private LocalServerIdentifier localServerIdentifier;
    @Mock
    private EnvironmentRepository environmentRepository;

    @InjectMocks
    private ConfigureService configureService;

    @Test
    public void configure_should_explore_and_wrapup_description_while_saving_environment() {
        NetworkConfiguration networkConfiguration = AgentNetworkTestUtils.createNetworkConfiguration(
            "A=hosta:1000", "B=hostb:1000", "C=hostc:1000",
            "e1|s1=lol:45", "e1|s2=lol:46", "e2|s3=lol:47"
            );

        when(exploreAgentsService.explore(any())).thenReturn(AgentNetworkTestUtils.createExploreResult("A->B", "A->C"));
        when(localServerIdentifier.withLocalHost(same(networkConfiguration))).thenReturn(networkConfiguration);
        when(environmentRepository.getEnvironment("GLOBAL")).thenReturn(Environment.builder().withName("ENV1").build());

        configureService.configure(networkConfiguration);

        verify(exploreAgentsService).explore(networkConfiguration);
        verify(currentNetworkDescription).switchTo(any());
        verify(exploreAgentsService).wrapUp(any());
        verify(environmentRepository).save(any());
    }
}
