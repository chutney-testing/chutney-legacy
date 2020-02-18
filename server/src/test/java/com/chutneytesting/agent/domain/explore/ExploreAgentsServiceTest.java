package com.chutneytesting.agent.domain.explore;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.chutneytesting.agent.AgentNetworkTestUtils;
import com.chutneytesting.agent.domain.AgentClient;
import com.chutneytesting.agent.domain.configure.ConfigurationState;
import com.chutneytesting.agent.domain.configure.Explorations;
import com.chutneytesting.agent.domain.configure.LocalServerIdentifier;
import com.chutneytesting.agent.domain.configure.NetworkConfiguration;
import com.chutneytesting.agent.domain.network.NetworkDescription;
import com.chutneytesting.engine.domain.delegation.ConnectionChecker;
import com.chutneytesting.engine.domain.delegation.NamedHostAndPort;
import java.util.stream.Stream;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;

@SuppressWarnings("WeakerAccess")
public class ExploreAgentsServiceTest {

    @Rule public MethodRule mockitoRule = MockitoJUnit.rule();

    @Mock Explorations explorations;
    @Mock AgentClient agentClient;
    @Mock LocalServerIdentifier localServerIdentifier;
    @Mock ConnectionChecker connectionChecker;

    ExploreAgentsService exploreAgentsService;

    @Before public void setUp() {
        exploreAgentsService = new ExploreAgentsService(
            explorations,
            agentClient,
            connectionChecker,
            localServerIdentifier);
    }

    @Test
    public void explore_with_empty_configuration_returns_empty_links() {
        NetworkConfiguration configuration = AgentNetworkTestUtils.createNetworkConfiguration("Env-NAME");

        ExploreResult exploreResult = exploreAgentsService.explore(configuration);

        assertThat(exploreResult.agentLinks()).isEmpty();
    }

    @Test
    public void explore_with_configuration_containing_reachable_agents_returns_links_to_these_agents() {
        when(explorations.changeStateToIfPossible(any(), any())).thenReturn(true);
        when(localServerIdentifier.getLocalName(any())).thenReturn("A");
        when(agentClient.explore(eq("A"), ArgumentMatchers.eq(AgentNetworkTestUtils.createAgentInfo("B=reachable:1").get()), any()))
            .thenReturn(AgentNetworkTestUtils.createExploreResult("A->B"));
        when(agentClient.explore(eq("A"), ArgumentMatchers.eq(AgentNetworkTestUtils.createAgentInfo("C=reachable:1").get()), any()))
            .thenReturn(AgentNetworkTestUtils.createExploreResult("A->C"));

        NetworkConfiguration configuration = AgentNetworkTestUtils.createNetworkConfiguration("A=self:1", "B=reachable:1", "C=reachable:1");

        ExploreResult links = exploreAgentsService.explore(configuration);

        assertThat(links.agentLinks())
            .hasSize(2)
            .extracting(link -> link.source().name() + "->" + link.destination().name())
            .containsExactlyInAnyOrder("A->B", "A->C");
    }

    @Test
    public void explore_to_agent_in_exploration_phase_returns_empty() {
        when(explorations.changeStateToIfPossible(any(), any())).thenReturn(false);

        ExploreResult links = exploreAgentsService.explore(null);

        assertThat(links.agentLinks()).hasSize(0);
    }

    @Test
    public void explore_with_configuration_containing_reachable_targets_returns_links_to_these_targets() {
        when(explorations.changeStateToIfPossible(any(), any())).thenReturn(true);
        when(localServerIdentifier.getLocalName(any())).thenReturn("A");
        when(agentClient.explore(eq("A"), ArgumentMatchers.eq(AgentNetworkTestUtils.createAgentInfo("B=reachable:1").get()), any()))
            .thenReturn(AgentNetworkTestUtils.createExploreResult("A->B", "B->e1|s2"));
        when(connectionChecker.canConnectTo(any())).thenReturn(true);

        NetworkConfiguration configuration = AgentNetworkTestUtils.createNetworkConfiguration(
            "A=self:1", "B=reachable:1",
            "e1|s1=reachable:1", "e1|s2=reachable:1"
        );

        ExploreResult links = exploreAgentsService.explore(configuration);

        assertThat(links.agentLinks())
            .hasSize(1)
            .extracting(link -> link.source().name() + "->" + link.destination().name())
            .containsExactlyInAnyOrder("A->B");

        assertThat(links.targetLinks())
            .hasSize(3)
            .extracting(link -> link.source().name() + "->" + link.destination().name)
            .containsExactlyInAnyOrder("A->s1", "A->s2", "B->s2");
    }

    @Test
    public void explore_with_configuration_containing_unreachable_targets_returns_links_without_these_targets() {
        when(explorations.changeStateToIfPossible(any(), any())).thenReturn(true);
        when(localServerIdentifier.getLocalName(any())).thenReturn("A");
        when(agentClient.explore(eq("A"), ArgumentMatchers.eq(AgentNetworkTestUtils.createAgentInfo("B=reachable:1").get()), any()))
            .thenReturn(AgentNetworkTestUtils.createExploreResult("A->B", "B->e1|s2"));
        when(connectionChecker.canConnectTo(eq(new NamedHostAndPort("s1", "reachable", 1)))).thenReturn(true);
        when(connectionChecker.canConnectTo(eq(new NamedHostAndPort("s2", "unreachable", 1)))).thenReturn(false);

        NetworkConfiguration configuration = AgentNetworkTestUtils.createNetworkConfiguration(
            "A=self:1", "B=reachable:1",
            "e1|s1=reachable:1", "e1|s2=unreachable:1"
        );

        ExploreResult links = exploreAgentsService.explore(configuration);

        assertThat(links.agentLinks())
            .hasSize(1)
            .extracting(link -> link.source().name() + "->" + link.destination().name())
            .containsExactlyInAnyOrder("A->B");

        assertThat(links.targetLinks())
            .hasSize(2)
            .extracting(link -> link.source().name() + "->" + link.destination().name)
            .containsExactlyInAnyOrder("A->s1", "B->s2");
    }

    @Test
    public void wrapup_should_change_state_and_call_client_for_each_host() {
        NetworkDescription networkDescription = mock(NetworkDescription.class, RETURNS_DEEP_STUBS);
        NamedHostAndPort ani1 = mock(NamedHostAndPort.class);
        NamedHostAndPort ani2 = mock(NamedHostAndPort.class);
        when(networkDescription.configuration().agentNetworkConfiguration().stream())
            .thenReturn(Stream.of(ani1, ani2));

        when(explorations.changeStateToIfPossible(networkDescription.configuration(), ConfigurationState.WRAPING_UP))
            .thenReturn(true);
        when(localServerIdentifier.getLocalName(any())).thenReturn("localName");

        exploreAgentsService.wrapUp(networkDescription);

        verify(agentClient).wrapUp(same(ani1), same(networkDescription));
        verify(agentClient).wrapUp(same(ani2), same(networkDescription));
        verify(explorations).changeStateToIfPossible(networkDescription.configuration(), ConfigurationState.FINISHED);
    }

    @Test
    public void allready_wrapingup_wrapup_should_do_nothing() {
        NetworkDescription networkDescription = mock(NetworkDescription.class, RETURNS_DEEP_STUBS);

        when(explorations.changeStateToIfPossible(networkDescription.configuration(), ConfigurationState.WRAPING_UP))
            .thenReturn(false);

        exploreAgentsService.wrapUp(networkDescription);

        verify(agentClient, never()).wrapUp(any(), any());
        verify(explorations, never()).changeStateToIfPossible(networkDescription.configuration(), ConfigurationState.FINISHED);
    }
}
