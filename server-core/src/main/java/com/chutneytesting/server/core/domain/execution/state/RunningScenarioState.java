package com.chutneytesting.server.core.domain.execution.state;

import com.chutneytesting.server.core.domain.execution.state.ImmutableRunningScenarioState;
import java.time.Instant;
import org.immutables.value.Value;

@Value.Immutable
public interface RunningScenarioState {
    @Value.Parameter
    String scenarioId();

    @Value.Derived
    default Instant startTime() {
        return Instant.now();
    }

    static RunningScenarioState of(String scenarioId) {
        return ImmutableRunningScenarioState.of(scenarioId);
    }
}
