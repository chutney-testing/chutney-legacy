package com.chutneytesting.agent.domain.network;

import com.chutneytesting.agent.domain.configure.NetworkConfiguration;
import com.chutneytesting.agent.domain.explore.AgentId;
import com.chutneytesting.agent.domain.explore.ExploreResult;
import com.chutneytesting.agent.domain.explore.ExploreResult.Link;
import com.chutneytesting.design.domain.environment.Target;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AgentGraph {

    public static AgentGraph of(ExploreResult exploreResult, NetworkConfiguration networkConfiguration) {
        Map<String, Agent> agentsByName = indexAgents(networkConfiguration, agent -> agent.agentInfo.name());
        Map<String, Agent> agentsByHost = indexAgents(networkConfiguration, agent -> agent.agentInfo.host());

        for (Link<AgentId, AgentId> agentLink : exploreResult.agentLinks()) {
            Agent sourceAgent = searchInConfiguration(agentsByName, agentsByHost, agentLink.source());
            Agent destAgent = searchInConfiguration(agentsByName, agentsByHost, agentLink.destination());
            if (sourceAgent == null) {
                throw new IllegalStateException(String.format("the agent [%s] is declared as source but does not exist in configuration", agentLink.source()));
            }
            if (destAgent == null) {
                throw new IllegalStateException(String.format("the agent [%s] is declared as destination but does not exist in configuration", agentLink.destination()));
            }

            sourceAgent.addReachable(destAgent);
        }

        Map<Target.TargetId, Target> targetById = networkConfiguration.environmentConfiguration().stream()
            .flatMap( e -> e.targets.stream())
            .collect(Collectors.toMap(t -> t.id, Function.identity()));

        for(Link<AgentId, Target.TargetId> targetLink : exploreResult.targetLinks()) {
            Agent sourceAgent = agentsByName.get(targetLink.source().name());
            Target destTarget = targetById.get(targetLink.destination());
            if (sourceAgent == null)
                throw new IllegalStateException(String.format("the agent [%s] is declared as source but does not exist in configuration", targetLink.source()));
            if (destTarget == null)
                throw new IllegalStateException(String.format("the target [%s] is declared as destination but does not exist in configuration", targetLink.destination()));
            sourceAgent.addReachable(destTarget.id);
        }

        return new AgentGraph(new HashSet<>(agentsByName.values()));
    }

    private static Agent searchInConfiguration(Map<String, Agent> agentsByName, Map<String, Agent> agentsByHost, AgentId agentId) {
        return agentsByName.getOrDefault(agentId.name(), agentsByHost.get(agentId.name()));
    }

    private static Map<String, Agent> indexAgents(NetworkConfiguration networkConfiguration, Function<Agent, String> indexFunction) {
        return networkConfiguration.agentNetworkConfiguration().stream()
            .map(Agent::new)
            .collect(Collectors.toMap(indexFunction, Function.identity()));
    }

    private final Set<Agent> agents;

    public AgentGraph(Collection<Agent> agents) {
        this.agents = new LinkedHashSet<>(agents);
    }

    public Set<Agent> agents() {
        return new LinkedHashSet<>(agents);
    }

    Optional<Agent> getBy(AgentId agentId) {
        return agents.stream()
            .filter(agent -> agent.agentInfo.name().equals(agentId.name()))
            .findFirst();
    }

}
