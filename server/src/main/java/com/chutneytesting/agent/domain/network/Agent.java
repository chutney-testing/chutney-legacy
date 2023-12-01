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

package com.chutneytesting.agent.domain.network;

import com.chutneytesting.agent.domain.TargetId;
import com.chutneytesting.engine.domain.delegation.NamedHostAndPort;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Agent {

    private static final Logger LOGGER = LoggerFactory.getLogger(Agent.class);

    public final NamedHostAndPort agentInfo; // TODO any - why NamedHostAndPort does not have an AgentId + why member is agentInfo which sounds like Target which has the right to be a real class ?
    private final Set<Agent> reachableAgents = new LinkedHashSet<>(); // TODO any - why have Agent and not AgentId
    private final Set<TargetId> reachableTargets = new LinkedHashSet<>(); // TODO any - why have TargetId and not Target
    // TODO any - all these words, attribute name and classes are not coherent and are mostly confusing

    public Agent(NamedHostAndPort agentInfo) {
        this.agentInfo = agentInfo;
    }

    public Agent addReachable(Agent agent) {
        reachableAgents.add(agent);
        return this;
    }

    public Set<Agent> reachableAgents() {
        return new LinkedHashSet<>(reachableAgents);
    }

    public Agent addReachable(TargetId target) {
        reachableTargets.add(target);
        return this;
    }

    public Set<TargetId> reachableTargets() {
        return new LinkedHashSet<>(reachableTargets);
    }

    public List<Agent> findFellowAgentForReaching(String targetName, String environment) {
        List<Agent> result = new ArrayList<>();
        Optional<Agent> resultFound = findNext(this, TargetId.of(targetName, environment),  Sets.newHashSet(this), result);
        if (resultFound.isPresent()) {
            Collections.reverse(result);
            return result;
        } else {
            return Collections.emptyList();
        }
    }

    private Optional<Agent> findNext(Agent agent, TargetId target, Set<Agent> scannedAgents, List<Agent> accumulator) {

        if (agent.reachableTargets().contains(target)) {
            LOGGER.debug("Target reachable by agent " + agent.agentInfo.name());
            return Optional.of(agent);
        }
        LOGGER.debug("Target NOT reachable by agent " + agent.agentInfo.name());
        scannedAgents.add(agent);

        Optional<Agent> agentFound = agent.reachableAgents.stream()
            .filter(nextAgent -> !scannedAgents.contains(nextAgent))
            .filter(nextAgent -> findNext(nextAgent, target, Sets.newHashSet(scannedAgents), accumulator).isPresent())
            .findFirst();

        if (agentFound.isPresent()) {
            LOGGER.debug("Next Agent found " + agentFound.get().agentInfo.name());
            accumulator.add(agentFound.get());
        } else {
            LOGGER.debug("No next agent found. Will try to execute with local agent.");
        }
        return agentFound;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Agent agent = (Agent) o;
        return Objects.equals(agentInfo, agent.agentInfo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(agentInfo);
    }

    @Override
    public String toString() {
        return "Agent: " + agentInfo.toString();
    }
}
