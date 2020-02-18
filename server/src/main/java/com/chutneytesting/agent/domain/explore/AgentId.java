package com.chutneytesting.agent.domain.explore;

import org.immutables.value.Value;

@Value.Immutable
public interface AgentId {
    @Value.Parameter
    String name();

    static AgentId of(String name) {
        return ImmutableAgentId.of(name);
    }
}
