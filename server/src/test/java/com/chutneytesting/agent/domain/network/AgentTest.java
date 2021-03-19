package com.chutneytesting.agent.domain.network;

import static org.assertj.core.api.Assertions.assertThat;

import com.chutneytesting.agent.AgentNetworkTestUtils;
import com.chutneytesting.agent.domain.TargetId;
import com.chutneytesting.agent.domain.configure.NetworkConfiguration;
import com.chutneytesting.agent.domain.explore.AgentId;
import com.chutneytesting.agent.domain.explore.ExploreResult;
import com.chutneytesting.agent.domain.explore.ImmutableExploreResult;
import com.chutneytesting.engine.domain.delegation.NamedHostAndPort;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

public class AgentTest {

    @Test
    public void should_return_empty_when_no_route_is_found_to_target() throws Exception {

        // Given
        NetworkConfiguration networkConfiguration = AgentNetworkTestUtils.createNetworkConfiguration(
            "A=agent:9350",
            "envZ|unreachable=url:80"
        );

        // No routes
        ExploreResult exploreResult = ImmutableExploreResult.of(
            // Agent -> Agent links
            ImmutableExploreResult.Links.of(
                Collections.emptyList()
            ),
            // Agent -> Target links
            ImmutableExploreResult.Links.of(
                Collections.emptyList()
            ));

        AgentGraph agentGraph = AgentGraph.of(exploreResult, networkConfiguration);

        Agent sourceAgent = agentGraph.getBy(AgentId.of("A")).get();

        // When + then
        assertThat(sourceAgent.findFellowAgentForReaching("unreachable", "env")).isEmpty();
    }

    @Test
    public void should_return_current_agent_as_the_next_hop_to_join_the_target() throws Exception {

        // Given
        NetworkConfiguration networkConfiguration = AgentNetworkTestUtils.createNetworkConfiguration(
            "A=agent:9350", "B=agentB:9350",
            "envA|reachable=url:80"
        );

        // Route : A -> reachable
        ExploreResult exploreResult = ImmutableExploreResult.of(
            // Agent -> Agent links
            ImmutableExploreResult.Links.of(
                Collections.emptyList()
            ),
            // Agent -> Target links
            ImmutableExploreResult.Links.of(
                Collections.singletonList(
                    ImmutableExploreResult.Link.of(AgentId.of("A"), TargetId.of("reachable", "env"))
                )
            ));

        AgentGraph agentGraph = AgentGraph.of(exploreResult, networkConfiguration);

        Agent sourceAgent = agentGraph.getBy(AgentId.of("A")).get();

        // When
        List<Agent> actual = sourceAgent.findFellowAgentForReaching("reachable", "env");

        // Then
        assertThat(actual).isEmpty();
    }

    @Test
    public void should_return_fellow_agent_as_the_next_hop_to_join_the_target() throws Exception {
        // Given
        NetworkConfiguration networkConfiguration = AgentNetworkTestUtils.createNetworkConfiguration(
            "A=agent:9350", "B=agentB:9350",
            "envA|reachable=url:80"
        );

        // Route : A -> B -> reachable
        ExploreResult exploreResult = ImmutableExploreResult.of(
            // Agent -> Agent links
            ImmutableExploreResult.Links.of(
                Collections.singletonList(
                    ImmutableExploreResult.Link.of(AgentId.of("A"), AgentId.of("B"))
                )
            ),
            // Agent -> Target links
            ImmutableExploreResult.Links.of(
                Collections.singletonList(
                    ImmutableExploreResult.Link.of(AgentId.of("B"), TargetId.of("reachable", "env"))
                )
            ));

        AgentGraph agentGraph = AgentGraph.of(exploreResult, networkConfiguration);

        Agent sourceAgent = agentGraph.getBy(AgentId.of("A")).get();

        // When
        List<Agent> actual = sourceAgent.findFellowAgentForReaching("reachable", "env");

        // Then
        Agent expectedAgent = new Agent(new NamedHostAndPort("B", "agentB", 9350));
        assertThat(actual).containsExactly(expectedAgent);
    }

    @Test
    public void should_return_local_even_when_a_fellow_has_a_route() throws Exception {

        // Given
        NetworkConfiguration networkConfiguration = AgentNetworkTestUtils.createNetworkConfiguration(
            "A=agent:9350", "B=agentB:9350",
            "envA|reachable=url:80"
        );

        // Route : A -> B
        //          \  /
        //        reachable
        // Target is unreachable
        ExploreResult exploreResult = ImmutableExploreResult.of(
            // Agent -> Agent links
            ImmutableExploreResult.Links.of(
                Arrays.asList(
                    ImmutableExploreResult.Link.of(AgentId.of("A"), AgentId.of("B"))
                )
            ),
            // Agent -> Target links
            ImmutableExploreResult.Links.of(
                Arrays.asList(
                    ImmutableExploreResult.Link.of(AgentId.of("A"), TargetId.of("reachable", "env")),
                    ImmutableExploreResult.Link.of(AgentId.of("B"), TargetId.of("reachable", "env"))
                )
            ));

        AgentGraph agentGraph = AgentGraph.of(exploreResult, networkConfiguration);

        Agent sourceAgent = agentGraph.getBy(AgentId.of("A")).get();

        // When
        List<Agent> actual = sourceAgent.findFellowAgentForReaching("reachable", "env");

        // Then
        assertThat(actual).isEmpty();
    }

    @Test
    public void should_return_closer_agent_as_the_next_hop_to_join_the_target() throws Exception {
        // Given
        NetworkConfiguration networkConfiguration = AgentNetworkTestUtils.createNetworkConfiguration(
            "A=agent:9350", "B=agentB:9350", "C=agentC:9350",
            "envA|reachable=url:80"
        );

        // Route : A -> B -> C -> reachable
        ExploreResult exploreResult = ImmutableExploreResult.of(
            // Agent -> Agent links
            ImmutableExploreResult.Links.of(
                Arrays.asList(
                    ImmutableExploreResult.Link.of(AgentId.of("A"), AgentId.of("B")),
                    ImmutableExploreResult.Link.of(AgentId.of("B"), AgentId.of("C"))
                )
            ),
            // Agent -> Target links
            ImmutableExploreResult.Links.of(
                Collections.singletonList(
                    ImmutableExploreResult.Link.of(AgentId.of("C"), TargetId.of("reachable", "env"))
                )
            ));

        AgentGraph agentGraph = AgentGraph.of(exploreResult, networkConfiguration);

        Agent sourceAgent = agentGraph.getBy(AgentId.of("A")).get();

        // When
        List<Agent> actual = sourceAgent.findFellowAgentForReaching("reachable", "env");

        // Then
        Agent expectedAgent1 = new Agent(new NamedHostAndPort("B", "agentB", 9350));
        Agent expectedAgent2 = new Agent(new NamedHostAndPort("C", "agentC", 9350));
        assertThat(actual).containsExactly(expectedAgent1, expectedAgent2);
    }

    @Test
    public void should_return_empty_when_no_routes_found_and_all_agents_were_scanned_avoiding_infinite_loops() throws Exception {

        // Given
        NetworkConfiguration networkConfiguration = AgentNetworkTestUtils.createNetworkConfiguration(
            "A=agent:9350", "B=agentB:9350",
            "envA|unreachable=url:80"
        );

        // Route : A <-> B
        // Target is unreachable
        ExploreResult exploreResult = ImmutableExploreResult.of(
            // Agent -> Agent links
            ImmutableExploreResult.Links.of(
                Arrays.asList(
                    ImmutableExploreResult.Link.of(AgentId.of("A"), AgentId.of("B")),
                    ImmutableExploreResult.Link.of(AgentId.of("B"), AgentId.of("A"))
                )
            ),
            // Agent -> Target links
            ImmutableExploreResult.Links.of(
                Collections.emptyList()
            ));

        AgentGraph agentGraph = AgentGraph.of(exploreResult, networkConfiguration);

        Agent sourceAgent = agentGraph.getBy(AgentId.of("A")).get();

        // When + then
        assertThat(sourceAgent.findFellowAgentForReaching("unreachable", "env")).isEmpty();
    }

    @Test
    public void should_return_empty_when_no_routes_found_and_all_agents_were_scanned_avoiding_infinite_loops_v2() throws Exception {

        // Given
        NetworkConfiguration networkConfiguration = AgentNetworkTestUtils.createNetworkConfiguration(
            "A=agent:9350", "B=agentB:9350", "C=agentC:9350",
            "envA|unreachable=url:80"
        );

        // Route :
        // A - B
        //  \ /
        //   C
        // Target is unreachable
        ExploreResult exploreResult = ImmutableExploreResult.of(
            // Agent -> Agent links
            ImmutableExploreResult.Links.of(
                Arrays.asList(
                    ImmutableExploreResult.Link.of(AgentId.of("A"), AgentId.of("B")),
                    ImmutableExploreResult.Link.of(AgentId.of("B"), AgentId.of("C")),
                    ImmutableExploreResult.Link.of(AgentId.of("C"), AgentId.of("A"))
                )
            ),
            // Agent -> Target links
            ImmutableExploreResult.Links.of(
                Collections.emptyList()
            ));

        AgentGraph agentGraph = AgentGraph.of(exploreResult, networkConfiguration);

        Agent sourceAgent = agentGraph.getBy(AgentId.of("A")).get();

        // When + then
        assertThat(sourceAgent.findFellowAgentForReaching("unreachable", "env")).isEmpty();
    }
}
