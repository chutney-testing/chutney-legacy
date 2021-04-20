package com.chutneytesting.agent.infra.storage;

import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

import com.chutneytesting.agent.domain.TargetId;
import com.chutneytesting.agent.domain.configure.ImmutableNetworkConfiguration;
import com.chutneytesting.agent.domain.network.Agent;
import com.chutneytesting.agent.domain.network.AgentGraph;
import com.chutneytesting.agent.domain.network.ImmutableNetworkDescription;
import com.chutneytesting.agent.domain.network.NetworkDescription;
import com.chutneytesting.engine.domain.delegation.NamedHostAndPort;
import com.chutneytesting.environment.domain.Environment;
import com.chutneytesting.environment.domain.Target;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

public class AgentNetworkMapperJsonFileMapperTest {

    private final AgentNetworkMapperJsonFileMapper mapper = new AgentNetworkMapperJsonFileMapper();

    @Test
    public void toDto_should_map_every_information() {
        Agent agent = new Agent(new NamedHostAndPort("name", "host", 42));
        Agent reachableAgent = new Agent(new NamedHostAndPort("reachable", "host2", 42));
        Target target = Target.builder()
            .withName("targetName")
            .withEnvironment("env")
            .withUrl("prot://me:42")
            .build();
        Environment environment = Environment.builder().withName("env").addTarget(target).build();
        TargetId targetId = TargetId.of("targetName", "env");
        agent.addReachable(reachableAgent);
        reachableAgent.addReachable(targetId);

        ImmutableNetworkDescription network = ImmutableNetworkDescription.builder()
            .configuration(ImmutableNetworkConfiguration.builder()
                .creationDate(Instant.now())
                .agentNetworkConfiguration(ImmutableNetworkConfiguration.AgentNetworkConfiguration.of(Arrays.asList(agent.agentInfo, reachableAgent.agentInfo)))
                .environmentConfiguration(ImmutableNetworkConfiguration.EnvironmentConfiguration.of(singleton(environment)))
                .build())
            .agentGraph(new AgentGraph(Arrays.asList(agent, reachableAgent)))
            .build();

        AgentNetworkForJsonFile dto = mapper.toDto(network);

        AgentForJsonFile firstAgent = dto.agents.get(0);
        assertThat(firstAgent.name).isEqualTo("name");
        assertThat(firstAgent.host).isEqualTo("host");
        assertThat(firstAgent.port).isEqualTo(42);
        assertThat(firstAgent.reachableAgentNames).contains("reachable");
        assertThat(firstAgent.reachableTargetIds).isEmpty();

        AgentForJsonFile secondAgent = dto.agents.get(1);
        assertThat(secondAgent.name).isEqualTo("reachable");
        assertThat(secondAgent.host).isEqualTo("host2");
        assertThat(secondAgent.port).isEqualTo(42);
        assertThat(secondAgent.reachableAgentNames).isEmpty();
        assertThat(secondAgent.reachableTargetIds).singleElement().hasFieldOrPropertyWithValue("name", "targetName");
    }

    @Test
    public void fromDto_should_rebuild_everything() {
        String targetName = "targetName";

        TargetForJsonFile target = new TargetForJsonFile();
        target.name = targetName;
        target.environment = "env";

        AgentNetworkForJsonFile networkJson = new AgentNetworkForJsonFile();
        networkJson.configurationCreationDate = Instant.now();
        AgentForJsonFile agent1Json = createAgentJson("agent1", "host1", singletonList("agent2"), emptyList());
        AgentForJsonFile agent2Json = createAgentJson("agent2", "host2", emptyList(), singletonList(target));
        networkJson.agents = Arrays.asList(agent1Json, agent2Json);

        List<Target> targets = new ArrayList<>();
        targets.add(
            Target.builder()
                .withName(targetName)
                .withEnvironment("env")
                .withUrl("http://s1:90")
                .build()
        );
        Environment environment = Environment.builder().withName("env").addAllTargets(targets).build();

        NetworkDescription description = mapper.fromDto(networkJson, singletonList(environment));

        assertThat(description.agentGraph().agents()).hasSize(2);
        assertThat(description.agentGraph().agents()).haveAtLeastOne(agentThatMatch(agent1Json));
        assertThat(description.agentGraph().agents()).haveAtLeastOne(agentThatMatch(agent2Json));
    }

    private Condition<Agent> agentThatMatch(AgentForJsonFile expectedAgent) {
        return new Condition<>(actualAgent -> {
            boolean result = actualAgent.agentInfo.name().equals(expectedAgent.name);
            result &= actualAgent.agentInfo.host().equals(expectedAgent.host);
            result &= actualAgent.agentInfo.port() == expectedAgent.port;

            result &= actualAgent.reachableAgents().stream()
                .map(_agent -> _agent.agentInfo.name())
                .allMatch(expectedAgent.reachableAgentNames::contains);

            result &= actualAgent.reachableTargets().stream()
                .allMatch(reachableTarget -> expectedAgent.reachableTargetIds.stream().anyMatch(targetJson ->
                    targetJson.name.equals(reachableTarget.name)));

            return result;
        }, expectedAgent.name);
    }

    private AgentForJsonFile createAgentJson(String agentName, String agentHost, List<String> reachableAgents, List<TargetForJsonFile> reachableTargets) {
        AgentForJsonFile agent1Json = new AgentForJsonFile();
        agent1Json.name = agentName;
        agent1Json.host = agentHost;
        agent1Json.port = 42;
        agent1Json.reachableAgentNames = reachableAgents;
        agent1Json.reachableTargetIds = reachableTargets;
        return agent1Json;
    }
}
