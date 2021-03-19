package com.chutneytesting.agent.domain.network;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.chutneytesting.agent.AgentNetworkTestUtils;
import com.chutneytesting.agent.domain.TargetId;
import com.chutneytesting.agent.domain.configure.NetworkConfiguration;
import com.chutneytesting.agent.domain.explore.AgentId;
import com.chutneytesting.agent.domain.explore.ExploreResult;
import com.chutneytesting.agent.domain.explore.ImmutableExploreResult;
import com.chutneytesting.agent.domain.explore.ImmutableExploreResult.Link;
import com.chutneytesting.agent.domain.explore.ImmutableExploreResult.Links;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.function.Predicate;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

public class AgentGraphTest {

    @Test
    public void of_should_rebuild_the_graph() {
        NetworkConfiguration networkConfiguration = AgentNetworkTestUtils.createNetworkConfiguration("environmentName",
            "A=self:1", "B=reachable1:1", "C=level2:1", "D=reachable2:1",
            "e1|s1=reachable:1", "e1|s2=reachable:1", "e2|s3=level2:1"
        );
        ExploreResult exploreResult = ImmutableExploreResult.of(
            Links.of(
                Arrays.asList(
                    Link.of(AgentId.of("A"), AgentId.of("B")),
                    Link.of(AgentId.of("A"), AgentId.of("D")),
                    Link.of(AgentId.of("B"), AgentId.of("C")),
                    Link.of(AgentId.of("B"), AgentId.of("D"))
                )
            ), Links.of(
                Arrays.asList(
                    Link.of(AgentId.of("A"), TargetId.of("s1", "env")),
                    Link.of(AgentId.of("A"), TargetId.of("s2", "env")),
                    Link.of(AgentId.of("D"), TargetId.of("s3", "env"))
                )
            ));

        AgentGraph agentGraph = AgentGraph.of(exploreResult, networkConfiguration);

        Set<Agent> agents = agentGraph.agents();

        Assertions.assertThat(agents).hasSize(4);

        // Asserts about agent links
        Assertions.assertThat(agents).haveExactly(1, matchAgent("A", agent -> containsAll(agent.reachableAgents(), hasName("B"), hasName("D")), "agentLinks"));
        Assertions.assertThat(agents).haveExactly(1, matchAgent("B", agent -> containsAll(agent.reachableAgents(), hasName("C"), hasName("D")), "agentLinks"));
        Assertions.assertThat(agents).haveExactly(1, matchAgent("C", agent -> agent.reachableAgents().size() == 0, "agentLinks"));
        Assertions.assertThat(agents).haveExactly(1, matchAgent("D", agent -> agent.reachableAgents().size() == 0, "agentLinks"));

        // Asserts about target links
        Assertions.assertThat(agents).haveExactly(1, matchAgent("A", agent -> containsAll(agent.reachableTargets(), hasId("s1"), hasId("s2")), "agentLinks"));
        Assertions.assertThat(agents).haveExactly(1, matchAgent("B", agent -> agent.reachableTargets().size() == 0, "agentLinks"));
        Assertions.assertThat(agents).haveExactly(1, matchAgent("C", agent -> agent.reachableTargets().size() == 0, "agentLinks"));
        Assertions.assertThat(agents).haveExactly(1, matchAgent("D", agent -> containsAll(agent.reachableTargets(), hasId("s3")), "agentLinks"));
    }

    @Test()
    public void of_with_agent_links_not_in_conf_should_fail() {
        NetworkConfiguration networkConfiguration = AgentNetworkTestUtils.createNetworkConfiguration("environmentName", "A=self:1", "B=reachable:1", "C=level2:1", "D=reachable2:1");
        ExploreResult exploreResult = ImmutableExploreResult.of(
            Links.of(
                Arrays.asList(
                    Link.of(AgentId.of("A"), AgentId.of("B")),
                    Link.of(AgentId.of("A"), AgentId.of("D")),
                    Link.of(AgentId.of("B"), AgentId.of("C")),
                    Link.of(AgentId.of("B"), AgentId.of("D")),
                    Link.of(AgentId.of("B"), AgentId.of("E"))
                )), ExploreResult.Links.empty());

        assertThatThrownBy(() -> AgentGraph.of(exploreResult, networkConfiguration))
            .isInstanceOf(IllegalStateException.class);
    }

    @Test()
    public void of_with_target_links_not_in_conf_should_fail() {
        NetworkConfiguration networkConfiguration = AgentNetworkTestUtils.createNetworkConfiguration(
            "A=self:1", "B=reachable:1",
            "e1|s1=reachable:1", "e1|s2=unreachable:1"
        );
        ExploreResult exploreResult = ImmutableExploreResult.of(
            Links.of(
                Arrays.asList(
                    Link.of(AgentId.of("A"), AgentId.of("B"))
                )), Links.of(
                Arrays.asList(
                    Link.of(AgentId.of("A"), TargetId.of("s1", "env")),
                    Link.of(AgentId.of("A"), TargetId.of("s3", "env"))
                )
            ));

        assertThatThrownBy(() -> AgentGraph.of(exploreResult, networkConfiguration))
            .isInstanceOf(IllegalStateException.class);
    }

    /**
     * When the  LocalServerIdentifier
     * serves a different address than the one used to access the node, an extra node is created describing the agent
     * with the hostname as name.
     */
    @Test
    public void graph_can_find_agents_by_host_when_self_created_during_discovery() {
        NetworkConfiguration networkConfiguration = AgentNetworkTestUtils.createNetworkConfiguration("A=self:1", "B=reachable:1", "C=level2:1", "D=reachable2:1");
        ExploreResult exploreResult = ImmutableExploreResult.of(
            Links.of(
                Arrays.asList(
                    Link.of(AgentId.of("A"), AgentId.of("B")),
                    Link.of(AgentId.of("A"), AgentId.of("D")),
                    Link.of(AgentId.of("B"), AgentId.of("C")),
                    Link.of(AgentId.of("B"), AgentId.of("reachable2"))
                )), ExploreResult.Links.empty());

        AgentGraph agentGraph = AgentGraph.of(exploreResult, networkConfiguration);

        Set<Agent> agents = agentGraph.agents();

        Assertions.assertThat(agents).hasSize(4);

        // Asserts about agent links
        Assertions.assertThat(agents).haveExactly(1, matchAgent("B", agent -> containsAll(agent.reachableAgents(), hasName("C"), hasName("D")), "agentLinks"));
    }

    private Condition<Agent> matchAgent(String agentName, Predicate<Agent> predicate, String agentLinks) {
        return new Condition<>(agent -> agent.agentInfo.name().equals(agentName) &&
            predicate.test(agent), "agent %s with " + agentLinks, agentName);
    }

    @SafeVarargs
    private final <T> boolean containsAll(Collection<T> items, Predicate<T>... tests) {
        for (Predicate<T> test : tests)
            if (items.stream().noneMatch(test))
                return false;
        return true;
    }

    private Predicate<Agent> hasName(String agentName) {
        return agent -> agent.agentInfo.name().equals(agentName);
    }

    private Predicate<TargetId> hasId(String name) {
        return targetId -> targetId.name.equals(name);
    }

}
